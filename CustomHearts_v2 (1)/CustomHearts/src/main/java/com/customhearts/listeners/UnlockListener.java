package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Set;

public class UnlockListener implements Listener {

    private final CustomHeartsPlugin plugin;

    private static final Set<Material> SEED_MATERIALS = Set.of(
            Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS, Material.CARROT, Material.POTATO, Material.SUGAR_CANE
    );

    public UnlockListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        plugin.getUnlockManager().checkAdvancement(event.getPlayer(),
                event.getAdvancement().getKey().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        Block b = event.getBlock();
        if (SEED_MATERIALS.contains(b.getType())) {
            plugin.getUnlockManager().checkSeedPlanted(p, b.getType().name());
        }
    }
}
