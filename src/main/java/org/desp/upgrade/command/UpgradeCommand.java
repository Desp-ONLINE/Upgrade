package org.desp.upgrade.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.desp.upgrade.ui.UpgradeUI;
import org.jetbrains.annotations.NotNull;

public class UpgradeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        if(!player.isOp()){
            return false;
        }
        UpgradeUI upgradeUI = new UpgradeUI();
        player.openInventory(upgradeUI.getInventory());
        return false;
    }
}
