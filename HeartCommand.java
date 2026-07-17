package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Set;

public class UnlockListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private static final Set<Material> SEEDS = Set.of(
            Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS, Material.CARROT, Material.POTATO, Material.SUGAR_CANE
    );

    public UnlockListener(CustomHeartsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        plugin.getUnlockManager().checkAdvancement(event.getPlayer(),
                event.getAdvancement().getKey().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (SEEDS.contains(event.getBlock().getType()))
            plugin.getUnlockManager().checkSeedPlanted(event.getPlayer(), event.getBlock().getType().name());
    }
}
