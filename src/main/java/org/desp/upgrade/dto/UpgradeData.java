package org.desp.upgrade.dto;

import java.util.List;
import java.util.Map;

public class UpgradeData {

    private String beforeWeapon;
    private String afterWeapon;
    private int cost;
    private int successPercentage;
    private int destructionPercentage;
    private int level;
    private int proceedQuest;
    private List<Map<String, Integer>> materials;

    public int getCost() {
        return cost;
    }

    public String getBeforeWeapon() {
        return beforeWeapon;
    }

    public String getAfterWeapon() {
        return afterWeapon;
    }

    public int getSuccessPercentage() {
        return successPercentage;
    }

    public int getDestructionPercentage() {
        return destructionPercentage;
    }

    public int getLevel() {
        return level;
    }

    public int getProceedQuest() {
        return proceedQuest;
    }

    public List<Map<String, Integer>> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Map<String, Integer>> materials) {
        this.materials = materials;
    }

    public static UpgradeData create(String beforeWeapon, String afterWeapon, int cost, int successPercentage, int destructionPercentage, int level, int proceedQuest, List<Map<String, Integer>> material) {
        UpgradeData data = new UpgradeData();
        data.beforeWeapon = beforeWeapon;
        data.afterWeapon = afterWeapon;
        data.cost = cost;
        data.successPercentage = successPercentage;
        data.destructionPercentage = destructionPercentage;
        data.level = level;
        data.proceedQuest = proceedQuest;
        data.materials = material;
        return data;
    }


}
