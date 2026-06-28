package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class AllSeeingHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private final Map<UUID, Integer> tasks = new HashMap<>();
    private final Map<UUID, Set<UUID>> glowingEntities = new HashMap<>();

    public AllSeeingHeartListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        // Start global scanner for all online players
        Bukkit.getScheduler().runTaskTimer(plugin, this::scanAllPlayers, 20L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Nothing needed; scanner handles it
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Remove glow from any entities this player was making glow
        clearGlow(event.getPlayer());
        glowingEntities.remove(event.getPlayer().getUniqueId());
    }

    private void scanAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
            if (data.hasEquipped(HeartType.ALLSEEING)) {
                updateGlow(p);
            } else {
                // Remove any leftover glow from this player
                if (glowingEntities.containsKey(p.getUniqueId())) {
                    clearGlow(p);
                    glowingEntities.remove(p.getUniqueId());
                }
            }
        }
    }

    private void updateGlow(Player p) {
        double range = 22.5; // half of 45x45x45
        Set<UUID> currentGlowing = glowingEntities.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());
        Set<UUID> nowInRange = new HashSet<>();

        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), range, range, range)) {
            if (e == p) continue;
            nowInRange.add(e.getUniqueId());
            if (!currentGlowing.contains(e.getUniqueId())) {
                e.setGlowing(true);
                currentGlowing.add(e.getUniqueId());
            }
        }

        // Remove glow from entities that left range
        Iterator<UUID> iter = currentGlowing.iterator();
        while (iter.hasNext()) {
            UUID id = iter.next();
            if (!nowInRange.contains(id)) {
                Entity e = findEntity(p, id);
                if (e != null) e.setGlowing(false);
                iter.remove();
            }
        }
    }

    private void clearGlow(Player p) {
        Set<UUID> glowing = glowingEntities.get(p.getUniqueId());
        if (glowing == null) return;
        for (UUID id : glowing) {
            Entity e = findEntity(p, id);
            if (e != null) e.setGlowing(false);
        }
        glowing.clear();
    }

    private Entity findEntity(Player p, UUID id) {
        for (Entity e : p.getWorld().getEntities()) {
            if (e.getUniqueId().equals(id)) return e;
        }
        return null;
    }
}
