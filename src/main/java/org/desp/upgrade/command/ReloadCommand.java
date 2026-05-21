package org.desp.upgrade.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.desp.upgrade.Upgrade;
import org.desp.upgrade.utils.UpgradeEventManager;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!commandSender.isOp()) {
            return false;
        }
        if (strings.length >= 1 && strings[0].equals("리로드")) {
            Upgrade.register();
            commandSender.sendMessage("§a강화 데이터가 리로드되었습니다.");
            return true;
        }
        if (strings.length >= 2 && strings[0].equals("이벤트")) {
            int boost;
            try {
                boost = Integer.parseInt(strings[1]);
            } catch (NumberFormatException ex) {
                commandSender.sendMessage("§c수치는 숫자로 입력해주세요.");
                return true;
            }
            if (boost <= 0) {
                commandSender.sendMessage("§c수치는 1 이상의 숫자로 입력해주세요.");
                return true;
            }
            UpgradeEventManager.start(boost);
            Bukkit.broadcastMessage("§6§l[강화 이벤트] §r§e강화 성공 확률 §a+" + boost + "%§e 이벤트가 시작되었습니다! §7(다음날 자정까지)");
            return true;
        }
        commandSender.sendMessage("§c사용법: /강화관리 리로드");
        commandSender.sendMessage("§c사용법: /강화관리 이벤트 <수치>");
        return true;
    }
}
