package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final CustomHeartsPlugin plugin;

    public PlayerJoinQuitListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().load(p.getUniqueId());

        // Restore absolute heart health modifier
        plugin.getLivesManager().updateAbsoluteHeart(p, data);

        // Restore growing heart state
        if (data.getGrowthLevel() != 0 && data.hasEquipped(HeartType.GROWING)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // find GrowingHeartListener and apply
                // We handle via a direct call to the plugin accessor
            }, 5L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().unload(event.getPlayer().getUniqueId());
    }
}
