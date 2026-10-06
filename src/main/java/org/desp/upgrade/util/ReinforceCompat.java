package org.desp.upgrade.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * Reinforce(초월 강화) 브릿지.
 * Reinforce 클래스는 내부 클래스 안에서만 참조하므로,
 * 해당 플러그인이 서버에 없어도 NoClassDefFoundError 없이 동작한다.
 */
public final class ReinforceCompat {

    private static final String REINFORCE = "Reinforce";

    private ReinforceCompat() {
    }

    /**
     * 강화 전 아이템의 초월 강화 기록(횟수, 옵션, 회차별 기록)을 강화 후 아이템으로 옮긴다.
     * @return 기록을 옮긴 아이템. 옮길 기록이 없거나 Reinforce 가 없으면 강화 후 아이템 그대로
     */
    public static ItemStack transfer(ItemStack before, ItemStack after) {
        if (before == null || after == null || !Bukkit.getPluginManager().isPluginEnabled(REINFORCE)) {
            return after;
        }
        try {
            ItemStack result = Api.transfer(before, after);
            return result != null ? result : after;
        } catch (RuntimeException | LinkageError e) {
            Bukkit.getLogger().warning("[Upgrade] 초월 강화 기록을 옮기지 못했습니다: " + e);
            return after;
        }
    }

    // ---------------------------------------------------------------- Reinforce

    private static final class Api {

        static ItemStack transfer(ItemStack before, ItemStack after) {
            return com.binggre.reinforce.ReinforceAPI.transfer(before, after);
        }
    }
}
