package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class MoltenHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private static final Map<Material, Material> SMELT_MAP = new EnumMap<>(Material.class);

    static {
        SMELT_MAP.put(Material.IRON_ORE,         Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE,         Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE,       Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.ANCIENT_DEBRIS,   Material.NETHERITE_SCRAP);
        SMELT_MAP.put(Material.COAL_ORE,         Material.COAL);
        SMELT_MAP.put(Material.DEEPSLATE_COAL_ORE, Material.COAL);
        SMELT_MAP.put(Material.DIAMOND_ORE,      Material.DIAMOND);
        SMELT_MAP.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND);
        SMELT_MAP.put(Material.EMERALD_ORE,      Material.EMERALD);
        SMELT_MAP.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD);
        SMELT_MAP.put(Material.LAPIS_ORE,        Material.LAPIS_LAZULI);
        SMELT_MAP.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_LAZULI);
        SMELT_MAP.put(Material.REDSTONE_ORE,     Material.REDSTONE);
        SMELT_MAP.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE);
        SMELT_MAP.put(Material.NETHER_GOLD_ORE,  Material.GOLD_NUGGET);
        SMELT_MAP.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
    }

    public MoltenHeartListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());

        Material blockType = event.getBlock().getType();

        // Track for unlock
        if (SMELT_MAP.containsKey(blockType)) {
            plugin.getUnlockManager().checkBlockMined(p, blockType.name());
        }

        // Also track stone/deepslate for goblin heart
        if (blockType == Material.STONE || blockType == Material.DEEPSLATE) {
            plugin.getUnlockManager().checkBlockMined(p, blockType.name());
        }

        // Ice for frozen heart
        if (blockType == Material.ICE || blockType == Material.PACKED_ICE || blockType == Material.BLUE_ICE) {
            plugin.getUnlockManager().checkBlockMined(p, "ICE");
        }

        // Molten heart smelt
        if (!data.hasEquipped(HeartType.MOLTEN)) return;
        Material smelted = SMELT_MAP.get(blockType);
        if (smelted == null) return;

        // Cancel normal drops and give smelted result
        event.setDropItems(false);
        p.getInventory().addItem(new ItemStack(smelted, 1));
        // Give appropriate XP
        int xp = getSmeltXP(blockType);
        if (xp > 0) p.giveExp(xp);
    }

    private int getSmeltXP(Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, COPPER_ORE, DEEPSLATE_COPPER_ORE -> 1;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> 1;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> 5;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 5;
            case ANCIENT_DEBRIS -> 2;
            default -> 0;
        };
    }
}
