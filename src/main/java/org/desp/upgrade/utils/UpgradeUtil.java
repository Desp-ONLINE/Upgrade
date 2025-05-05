package org.desp.upgrade.utils;

import java.util.*;
import net.Indyuce.mmocore.api.MMOCoreAPI;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.dto.UpgradeData;

public class UpgradeUtil {

    public static void setLore(InventoryClickEvent e, UpgradeData weaponData) {
        ItemStack cursor = e.getInventory().getItem(UpgradeButtonSlot.SLOT);
        if (cursor == null || !cursor.hasItemMeta()) return;

        List<String> upgradeLore = Arrays.asList(
                "§f    강화 정보",
                "§a§m                                                ",
                "§e     강화 필요 레벨: §f" + weaponData.getLevel() + "Lv",
                "§e     강화 필요 비용: §f" + weaponData.getCost() + "골드",
                "§a§m                                                ",
                "§3     성공 확률: §f" + weaponData.getSuccessPercentage() + "%",
                "§c     실패 확률: §f" + (100 - weaponData.getSuccessPercentage()) + "%",
                "§4     파괴 확률: §f" + weaponData.getDestructionPercentage() + "%",
                "§a§m                                                ",
                "§6     필요 퀘스트: §f메인퀘스트 "+weaponData.getProceedQuest()
        );

        cursor.setLore(upgradeLore);
        e.getInventory().setItem(UpgradeButtonSlot.SLOT, cursor);
    }

    public static int getPlayerLevel(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        PlayerData playerData = new MMOCoreAPI(Upgrade.getInstance()).getPlayerData(player);
        return playerData.getLevel();
    }

    public static UpgradeResult getResult(String itemName) {
        UpgradeData upgradeData = Upgrade.getAllWeaponData().get(itemName);
        if (upgradeData == null) return null;

        double successPercentage = upgradeData.getSuccessPercentage();
        double destructionPercentage = upgradeData.getDestructionPercentage();
        double failPercentage = 100.0 - successPercentage - destructionPercentage;

        double randomValue = new Random().nextDouble() * 100.0;
        if (randomValue < successPercentage) return UpgradeResult.SUCCESS;
        if (randomValue < successPercentage + failPercentage) return UpgradeResult.FAIL;
        return UpgradeResult.DESTRUCTION;
    }

    public static Map<String, Integer> extractMaterials(Inventory inventory) {
        Map<String, Integer> materials = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                String itemId = MMOItems.getID(item);
                int quantity = item.getAmount();
                // ID를 키로, 수량을 값으로 추가
                materials.put(itemId, materials.getOrDefault(itemId, 0) + quantity);
            }
        }
        return materials;
    }
}
