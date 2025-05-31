package org.desp.upgrade.listener;

import com.binggre.binggreEconomy.BinggreEconomy;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.quests.Quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
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
import org.desp.upgrade.utils.UpgradeButtonSlot;
import org.desp.upgrade.utils.UpgradeResult;
import org.desp.upgrade.utils.UpgradeUtil;
import org.desp.upgrade.utils.Validator;
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
                processUpgrade(e, weaponData, itemName, session);
            } else {
                player.sendMessage("§c 강화에 필요한 레벨에 도달하지 못했습니다");
            }
        }
    }

    private void processUpgrade(InventoryClickEvent e, UpgradeData weaponData, String itemName, PlayerUpgradeInfo session) {
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

        if (result == UpgradeResult.SUCCESS) {
            Bukkit.getPluginManager().callEvent(new UpgradeSuccessEvent(weaponData, player));
            player.sendMessage("§a 강화에 성공하였습니다!");

            BinggreEconomy.getInst().getEconomy().withdrawPlayer(player, upgradeCost);

            player.getInventory().removeItem(session.getCurrentItem());

            ItemStack upgradedItem = null;
            for (Type type : MMOItems.plugin.getTypes().getAll()) {
                if (MMOItems.plugin.getItem(type, weaponData.getAfterWeapon()) != null) {
                    upgradedItem = MMOItems.plugin.getItem(type, weaponData.getAfterWeapon());
                    break;
                }
            }
            
            player.getInventory().addItem(upgradedItem);

            player.closeInventory();
        } else if (result == UpgradeResult.FAIL) {
            Bukkit.getPluginManager().callEvent(new UpgradeFailEvent(weaponData, player));
            player.sendMessage("§c 강화에 실패하였습니다");
        } else if (result == UpgradeResult.DESTRUCTION) {
            Bukkit.getPluginManager().callEvent(new UpgradeFailandDistroyEvent(weaponData, session.getCurrentItem(), player));
            player.sendMessage("§4 강화에 실패하여 무기가 사라졌습니다");
            player.getInventory().removeItem(session.getCurrentItem());
            player.closeInventory();
        }

        removeRequiredMaterials(player, requiredMaterials);
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

    private void removeRequiredMaterials(Player player, List<Map<String, Integer>> requiredMaterials) {
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
        session.setCurrentItem(currentItem);
        session.setItemName(itemName);

        ItemRender.renderAfterWeapon(currentItem, itemName, e, player);

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
