package com.customhearts;

import com.customhearts.commands.*;
import com.customhearts.listeners.*;
import com.customhearts.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomHeartsPlugin extends JavaPlugin {

    private static CustomHeartsPlugin instance;
    private HeartManager heartManager;
    private PlayerDataManager playerDataManager;
    private UnlockManager unlockManager;
    private LivesManager livesManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        heartManager = new HeartManager(this);
        playerDataManager = new PlayerDataManager(this);
        unlockManager = new UnlockManager(this);
        livesManager = new LivesManager(this);

        heartManager.loadHearts();
        playerDataManager.loadAll();

        // Listeners
        getServer().getPluginManager().registerEvents(new HeartAbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new UnlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new LivesListener(this), this);
        getServer().getPluginManager().registerEvents(new GrowingHeartListener(this), this);
        getServer().getPluginManager().registerEvents(new AllSeeingHeartListener(this), this);
        getServer().getPluginManager().registerEvents(new MoltenHeartListener(this), this);
        getServer().getPluginManager().registerEvents(new RottenHeartListener(this), this);
        getServer().getPluginManager().registerEvents(new GoblinHeartListener(this), this);

        // Commands
        HeartCommand hc = new HeartCommand(this);
        getCommand("heart").setExecutor(hc);
        getCommand("heart").setTabCompleter(hc);

        LivesCommand lc = new LivesCommand(this);
        getCommand("lives").setExecutor(lc);
        getCommand("lives").setTabCompleter(lc);

        getCommand("growlock").setExecutor(new GrowLockCommand(this));
        getCommand("growunlock").setExecutor(new GrowLockCommand(this));

        getLogger().info("CustomHearts v2 enabled!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) playerDataManager.saveAll();
    }

    public static CustomHeartsPlugin getInstance() { return instance; }
    public HeartManager getHeartManager() { return heartManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public UnlockManager getUnlockManager() { return unlockManager; }
    public LivesManager getLivesManager() { return livesManager; }
}
