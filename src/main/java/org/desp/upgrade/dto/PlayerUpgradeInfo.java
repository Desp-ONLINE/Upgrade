package org.desp.upgrade.dto;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class PlayerUpgradeInfo {
    private final UUID playerId;
    private String itemName;
    private ItemStack currentItem;
    private List<Map<String, Integer>> materials;

    // 미리보기(다음 단계 확인) 관련 상태
    // rootItemName / rootItem : 플레이어가 인벤토리에서 실제로 선택한 아이템
    // previewHistory : 미리보기로 넘어온 경로 (뒤로가기용). 비어있으면 실제 강화 가능 상태
    private String rootItemName;
    private ItemStack rootItem;
    private final Deque<String> previewHistory = new ArrayDeque<>();

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

    public String getRootItemName() {
        return rootItemName;
    }

    public ItemStack getRootItem() {
        return rootItem;
    }

    /** 인벤토리에서 실제 아이템을 선택했을 때 호출. 미리보기 기록을 초기화한다. */
    public void setRoot(String itemName, ItemStack item) {
        this.rootItemName = itemName;
        this.rootItem = item;
        this.previewHistory.clear();
    }

    public Deque<String> getPreviewHistory() {
        return previewHistory;
    }

    /** 미리보기 상태인지 (실제 강화 불가) */
    public boolean isPreview() {
        return !previewHistory.isEmpty();
    }

    public void clear() {
        this.itemName = null;
        this.currentItem = null;
        this.materials = null;
        this.rootItemName = null;
        this.rootItem = null;
        this.previewHistory.clear();
    }
}
