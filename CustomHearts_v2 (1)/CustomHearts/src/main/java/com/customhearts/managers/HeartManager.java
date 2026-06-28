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

    // Static definition of all hearts: type -> [displayName, material, lore...]
    private static final Map<HeartType, Object[]> HEART_DEFS = new LinkedHashMap<>();

    static {
        def(HeartType.HARDCORE,   "&c&lHardcore Heart",   Material.NETHER_STAR,
                "&7Grants you an extra life.", "&eCrafting Recipe");
        def(HeartType.ALLSEEING,  "&d&lAll-Seeing Heart", Material.ENDER_EYE,
                "&7All entities within 45 blocks glow.", "&eCrafting Recipe");
        def(HeartType.ENDERMAN,   "&5&lEnderman Heart",   Material.ENDER_PEARL,
                "&7Right-click with sword/axe/pick to teleport 15 blocks.",
                "&eKill 100 Endermen & 10 Endermites");
        def(HeartType.CLOUD,      "&b&lCloud Heart",      Material.FEATHER,
                "&7Right-click with sword/axe/pick to wind-dash upward.",
                "&7Permanent Slow Falling.", "&eKill 45 Breezes");
        def(HeartType.EMERALD,    "&a&lEmerald Heart",    Material.EMERALD,
                "&7Permanent Hero of the Village V.", "&eCommand Only");
        def(HeartType.FROZEN,     "&3&lFrozen Heart",     Material.PACKED_ICE,
                "&7Applies powder snow effect on hit. (45s cooldown)",
                "&eMine 1000 Ice");
        def(HeartType.GOBLIN,     "&2&lGoblin Heart",     Material.IRON_PICKAXE,
                "&7Haste II when holding a pickaxe or shovel.",
                "&eMine 7500 Stone & 5250 Deepslate");
        def(HeartType.BLAZE,      "&6&lBlaze Heart",      Material.BLAZE_ROD,
                "&7Permanent Fire Resistance.", "&7Hits set enemies on fire (Fire Aspect III).",
                "&eKill 100 Blazes");
        def(HeartType.OCEAN,      "&1&lOcean Heart",      Material.HEART_OF_THE_SEA,
                "&7Permanent Dolphin's Grace II & Conduit Power.",
                "&eKill 3 Elder Guardians & Swim 10,000 blocks");
        def(HeartType.SOUL,       "&8&lSoul Heart",       Material.SOUL_SAND,
                "&7Permanent Invisibility.",
                "&eAdvancement: Return to Sender");
        def(HeartType.ABSOLUTE,   "&f&lAbsolute Heart",   Material.BEACON,
                "&72 lives left: +3 hearts. 1 life left: 16 total hearts.",
                "&eReach 1 life remaining");
        def(HeartType.WITHERED,   "&0&lWithered Heart",   Material.WITHER_ROSE,
                "&7Hits apply Wither I for 5s. (20s cooldown)", "&eCrafting Recipe");
        def(HeartType.WARDEN,     "&5&lWarden Heart",     Material.SCULK_CATALYST,
                "&7Right-click: Warden Sonic Beam (capped 6 dmg, 60s cd)",
                "&eKill 1 Warden");
        def(HeartType.ROTTEN,     "&2&lRotten Heart",     Material.ROTTEN_FLESH,
                "&7Hostile mobs ignore you.", "&7Eat rotten flesh/spider eyes safely.",
                "&eKill 250 Zombies, 250 Skeletons, 100 Spiders");
        def(HeartType.TOTEM,      "&a&lTotem Heart",      Material.TOTEM_OF_UNDYING,
                "&7Auto totem proc at 15% HP. 50% chance to break on proc.",
                "&eCrafting Recipe");
        def(HeartType.NETHERITE,  "&8&lNetherite Heart",  Material.NETHERITE_INGOT,
                "&7Permanent Resistance III & Slowness IV.", "&eCrafting Recipe");
        def(HeartType.GOLDEN,     "&e&lGolden Heart",     Material.GOLDEN_APPLE,
                "&7Below 15% HP: Enchanted Golden Apple effects. 50% break chance.",
                "&eCrafting Recipe");
        def(HeartType.RUNNER,     "&f&lRunner Heart",     Material.RABBIT_FOOT,
                "&7Below 15% HP: True Invisibility + Speed III for 10s. (8min cd)",
                "&eAdvancement: Caves and Cliffs");
        def(HeartType.GROWING,    "&a&lGrowing Heart",    Material.CHORUS_FRUIT,
                "&7Jump to grow taller (+0.5 blocks, max 4). Crouch to shrink.",
                "&7Height affects hearts and reach.", "&ePlant all crop seeds");
        def(HeartType.SWIFT,      "&b&lSwift Heart",      Material.SUGAR,
                "&7Permanent Speed I.", "&7Right-click with sword/axe/pick: Riptide dash forward 10 blocks.",
                "&eKill 25 Phantoms");
        def(HeartType.MOLTEN,     "&6&lMolten Heart",     Material.MAGMA_CREAM,
                "&7Auto-smelt mined ores.", "&eMine 2000 ores total");
        def(HeartType.LUCKY,      "&a&lLucky Heart",      Material.RABBIT_FOOT,
                "&7Permanent Luck III.", "&eCrafting Recipe");
        def(HeartType.CURSED,     "&5&lCursed Heart",     Material.FERMENTED_SPIDER_EYE,
                "&7Hits apply random debuff. (60s cooldown)", "&eKill 50 Witches");
        def(HeartType.HEAVEN,     "&f&lHeaven Heart",     Material.GHAST_TEAR,
                "&7On hit taken: Regen I for 5s. (5s cooldown after regen ends)",
                "&eCommand Only");
        def(HeartType.HELL,       "&4&lHell Heart",       Material.BLAZE_POWDER,
                "&7Permanent Strength II.", "&7Every 3rd hit: Wither II for 5s on you.",
                "&eKill 5 Players");
        def(HeartType.ENCHANTED,  "&b&lEnchanted Heart",  Material.EXPERIENCE_BOTTLE,
                "&7Double EXP gain. Armor is 2x effective.", "&eCrafting Recipe");
        def(HeartType.DRAGON,     "&4&lDragon Heart",     Material.DRAGON_EGG,
                "&7Right-click: Dragon Breath cloud. Permanent Resistance I.",
                "&eKill the Ender Dragon");
    }

    private static void def(HeartType type, String name, Material mat, String... lore) {
        HEART_DEFS.put(type, new Object[]{name, mat, lore});
    }

    public HeartManager(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        HEART_KEY = new NamespacedKey(plugin, "heart_type");
    }

    public void loadHearts() {
        plugin.getLogger().info("Loaded " + HEART_DEFS.size() + " heart definitions.");
    }

    public int getHeartCount() { return HEART_DEFS.size(); }

    public ItemStack createHeartItem(HeartType type) {
        Object[] def = HEART_DEFS.get(type);
        if (def == null) return null;

        String displayName = (String) def[0];
        Material mat = (Material) def[1];
        String[] lore = (String[]) def[2];

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(c(displayName));
        List<Component> loreList = new ArrayList<>();
        for (String line : lore) loreList.add(c(line));
        meta.lore(loreList);
        meta.getPersistentDataContainer().set(HEART_KEY, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public HeartType getHeartType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String val = meta.getPersistentDataContainer().get(HEART_KEY, PersistentDataType.STRING);
        if (val == null) return null;
        try { return HeartType.valueOf(val); } catch (IllegalArgumentException e) { return null; }
    }

    public String getDisplayName(HeartType type) {
        Object[] def = HEART_DEFS.get(type);
        return def != null ? (String) def[0] : type.name();
    }

    public Set<HeartType> getAllTypes() { return HEART_DEFS.keySet(); }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
