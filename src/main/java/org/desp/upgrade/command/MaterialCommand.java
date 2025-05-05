package org.desp.upgrade.command;

import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.desp.upgrade.ui.MaterialUI;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.database.Repository;
import org.desp.upgrade.view.ItemRender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        if (!player.isOp()){
            return false;
        }

        String beforeWeapon = strings[0];
        MaterialUI materialUI = new MaterialUI(beforeWeapon);

        Document weaponDocument = Repository.weapons.find(Filters.eq("beforeWeapon", beforeWeapon)).first();

        Document conditions = (Document) weaponDocument.get("conditions");
        if (conditions == null) {
            return false;
        }

        List<Document> materials = conditions.getList("material", Document.class);
        if (materials == null || materials.isEmpty()) {
            player.sendMessage("§c 강화 재료가 설정되지 않았습니다.");
            return false;
        }

        Inventory inventory = materialUI.getInventory();
        int slot = 0;

        ItemRender.renderMaterialOnCommand(player, materials, inventory, slot);

        player.openInventory(materialUI.getInventory());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] strings) {
        List<String> completions = new ArrayList<>();
        Set<String> keys = Upgrade.getAllWeaponData().keySet();
        completions.addAll(keys);
        return completions;
    }
}
