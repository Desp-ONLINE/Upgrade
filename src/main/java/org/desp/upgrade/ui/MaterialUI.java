package org.desp.upgrade.ui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MaterialUI implements InventoryHolder {

    public Inventory inventory;
    private final String beforeWeaponID;
    public MaterialUI(String beforeWeaponID) {
        this.beforeWeaponID = beforeWeaponID;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if(this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, 54, "강화재료설정-"+beforeWeaponID);
        }
        return inventory;
    }
}
