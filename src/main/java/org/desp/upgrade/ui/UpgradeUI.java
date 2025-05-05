package org.desp.upgrade.ui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.utils.UpgradeButtonSlot;
import org.jetbrains.annotations.NotNull;

public class UpgradeUI implements InventoryHolder {

    public Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        String reinforceTitle = (String) Upgrade.getInstance().getConfig().get("reinforce_title");
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, 54, reinforceTitle);
            ItemStack item = new ItemStack(Material.GLASS_PANE, 1);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setCustomModelData(1);
            itemMeta.setDisplayName("§a  강화하기");
            item.setItemMeta(itemMeta);

            List<String> defaultLore = new ArrayList<>();
            defaultLore.add("§c    강화할 무기를 선택해주세요.");
            item.setLore(defaultLore);

            inventory.setItem(UpgradeButtonSlot.SLOT, item);
        }
        return inventory;
    }
}
