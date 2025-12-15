package org.desp.upgrade.ui;

import com.binggre.binggreapi.functions.HolderListener;
import com.binggre.binggreapi.utils.InventoryManager;
import com.binggre.binggreapi.utils.ItemManager;
import com.binggre.mmomail.MMOMail;
import com.binggre.mmomail.api.MailAPI;
import com.binggre.mmomail.objects.Mail;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RollbackUI implements InventoryHolder, HolderListener {

    public Inventory inventory;
    private final ItemStack rollbackItemID;
    private final List<ItemStack> materials;

    public RollbackUI(Player player, ItemStack rollbackItemID, List<ItemStack> materials) {
        this.rollbackItemID = rollbackItemID;
        this.materials = materials;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, 27, "무기 다운그레이드");
            ItemStack itemStack1 = ItemManager.create(Material.PAPER, "§a이 아이템을 롤백하겠습니다.");
            ItemManager.setCustomModelData(itemStack1, 10250);

            ItemStack itemStack2 = ItemManager.create(Material.PAPER, "§c이 아이템을 롤백하지 않겠습니다.");
            ItemManager.setCustomModelData(itemStack2, 10249);

            inventory.setItem(11, itemStack1);
            inventory.setItem(15, itemStack2);

            List<String> strings = new ArrayList<>(List.of("", "§e§n                     돌려 받을 수 있는 아이템§e§n                     ", ""));


            for (ItemStack material : materials) {
                strings.add(ItemManager.getDisplayName(material) + " §fx" + material.getAmount());
            }
            ItemManager.setLore(rollbackItemID, strings);
            inventory.setItem(13, rollbackItemID);
        }
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) {
            return;
        }

        e.setCancelled(true);

        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        switch (e.getSlot()) {
            case 11 -> //롤백
                    rollbackItem(player, materials);
            case 15 -> // 취소
                    player.closeInventory();
        }

    }

    @Override
    public void onClose(InventoryCloseEvent e) {
    }

    @Override
    public void onDrag(InventoryDragEvent inventoryDragEvent) {

    }

    public void rollbackItem(Player player, List<ItemStack> materials) {
        boolean isPlayerHasTicket = false;


        ItemStack[] contents = player.getInventory().getStorageContents();
        for (ItemStack content : contents) {
            String id = MMOItems.getID(content);
            if (id == null) continue;
            if (id.equals("기타_다운그레이드권")) {
                content.setAmount(content.getAmount() - 1);
                isPlayerHasTicket = true;
                break;
            }
        }
        if (!isPlayerHasTicket) {
            player.sendMessage("§c 아이템 강화 다운그레이드권이 없습니다! 강화의 전당 투발카인에게서 구매하실 수 있습니다.");
            player.closeInventory();
            return;
        }
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        String id = MMOItems.getID(itemInMainHand);
        String substring = id.substring(0, id.length() - 1);
        substring += "0";

        ItemStack item = MMOItems.plugin.getItem("SWORD", substring);
        materials.add(item);

        MailAPI mailAPI = MMOMail.getInstance().getMailAPI();
        Mail mail = mailAPI.createMail("시스템", "무기 다운그레이드 아이템을 지급해드립니다.", 0, materials);
        mailAPI.sendMail(player.getName(), mail);
        itemInMainHand.setAmount(0);
        player.closeInventory();
        player.sendMessage("§a 아이템 강화 등급 다운그레이드가 완료 되었습니다. §7§o(/메일함)");

        System.out.println("다운그레이드: " + player.getName() + "::" + substring);
    }
}
