package org.desp.upgrade.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.desp.upgrade.dto.UpgradeData;
import org.jetbrains.annotations.NotNull;

public class UpgradeFailandDistroyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private UpgradeData upgradeData;
    private ItemStack destroyItem;
    private Player player;
    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public UpgradeFailandDistroyEvent(UpgradeData upgradeData, ItemStack destroyItem, Player player) {
        this.upgradeData = upgradeData;
        this.destroyItem = destroyItem;
        this.player = player;
    }

    public ItemStack getDestroyItem() {
        return destroyItem;
    }

    public void setDestroyItem(ItemStack destroyItem) {
        this.destroyItem = destroyItem;
    }

    public UpgradeData getUpgradeData() {
        return upgradeData;
    }

    public void setUpgradeData(UpgradeData upgradeData) {
        this.upgradeData = upgradeData;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
