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
import org.bukkit.entity.Item;
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

    /** MMOItems 전체 타입에서 ID로 아이템을 찾는다. 없으면 null */
    public static ItemStack findMMOItem(String itemId) {
        if (itemId == null) {
            return null;
        }
        ItemStack found = null;
        TypeManager types = MMOItems.plugin.getTypes();
        for (Type type : types.getAll()) {
            ItemStack item = MMOItems.plugin.getItem(type, itemId);
            if (item != null) {
                found = item;
            }
        }
        return found;
    }

    /** 표시용 아이템 복사본에 안내 로어를 덧붙인다 (원본은 건드리지 않음) */
    public static ItemStack withHint(ItemStack item, List<String> hintLines) {
        if (item == null) {
            return null;
        }
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.addAll(hintLines);
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public static void renderAfterWeapon(ItemStack currentItem, String itemName, InventoryClickEvent e, Player player) {
        renderAfterWeapon(currentItem, itemName, e, false);
    }

    /**
     * 강화 전/후 아이템 슬롯을 렌더링한다.
     * @param isPreview 미리보기(다음 단계 확인) 상태 여부 - 이전 단계로 돌아가기 안내를 표시
     */
    public static void renderAfterWeapon(ItemStack currentItem, String itemName, InventoryClickEvent e, boolean isPreview) {
        UpgradeData upgradeData = Upgrade.getAllWeaponData().get(itemName);
        if (upgradeData == null) {
            return;
        }

        String afterWeapon = upgradeData.getAfterWeapon();

        if (afterWeapon == null) {
            return;
        }
        ItemStack nextItem = findMMOItem(afterWeapon);

        // 이전 단계(왼쪽) 아이템 - 미리보기 중이면 돌아가기 안내
        ItemStack beforeDisplay = currentItem;
        if (isPreview) {
            beforeDisplay = withHint(currentItem, List.of(
                    "§7 [미리보기] 실제 강화는 인벤토리에서 아이템을 선택해주세요.",
                    "§e ◀ 클릭 시 이전 강화 단계로 돌아갑니다."));
        }

        // 다음 단계(오른쪽) 아이템 - 다음 강화 데이터가 있으면 이어보기 안내
        ItemStack afterDisplay = nextItem;
        if (nextItem != null) {
            boolean hasNext = Upgrade.getAllWeaponData().containsKey(afterWeapon);
            afterDisplay = withHint(nextItem, hasNext
                    ? List.of("§e ▶ 클릭 시 다음 강화 단계를 미리 볼 수 있습니다.")
                    : List.of("§7 마지막 강화 단계입니다."));
        }

        e.getInventory().setItem(UpgradeButtonSlot.BEFORE_SLOT, beforeDisplay);
        e.getInventory().setItem(UpgradeButtonSlot.AFTER_SLOT, afterDisplay);
    }


    public static void renderMaterialsInventoryClick(String itemName, InventoryClickEvent e) {
//        Upgrade.register();

        UpgradeData upgradeData = Upgrade.getAllWeaponData().get(itemName);
        if (upgradeData == null) {
            return;
        }

        List<Map<String, Integer>> materials = upgradeData.getMaterials();
        int slot = 36;

        for(int i = slot; i<54;i++){
            e.getInventory().setItem(i, new ItemStack(Material.AIR));
        }

        for (Map<String, Integer> material : materials) {
            for (Entry<String, Integer> entry : material.entrySet()) {
                if (slot >= 54) {
                    break;
                }
                String materialId = entry.getKey();
                Integer quantity = entry.getValue();

                ItemStack materialItem = findMMOItem(materialId);
                if (materialItem == null) {
                    continue;
                }
                while (quantity > 64) {
                    materialItem.setAmount(64);
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
