package org.desp.upgrade.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * IDEQuest 퀘스트 조회 브릿지.
 * IDEQuest 클래스는 내부 클래스 안에서만 참조하므로,
 * 해당 플러그인이 서버에 없어도 NoClassDefFoundError 없이 동작한다.
 */
public final class QuestCompat {

    private static final String IDE_QUEST = "IDEQuest";

    private QuestCompat() {
    }

    public static boolean isIDEQuestEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(IDE_QUEST) && Ide.loaded();
    }

    /** 해당 id의 퀘스트가 IDEQuest에 정의되어 있는지. */
    public static boolean questExists(int questId) {
        return isIDEQuestEnabled() && Ide.exists(questId);
    }

    /** 퀘스트 이름. 없으면 null. */
    public static String questName(int questId) {
        if (isIDEQuestEnabled()) {
            return Ide.name(questId);
        }
        return null;
    }

    /** 퀘스트를 시작한 적이 있는지 (진행 중이거나 완료 이력 포함). */
    public static boolean hasQuestData(Player player, int questId) {
        return isIDEQuestEnabled() && Ide.hasQuestData(player, questId);
    }

    /** 1회 이상 완료했는지. */
    public static boolean hasFinished(Player player, int questId) {
        return isIDEQuestEnabled() && Ide.hasFinished(player, questId);
    }

    /** 현재 진행 중인지. */
    public static boolean isActive(Player player, int questId) {
        return isIDEQuestEnabled() && Ide.isActive(player, questId);
    }

    /** 시작했거나 완료했는지 (isActive || hasFinished). */
    public static boolean hasStartedOrFinished(Player player, int questId) {
        return isActive(player, questId) || hasFinished(player, questId);
    }

    // ---------------------------------------------------------------- IDEQuest

    private static final class Ide {

        static boolean loaded() {
            return org.example.dople.iDEQuest.api.IDEQuestAPI.isLoaded();
        }

        static boolean exists(int questId) {
            return org.example.dople.iDEQuest.api.IDEQuestAPI.quest(questId) != null;
        }

        static String name(int questId) {
            org.example.dople.iDEQuest.quest.Quest quest = org.example.dople.iDEQuest.api.IDEQuestAPI.quest(questId);
            return quest != null ? quest.name() : null;
        }

        static boolean hasQuestData(Player player, int questId) {
            return org.example.dople.iDEQuest.api.IDEQuestAPI.hasQuestData(player.getUniqueId(), questId);
        }

        static boolean hasFinished(Player player, int questId) {
            return org.example.dople.iDEQuest.api.IDEQuestAPI.hasFinished(player.getUniqueId(), questId);
        }

        static boolean isActive(Player player, int questId) {
            return org.example.dople.iDEQuest.api.IDEQuestAPI.isActive(player.getUniqueId(), questId);
        }
    }
}
