package org.desp.upgrade.utils;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.desp.upgrade.ui.UpgradeUI;

public class Validator {

    public static boolean isInvalidClick(InventoryClickEvent e) {
        return e.getClickedInventory() == null || !(e.getInventory().getHolder() instanceof UpgradeUI);
    }
    public static boolean isPlayerInventory(InventoryClickEvent e) {
        return e.getClickedInventory().getType().equals(InventoryType.PLAYER);
    }
}
