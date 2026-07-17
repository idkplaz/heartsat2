package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.*;

public class AllSeeingHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private final Map<UUID, Set<UUID>> glowing = new HashMap<>();

    public AllSeeingHeartListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::scan, 20L, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearGlow(event.getPlayer());
        glowing.remove(event.getPlayer().getUniqueId());
    }

    private void scan() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
            if (data.hasEquipped(HeartType.ALLSEEING)) updateGlow(p);
            else if (glowing.containsKey(p.getUniqueId())) { clearGlow(p); glowing.remove(p.getUniqueId()); }
        }
    }

    private void updateGlow(Player p) {
        double range = plugin.getHeartManager().getMiscDouble(HeartType.ALLSEEING, "glow-range", 22.5);
        Set<UUID> cur = glowing.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());
        Set<UUID> inRange = new HashSet<>();
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), range, range, range)) {
            if (e == p) continue;
            inRange.add(e.getUniqueId());
            if (!cur.contains(e.getUniqueId())) { e.setGlowing(true); cur.add(e.getUniqueId()); }
        }
        cur.removeIf(id -> {
            if (!inRange.contains(id)) {
                p.getWorld().getEntities().stream().filter(e -> e.getUniqueId().equals(id)).findFirst().ifPresent(e -> e.setGlowing(false));
                return true;
            }
            return false;
        });
    }

    private void clearGlow(Player p) {
        Set<UUID> g = glowing.get(p.getUniqueId()); if (g == null) return;
        g.forEach(id -> p.getWorld().getEntities().stream().filter(e -> e.getUniqueId().equals(id)).findFirst().ifPresent(e -> e.setGlowing(false)));
        g.clear();
    }
}
