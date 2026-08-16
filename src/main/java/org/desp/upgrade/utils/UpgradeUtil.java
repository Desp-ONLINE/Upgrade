package org.desp.upgrade.utils;

import java.util.*;

import com.binggre.binggreapi.utils.NumberUtil;
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
        setLore(e, weaponData, false);
    }

    /**
     * 강화 버튼 로어를 갱신한다.
     * @param isPreview 미리보기 상태면 강화 불가 안내를 덧붙인다
     */
    public static void setLore(InventoryClickEvent e, UpgradeData weaponData, boolean isPreview) {
        ItemStack cursor = e.getInventory().getItem(UpgradeButtonSlot.SLOT);
        if (cursor == null || !cursor.hasItemMeta()) return;

        int boost = UpgradeEventManager.getBoost();
        int baseSuccess = weaponData.getSuccessPercentage();
        int destruction = weaponData.getDestructionPercentage();
        int boostedSuccess = Math.min(baseSuccess + boost, 100);
        int actualBoost = Math.max(0, boostedSuccess - baseSuccess);
        int fail = Math.max(0, 100 - boostedSuccess - destruction);

        String successLine = actualBoost > 0
                ? "§3     성공 확률: §f" + baseSuccess + "% §a(+" + actualBoost + "%)"
                : "§3     성공 확률: §f" + baseSuccess + "%";

        List<String> upgradeLore = new ArrayList<>(Arrays.asList(
                "§f    강화 정보",
                "§a§m                                                ",
                "§e     강화 필요 레벨: §f" + weaponData.getLevel() + "Lv",
                "§e     강화 필요 비용: §f" + NumberUtil.applyComma(weaponData.getCost()) + "골드",
                "§a§m                                                ",
                successLine,
                "§c     실패 확률: §f" + fail + "%",
                "§4     파괴 확률: §f" + destruction + "%",
                "§a§m                                                ",
                "§6     필요 퀘스트: §f메인 퀘스트 "+weaponData.getProceedQuest()
        ));

        if (isPreview) {
            upgradeLore.add("§a§m                                                ");
            upgradeLore.add("§7     [미리보기] §c현재 상태에서는 강화할 수 없습니다.");
            upgradeLore.add("§7     실제 강화는 인벤토리에서 아이템을 선택해주세요.");
        }

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

        double destructionPercentage = upgradeData.getDestructionPercentage();
        double successPercentage = Math.min(upgradeData.getSuccessPercentage() + UpgradeEventManager.getBoost(), 100.0);
        double failPercentage = Math.max(0.0, 100.0 - successPercentage - destructionPercentage);

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

    public static Map<String, Integer> getMaterials(Inventory inventory) {
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
