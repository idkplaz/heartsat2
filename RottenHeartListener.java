package com.customhearts.managers;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks how many of each heart type have been obtained server-wide.
 * Once a heart reaches its server-limit it can never be earned again
 * (admins can still force-give via /heart give --force).
 */
public class LimitManager {

    private final CustomHeartsPlugin plugin;
    private final File dataFile;
    private final Map<HeartType, Integer> obtained = new EnumMap<>(HeartType.class);

    public LimitManager(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "limits.yml");
    }

    public void load() {
        obtained.clear();
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        for (HeartType type : HeartType.values()) {
            obtained.put(type, cfg.getInt(type.name(), 0));
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        obtained.forEach((type, count) -> cfg.set(type.name(), count));
        try { cfg.save(dataFile); } catch (IOException e) {
            plugin.getLogger().warning("Failed to save limits.yml: " + e.getMessage());
        }
    }

    /**
     * Returns true if this heart type can still be obtained (limit not reached).
     */
    public boolean canObtain(HeartType type) {
        int limit = getLimit(type);
        if (limit == -1) return true;
        return getObtained(type) < limit;
    }

    /**
     * Call when a heart is successfully granted to a player.
     * Increments the count and notifies ops if limit is now reached.
     */
    public void recordObtained(HeartType type) {
        int newCount = obtained.getOrDefault(type, 0) + 1;
        obtained.put(type, newCount);
        save();

        int limit = getLimit(type);
        if (limit != -1 && newCount >= limit) {
            String name = plugin.getHeartManager().getDisplayName(type);
            String msg = "&4[CustomHearts] &cThe server limit for " + name
                    + " &c(&f" + limit + "&c) has been reached! "
                    + "No more can be obtained without &f/heart give --force&c.";
            notifyOps(msg);
            plugin.getLogger().warning("Heart limit reached for " + type.name()
                    + " (" + newCount + "/" + limit + ")");
        }
    }

    /**
     * Force-remove one from the count (e.g. if admin removes a heart).
     */
    public void recordRemoved(HeartType type) {
        int current = obtained.getOrDefault(type, 0);
        obtained.put(type, Math.max(0, current - 1));
        save();
    }

    public int getObtained(HeartType type) {
        return obtained.getOrDefault(type, 0);
    }

    public int getLimit(HeartType type) {
        return plugin.getConfig().getInt("hearts." + type.name() + ".server-limit", -1);
    }

    private void notifyOps(String message) {
        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) p.sendMessage(comp);
        }
        // Also log to console
        plugin.getLogger().info(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(message).toString());
    }
}
