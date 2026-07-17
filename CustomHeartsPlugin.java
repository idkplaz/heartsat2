package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

public class RottenHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;

    public RottenHeartListener(CustomHeartsPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player p)) return;
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (data.hasEquipped(HeartType.ROTTEN) && event.getEntity() instanceof Monster)
            event.setCancelled(true);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.ROTTEN)) return;
        Material m = event.getItem().getType();
        if (m == Material.ROTTEN_FLESH || m == Material.SPIDER_EYE) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.removePotionEffect(PotionEffectType.HUNGER);
                p.removePotionEffect(PotionEffectType.POISON);
            }, 1L);
        }
    }
}
