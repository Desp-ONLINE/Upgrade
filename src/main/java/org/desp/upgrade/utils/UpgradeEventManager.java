package org.desp.upgrade.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UpgradeEventManager {

    private static int boostPercentage = 0;
    private static long expiryMillis = 0L;

    public static void start(int boost) {
        boostPercentage = boost;
        LocalDateTime nextMidnight = LocalDate.now().plusDays(1).atStartOfDay();
        expiryMillis = nextMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static int getBoost() {
        if (boostPercentage <= 0) {
            return 0;
        }
        if (System.currentTimeMillis() >= expiryMillis) {
            boostPercentage = 0;
            expiryMillis = 0L;
            return 0;
        }
        return boostPercentage;
    }

    public static boolean isActive() {
        return getBoost() > 0;
    }
}
