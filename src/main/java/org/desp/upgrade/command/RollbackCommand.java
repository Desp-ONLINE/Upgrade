package org.desp.upgrade.command;

import com.binggre.binggreapi.utils.ItemManager;
import io.lumine.mythic.lib.skill.handler.def.item.Item_Bomb;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.desp.upgrade.database.Repository;
import org.desp.upgrade.dto.UpgradeData;
import org.desp.upgrade.ui.RollbackUI;
import org.desp.upgrade.ui.UpgradeUI;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RollbackCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if(itemInMainHand.isEmpty()){
            player.sendMessage("§c 다운그레이드 하실 무기를 손에 들고 시도해주세요.");
            return true;
        }

        String id = MMOItems.getID(itemInMainHand);
        int idLength = id.length();
        String weaponID = id.substring(0, idLength - 1);

        if (!(id.startsWith("특수무기_") || id.startsWith("합성무기_"))) {
            player.sendMessage("§c 이 아이템은 강화 단계를 다운그레이드할 수 없습니다.");
            return true;
        }
        int upgradeLevel = Integer.parseInt(String.valueOf(id.charAt(idLength - 1)));
        if ((upgradeLevel == 0 || upgradeLevel == 5)) {
            player.sendMessage("§c 1~4단계 아이템만 다운그레이드할 수 있습니다.");
            return true;
        }

        List<ItemStack> materialList = new ArrayList<>();


        for (int i = upgradeLevel - 1; i >= 0; i--) {
            String newID = weaponID + i;
            UpgradeData upgradeData = Repository.weaponRepository.get(newID);
            System.out.println("newID = " + newID);
            List<Map<String, Integer>> materials = upgradeData.getMaterials();

            ItemStack materialItem = new ItemStack(Material.PAPER);

            for (Map<String, Integer> material : materials) {
                for (String materialID : material.keySet()) {
                    if (MMOItems.plugin.getItem(Type.MISCELLANEOUS, materialID) == null) {
                        materialItem = MMOItems.plugin.getItem(Type.SWORD, materialID);
                        materialItem.setAmount(material.get(materialID));
                    } else {
                        materialItem = MMOItems.plugin.getItem(Type.MISCELLANEOUS, materialID);
                        materialItem.setAmount(material.get(materialID));
                    }
                }
                materialList.add(materialItem);

            }
        }
        merge(materialList);


        ItemStack targetItem = MMOItems.plugin.getItem("SWORD", id);
        RollbackUI rollbackUI = new RollbackUI(player, targetItem, materialList);
        player.openInventory(rollbackUI.getInventory());
        return false;
    }

    private void merge(List<ItemStack> materialList) {
        Map<String, ItemObj> amounts = new HashMap<>();

        for (ItemStack itemStack : materialList) {
            String name = ItemManager.getDisplayName(itemStack);
            ItemObj obj = amounts.getOrDefault(name, new ItemObj(itemStack));
            obj.name = name;
            obj.amount += itemStack.getAmount();
            amounts.put(obj.name, obj);
        }
        materialList.clear();
        amounts.forEach((s, itemObj) -> {
            itemObj.itemStack.setAmount(itemObj.amount);
            materialList.add(itemObj.itemStack);
        });
    }

    private static class ItemObj {

        private final ItemStack itemStack;
        private String name;
        private int amount;

        ItemObj(ItemStack itemStack) {
            this.itemStack = itemStack;
        }
    }
}
