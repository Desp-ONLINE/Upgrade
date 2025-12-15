package org.desp.upgrade.listener;

import com.binggre.binggreEconomy.BinggreEconomy;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.quests.Quest;

import java.util.*;
import java.util.Map.Entry;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.desp.upgrade.ui.MaterialUI;
import org.desp.upgrade.ui.UpgradeUI;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.database.Repository;
import org.desp.upgrade.dto.PlayerUpgradeInfo;
import org.desp.upgrade.dto.UpgradeData;
import org.desp.upgrade.event.UpgradeFailEvent;
import org.desp.upgrade.event.UpgradeFailandDistroyEvent;
import org.desp.upgrade.event.UpgradeSuccessEvent;
import org.desp.upgrade.event.UpgradeTryEvent;
import org.desp.upgrade.utils.*;
import org.desp.upgrade.view.ItemRender;

public class UpgradeListener implements Listener {

    private final Map<UUID, PlayerUpgradeInfo> playerSessions = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (Validator.isInvalidClick(e)) {
            return;
        }
        e.setCancelled(true);

        if (Validator.isPlayerInventory(e)) {
            handlePlayerInventoryClick(e);
        } else {
            handleUpgradeClick(e);
        }
    }

    private void handleUpgradeClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        PlayerUpgradeInfo session = playerSessions.get(player.getUniqueId());
        if (session == null || session.getItemName() == null) {
            return;
        }

        String itemName = session.getItemName();
        UpgradeData weaponData = Upgrade.getAllWeaponData().get(itemName);
        if (weaponData == null) {
            return;
        }

        int playerLevel = UpgradeUtil.getPlayerLevel(e);
        int weaponLevel = weaponData.getLevel();

        if (e.getSlot() == UpgradeButtonSlot.SLOT) {
            if (weaponLevel <= playerLevel) {
                if (e.getInventory().getItem(UpgradeButtonSlot.PROTECT_SLOT).getItemMeta().getCustomModelData() == ProtectItemCode.PROTECT_FALSE) {
                    processUpgrade(e, weaponData, itemName, session, false);

                } else {
                    processUpgrade(e, weaponData, itemName, session, true);

                }
            } else {
                player.sendMessage("§c 강화에 필요한 레벨에 도달하지 못했습니다");
            }
        }
        if (e.getSlot() == UpgradeButtonSlot.PROTECT_SLOT) {
            if (e.getCurrentItem().getItemMeta().getCustomModelData() == ProtectItemCode.PROTECT_FALSE) {
                ItemStack protectLight = MMOItems.plugin.getItem("MISCELLANEOUS", "기타_광휘의빛");
                boolean isLightExist = false;
                for (ItemStack itemStack : player.getInventory()) {
                    if (MMOItems.getID(itemStack).equals("기타_광휘의빛")) {
                        isLightExist = true;
                        break;
                    }
                }
                if (!isLightExist) {
                    player.sendMessage("§c 광휘의 빛이 인벤토리에 없습니다. 확인 해주세요.");
                    return;
                }
                ItemStack protectItem = ItemRender.getProtectItem(true);
                e.setCurrentItem(protectItem);
            } else if (e.getCurrentItem().getItemMeta().getCustomModelData() == ProtectItemCode.PROTECT_TRUE) {
                ItemStack protectItem = ItemRender.getProtectItem(false);
                e.setCurrentItem(protectItem);
            }
        }

    }

    private void processUpgrade(InventoryClickEvent e, UpgradeData weaponData, String itemName, PlayerUpgradeInfo session, boolean isProtected) {
        Player player = (Player) e.getWhoClicked();
        double balance = BinggreEconomy.getInst().getEconomy().getBalance(player);
        int upgradeCost = weaponData.getCost();

        if (upgradeCost > balance) {
            player.sendMessage("§c 강화에 필요한 금액이 부족합니다");
            return;
        }

        ItemStack[] contents = player.getInventory().getContents();

        List<Map<String, Integer>> requiredMaterials = session.getMaterials();

        if (!hasRequiredMaterials(contents, requiredMaterials)) {
            player.sendMessage("§c 강화재료가 부족합니다");
            return;
        }

        int proceedQuest = weaponData.getProceedQuest();
        Quest quest = BeautyQuests.getInstance().getAPI().getQuestsManager().getQuest(proceedQuest);
        boolean isSatisfiedQuest = BeautyQuests.getInstance().getPlayersManager().getAccount(player).hasQuestDatas(quest);

        if (!isSatisfiedQuest) {
            player.sendMessage("§c 최소 퀘스트를 진행하지 않았습니다");
            return;
        }

        UpgradeResult result = UpgradeUtil.getResult(itemName);

        Bukkit.getPluginManager().callEvent(new UpgradeTryEvent(weaponData, player));

        BinggreEconomy.getInst().getEconomy().withdrawPlayer(player, upgradeCost);
        if (isProtected) {
            for (ItemStack itemStack : player.getInventory()) {
                if (MMOItems.getID(itemStack).equals("기타_광휘의빛")) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
            }
            ItemStack protectItem = ItemRender.getProtectItem(false);
            e.getInventory().setItem(UpgradeButtonSlot.PROTECT_SLOT, protectItem);
        }
        if (result == UpgradeResult.SUCCESS) {
            Bukkit.getPluginManager().callEvent(new UpgradeSuccessEvent(weaponData, player));
            player.sendMessage("§a 강화에 성공하였습니다!");

            player.getInventory().removeItem(session.getCurrentItem());

            ItemStack upgradedItem = null;
            TypeManager types = MMOItems.plugin.getTypes();
            for (Type type : types.getAll()) {
                if (MMOItems.plugin.getItem(type, weaponData.getAfterWeapon()) == null) {
                    continue;
                } else {
                    upgradedItem = MMOItems.plugin.getItem(type, weaponData.getAfterWeapon());
                }
            }

            player.getInventory().addItem(upgradedItem);

            removeRequiredMaterials(player, requiredMaterials, false);
            session.setCurrentItem(null);
            session.setMaterials(null);
            session.setItemName(null);
            e.getInventory().setItem(10, new ItemStack(Material.AIR));
            e.getInventory().setItem(16, new ItemStack(Material.AIR));
            for (int i = 36; i < 54; i++) {
                e.getInventory().setItem(i, new ItemStack(Material.AIR));
            }

            // 강화 성공 시 상태 그대로 유지
            processUpgrade(e, );

        } else if (result == UpgradeResult.FAIL) {
            Bukkit.getPluginManager().callEvent(new UpgradeFailEvent(weaponData, player));
            if (isProtected) {
                removeRequiredMaterials(player, requiredMaterials, true);
                player.sendMessage("§c 강화에 실패하였습니다. 광휘의 빛으로 재료를 보호했습니다.");
            } else {
                removeRequiredMaterials(player, requiredMaterials, false);
                player.sendMessage("§c 강화에 실패하였습니다.");
            }

        } else if (result == UpgradeResult.DESTRUCTION) {
            Bukkit.getPluginManager().callEvent(new UpgradeFailandDistroyEvent(weaponData, session.getCurrentItem(), player));
            player.sendMessage("§4 강화에 실패하여 무기가 사라졌습니다");
            player.getInventory().removeItem(session.getCurrentItem());
            player.closeInventory();
            removeRequiredMaterials(player, requiredMaterials, false);

        }


    }

    private boolean hasRequiredMaterials(ItemStack[] inventory, List<Map<String, Integer>> requiredMaterials) {
        Map<String, Integer> inventoryMaterials = new HashMap<>();

        for (ItemStack item : inventory) {
            if (item != null && !item.getType().isAir()) {
                String itemId = MMOItems.getID(item);
                if (itemId != null) {
                    inventoryMaterials.put(itemId, inventoryMaterials.getOrDefault(itemId, 0) + item.getAmount());
                }
            }
        }

        for (Map<String, Integer> material : requiredMaterials) {
            Set<Entry<String, Integer>> entries = material.entrySet();
            for (Entry<String, Integer> entry : entries) {
                String requiredId = entry.getKey();
                int requiredQuantity = entry.getValue();

                int playerQuantity = inventoryMaterials.getOrDefault(requiredId, 0);
                if (playerQuantity < requiredQuantity) {
                    return false;
                }

            }
        }
        return true;
    }

    private void removeRequiredMaterials(Player player, List<Map<String, Integer>> requiredMaterials, boolean isProtected) {
        if (isProtected) {
            return;
        }
        for (Map<String, Integer> material : requiredMaterials) {
            material.forEach((requiredId, requiredQuantity) ->
                    requiredQuantity = removeMaterialFromInventory(player, requiredId, requiredQuantity)
            );
        }
    }

    private int removeMaterialFromInventory(Player player, String requiredId, int requiredQuantity) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && MMOItems.getID(item).equals(requiredId)) {
                int amount = item.getAmount();
                if (amount > requiredQuantity) {
                    item.setAmount(amount - requiredQuantity);
                    return 0;
                } else {
                    requiredQuantity -= amount;
                    item.setAmount(0);
                }
            }
        }
        return requiredQuantity;
    }


    private void handlePlayerInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack currentItem = e.getCurrentItem();

        if (currentItem == null || currentItem.getType().isAir()) {
            return;
        }
        String itemName = MMOItems.getID(currentItem);

        UpgradeData weaponData = Upgrade.getAllWeaponData().get(itemName);
        if (weaponData == null) {
            return;
        }

        PlayerUpgradeInfo session = playerSessions.computeIfAbsent(player.getUniqueId(), PlayerUpgradeInfo::new);

        session.setMaterials(weaponData.getMaterials());

        ItemStack oneItem = currentItem.clone();
        oneItem.setAmount(1);
        session.setCurrentItem(oneItem);
        session.setItemName(itemName);

        ItemRender.renderAfterWeapon(oneItem, itemName, e, player);

        UpgradeUtil.setLore(e, weaponData);

        ItemRender.renderMaterialsInventoryClick(itemName, e);
    }

    private void setPlayerSessions(InventoryClickEvent e, Player player, ItemStack currentItem) {

        if (currentItem == null || currentItem.getType().isAir()) {
            return;
        }
        String itemName = MMOItems.getID(currentItem);

        UpgradeData weaponData = Upgrade.getAllWeaponData().get(itemName);
        if (weaponData == null) {
            return;
        }

        PlayerUpgradeInfo session = playerSessions.computeIfAbsent(player.getUniqueId(), PlayerUpgradeInfo::new);

        session.setMaterials(weaponData.getMaterials());

        ItemStack oneItem = currentItem.clone();
        oneItem.setAmount(1);
        session.setCurrentItem(oneItem);
        session.setItemName(itemName);

        ItemRender.renderAfterWeapon(oneItem, itemName, e, player);

        UpgradeUtil.setLore(e, weaponData);

        ItemRender.renderMaterialsInventoryClick(itemName, e);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof UpgradeUI) {
            Player player = (Player) e.getPlayer();
            UUID uuid = player.getUniqueId();
            PlayerUpgradeInfo session = playerSessions.remove(uuid);
            if (session == null) {
                return;
            } else {
                session.setItemName(null);
            }

        }
        if (e.getInventory().getHolder() instanceof MaterialUI) {
            String itemName = e.getView().getTitle().replace("강화재료설정-", "");

            Map<String, Integer> materials = UpgradeUtil.extractMaterials(e.getInventory());

            Repository.updateMaterial(itemName, materials);
        }
    }
}
