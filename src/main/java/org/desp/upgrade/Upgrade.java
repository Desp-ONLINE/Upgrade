package org.desp.upgrade;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;
import org.desp.upgrade.command.MaterialCommand;
import org.desp.upgrade.command.UpgradeCommand;
import org.desp.upgrade.database.Repository;
import org.desp.upgrade.dto.UpgradeData;
import org.desp.upgrade.listener.UpgradeListener;

public class Upgrade extends JavaPlugin {

    private static Map<String, UpgradeData> allWeaponData = new HashMap<>();
    private static Repository dbRepository;
    private static Upgrade instance;

    public static Upgrade getInstance() {
        return instance;
    }

    public static Map<String, UpgradeData> getAllWeaponData() {
        return allWeaponData;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.dbRepository = new Repository();
        allWeaponData = dbRepository.getAllWeaponData();

        this.getServer().getPluginManager().registerEvents(new UpgradeListener(), this);
        getCommand("reinforce").setExecutor(new UpgradeCommand());
        getCommand("강화재료설정").setExecutor(new MaterialCommand());
    }

    public static void register() {
        allWeaponData = dbRepository.getAllWeaponData();
    }

    @Override
    public void onDisable() {
    }
}
