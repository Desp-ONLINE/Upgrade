package org.desp.upgrade.view;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.dto.UpgradeData;
import org.desp.upgrade.utils.UpgradeButtonSlot;

public class ItemRender {

    public static void renderMaterialOnCommand(Player player, List<Document> materials, Inventory inventory, int slot) {
        for (Document materialDoc : materials) {
            String materialId = materialDoc.getString("id");
            int quantity = materialDoc.getInteger("quantity");
            ItemStack materialItem;
            if (MMOItems.plugin.getItem(Type.MISCELLANEOUS, materialId) == null) {
                materialItem = MMOItems.plugin.getItem(Type.SWORD, materialId);
            } else {
                materialItem = MMOItems.plugin.getItem(Type.MISCELLANEOUS, materialId);
            }

            if (materialItem != null) {
                materialItem.setAmount(quantity);
                inventory.setItem(slot, materialItem);
                slot++;
            } else {
                player.sendMessage("§c" + materialId + " 아이템을 찾을 수 없습니다.");
            }
        }
    }

    public static void renderAfterWeapon(ItemStack currentItem, String itemName, InventoryClickEvent e, Player player) {
        UpgradeData upgradeData = Upgrade.getAllWeaponData().get(itemName);
        if (upgradeData == null) {
            return;
        }

        String afterWeapon = upgradeData.getAfterWeapon();

        if (afterWeapon == null) {
            return;
        }
        ItemStack nextItem = null;
        TypeManager types = MMOItems.plugin.getTypes();
        for (Type type : types.getAll()) {
            if(MMOItems.plugin.getItem(type, afterWeapon) == null) {
                continue;
            } else {
                nextItem = MMOItems.plugin.getItem(type, afterWeapon);
            }
        }
        e.getInventory().setItem(UpgradeButtonSlot.BEFORE_SLOT, currentItem);
        e.getInventory().setItem(UpgradeButtonSlot.AFTER_SLOT, nextItem);
    }


    public static void renderMaterialsInventoryClick(String itemName, InventoryClickEvent e) {
        Upgrade.register();

        UpgradeData upgradeData = Upgrade.getAllWeaponData().get(itemName);
        if (upgradeData == null) {
            return;
        }

        List<Map<String, Integer>> materials = upgradeData.getMaterials();
        int slot = 36;

        for (Map<String, Integer> material : materials) {
            for (Entry<String, Integer> entry : material.entrySet()) {
                if (slot >= 54) {
                    break;
                }
                String materialId = entry.getKey();
                Integer quantity = entry.getValue();

                ItemStack materialItem = null;
                TypeManager types = MMOItems.plugin.getTypes();
                for (Type type : types.getAll()) {
                    if(MMOItems.plugin.getItem(type, materialId) == null) {
                        continue;
                    } else {
                        materialItem = MMOItems.plugin.getItem(type, materialId);
                    }
                }
                while (quantity > 64) {
                    System.out.println(quantity);
                    materialItem.setAmount(quantity);
                    e.getInventory().setItem(slot, materialItem);
                    quantity = quantity - 64;
                    slot++;
                }
                materialItem.setAmount(quantity);
                e.getInventory().setItem(slot, materialItem);
                slot++;
            }
        }
    }
}
