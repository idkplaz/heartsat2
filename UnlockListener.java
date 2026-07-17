package com.customhearts.managers;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private final CustomHeartsPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public void loadAll() {}

    public PlayerData load(UUID uuid) {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        PlayerData data = new PlayerData(uuid);
        File file = new File(dataFolder, uuid + ".yml");
        if (file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            data.setLives(cfg.getInt("lives", plugin.getConfig().getInt("settings.default-lives", 3)));
            int maxSlots = plugin.getConfig().getInt("settings.max-heart-slots", 3);
            cfg.getStringList("equipped").forEach(s -> {
                try { data.equipHeart(HeartType.valueOf(s), maxSlots); } catch (Exception ignored) {}
            });
            cfg.getStringList("unlocked").forEach(s -> {
                try { data.unlockHeart(HeartType.valueOf(s)); } catch (Exception ignored) {}
            });
            var mob = cfg.getConfigurationSection("mob-kills");
            if (mob != null) mob.getKeys(false).forEach(k -> data.getMobKillMap().put(k, mob.getInt(k)));
            var mine = cfg.getConfigurationSection("blocks-mined");
            if (mine != null) mine.getKeys(false).forEach(k -> data.getBlocksMinedMap().put(k, mine.getInt(k)));
            data.setPlayerKills(cfg.getInt("player-kills", 0));
            data.setBlocksSwum(cfg.getLong("blocks-swum", 0));
            data.setOresMined(cfg.getInt("ores-mined", 0));
            data.setIceMined(cfg.getInt("ice-mined", 0));
            data.getSeedsPlanted().addAll(cfg.getStringList("seeds-planted"));
            data.setGrowthLevel(cfg.getInt("growth-level", 0));
            data.setGrowLocked(cfg.getBoolean("grow-locked", false));
        } else {
            data.setLives(plugin.getConfig().getInt("settings.default-lives", 3));
        }
        cache.put(uuid, data);
        return data;
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;
        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("lives", data.getLives());
        List<String> eq = new ArrayList<>(); data.getEquippedHearts().forEach(h -> eq.add(h.name())); cfg.set("equipped", eq);
        List<String> ul = new ArrayList<>(); data.getUnlockedHearts().forEach(h -> ul.add(h.name())); cfg.set("unlocked", ul);
        data.getMobKillMap().forEach((k,v) -> cfg.set("mob-kills."+k, v));
        data.getBlocksMinedMap().forEach((k,v) -> cfg.set("blocks-mined."+k, v));
        cfg.set("player-kills", data.getPlayerKills());
        cfg.set("blocks-swum", data.getBlocksSwum());
        cfg.set("ores-mined", data.getOresMined());
        cfg.set("ice-mined", data.getIceMined());
        cfg.set("seeds-planted", new ArrayList<>(data.getSeedsPlanted()));
        cfg.set("growth-level", data.getGrowthLevel());
        cfg.set("grow-locked", data.isGrowLocked());
        try { cfg.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for " + uuid);
        }
    }

    public void saveAll() { cache.keySet().forEach(this::save); }
    public PlayerData get(UUID uuid) { return cache.computeIfAbsent(uuid, this::load); }
    public void unload(UUID uuid) { save(uuid); cache.remove(uuid); }
}
