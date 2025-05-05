package org.desp.upgrade.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.Document;
import org.desp.upgrade.dto.UpgradeData;

public class Repository {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    public static MongoCollection<Document> weapons = null;
    private final Map<String, UpgradeData> weaponRepository = new HashMap<>();

    public Repository() {
        DBConfig connector = new DBConfig();
        String path = connector.getMongoConnectionContent();
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(path))
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("Upgrade");
        weapons = database.getCollection("Weapons");
    }

    public static void updateMaterial(String itemName, Map<String, Integer> materials) {
        Document filter = new Document("beforeWeapon", itemName);

        List<Document> materialDocs = new ArrayList<>();

        for (Entry<String, Integer> entry : materials.entrySet()) {
            Document materialDoc = new Document("id", entry.getKey())
                    .append("quantity", entry.getValue());
            materialDocs.add(materialDoc);
        }

        Document updateOperation = new Document("$set",
                new Document("conditions.material", materialDocs)
        );
        weapons.updateMany(filter, updateOperation);
    }

    public Map<String, UpgradeData> getAllWeaponData() {
        FindIterable<Document> documents = weapons.find();
        for (Document document : documents) {
            String itemName = document.getString("beforeWeapon");
            Document doc = weapons.find(Filters.eq("beforeWeapon", itemName)).first();
            if (doc != null) {
                Document conditions = (Document) doc.get("conditions");
                if (conditions != null) {
                    List<Map<String, Integer>> materialList = parseMaterials(conditions);
                    UpgradeData upgradeData = UpgradeData.create(
                            doc.getString("beforeWeapon"),
                            doc.getString("afterWeapon"),
                            doc.getInteger("cost"),
                            doc.getInteger("successPercentage"),
                            doc.getInteger("destructionPercentage"),
                            conditions.getInteger("level"),
                            conditions.getInteger("proceedQuest"),
                            materialList
                    );
                    weaponRepository.put(itemName, upgradeData);
                }
            }
        }
        return weaponRepository;
    }

    private List<Map<String, Integer>> parseMaterials(Document document) {
        List<Map<String, Integer>> materialList = new ArrayList<>();
        List<Document> materials = document.getList("material", Document.class);
        if (materials != null) {
            for (Document materialDoc : materials) {
                Map<String, Integer> materialMap = new HashMap<>();
                materialMap.put(materialDoc.getString("id"), materialDoc.getInteger("quantity"));
                materialList.add(materialMap);
            }
        }
        return materialList;
    }
}
