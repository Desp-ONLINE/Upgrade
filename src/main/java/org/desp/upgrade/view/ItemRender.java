package org.desp.upgrade.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.dto.UpgradeData;
import org.desp.upgrade.utils.ProtectItemCode;
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
//        Upgrade.register();

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

    public static ItemStack getProtectItem(boolean isProtect){
        ItemStack protectItem = new ItemStack(Material.PAPER, 1);

        if(isProtect){
            ItemMeta protectItemMeta = protectItem.getItemMeta();
            protectItemMeta.setCustomModelData(ProtectItemCode.PROTECT_TRUE);
            protectItemMeta.setDisplayName("§a 광휘의 빛 사용");
            protectItem.setItemMeta(protectItemMeta);

            List<String> protectItemLore = new ArrayList<>();
            protectItemLore.add("§7   현재 광휘의 빛 §a§n사용§7으로 설정되어 있습니다.");
            protectItemLore.add("§7   클릭 시 §c비활성화§7로 변경됩니다.");
            protectItemLore.add("");
            protectItemLore.add("§e   광휘의 빛이란?");
            protectItemLore.add("§f   - 강화 실패 시 재료가 소모되는 것을 방지해줍니다. 성공 시에도 광휘의 빛은 소모됩니다.");
            protectItem.setLore(protectItemLore);
        }else {
            ItemMeta protectItemMeta = protectItem.getItemMeta();
            protectItemMeta.setCustomModelData(ProtectItemCode.PROTECT_FALSE);
            protectItemMeta.setDisplayName("§c 광휘의 빛 미사용");
            protectItem.setItemMeta(protectItemMeta);

            List<String> protectItemLore = new ArrayList<>();
            protectItemLore.add("§7   현재 광휘의 빛 §c§n미사용§7으로 설정되어 있습니다.");
            protectItemLore.add("§7   클릭 시 §a활성화§7로 변경됩니다.");
            protectItemLore.add("");
            protectItemLore.add("§e   광휘의 빛이란?");
            protectItemLore.add("§f   - 강화 실패 시 재료가 소모되는 것을 방지해줍니다. 성공 시에도 광휘의 빛은 소모됩니다.");
            protectItem.setLore(protectItemLore);
        }
        return protectItem;

    }
}
