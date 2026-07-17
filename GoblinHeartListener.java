package com.customhearts.managers;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class HeartManager {

    private final CustomHeartsPlugin plugin;
    public final NamespacedKey HEART_KEY;

    public HeartManager(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        HEART_KEY = new NamespacedKey(plugin, "heart_type");
    }

    public void loadHearts() {
        plugin.getLogger().info("Heart definitions loaded from config.yml.");
    }

    /** Get display name from config, fallback to type name */
    public String getDisplayName(HeartType type) {
        return plugin.getConfig().getString("hearts." + type.name() + ".display-name",
                "&f" + type.name());
    }

    /** Get material from config */
    public Material getMaterial(HeartType type) {
        String mat = plugin.getConfig().getString("hearts." + type.name() + ".material", "NETHER_STAR");
        Material m = Material.matchMaterial(mat);
        return m != null ? m : Material.NETHER_STAR;
    }

    /** Get lore from config */
    public List<String> getLore(HeartType type) {
        return plugin.getConfig().getStringList("hearts." + type.name() + ".lore");
    }

    /** Build item stack for a heart type */
    public ItemStack createHeartItem(HeartType type) {
        ItemStack item = new ItemStack(getMaterial(type));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(c(getDisplayName(type)));

        List<Component> loreList = new ArrayList<>();
        for (String line : getLore(type)) loreList.add(c(line));
        meta.lore(loreList);
        meta.getPersistentDataContainer().set(HEART_KEY, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    /** Get heart type from item PDC */
    public HeartType getHeartType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String val = meta.getPersistentDataContainer().get(HEART_KEY, PersistentDataType.STRING);
        if (val == null) return null;
        try { return HeartType.valueOf(val); } catch (IllegalArgumentException e) { return null; }
    }

    /** Helper to get a cooldown from config in seconds, converted to ms */
    public long getCooldownMs(HeartType type, String key) {
        int seconds = plugin.getConfig().getInt("hearts." + type.name() + ".cooldowns." + key, 0);
        return seconds * 1000L;
    }

    /** Helper to get a threshold value */
    public int getThreshold(HeartType type, String key) {
        return plugin.getConfig().getInt("hearts." + type.name() + ".thresholds." + key, 20);
    }

    /** Helper to get a misc value as int */
    public int getMiscInt(HeartType type, String key, int def) {
        return plugin.getConfig().getInt("hearts." + type.name() + ".misc." + key, def);
    }

    /** Helper to get a misc value as double */
    public double getMiscDouble(HeartType type, String key, double def) {
        return plugin.getConfig().getDouble("hearts." + type.name() + ".misc." + key, def);
    }

    public Set<HeartType> getAllTypes() { return Set.of(HeartType.values()); }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
