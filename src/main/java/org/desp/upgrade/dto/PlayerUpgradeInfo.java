package org.desp.upgrade.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class PlayerUpgradeInfo {
    private final UUID playerId;
    private String itemName;
    private ItemStack currentItem;
    private List<Map<String, Integer>> materials;

    public List<Map<String, Integer>> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Map<String, Integer>> materials) {
        this.materials = materials;
    }

    public PlayerUpgradeInfo(UUID playerId) {
        this.playerId = playerId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public UUID getPlayerId() {
        return playerId;

    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
    }
}