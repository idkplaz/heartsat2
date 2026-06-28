package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LivesListener implements Listener {

    private final CustomHeartsPlugin plugin;

    public LivesListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        plugin.getLivesManager().onDeath(event.getEntity());
    }
}
