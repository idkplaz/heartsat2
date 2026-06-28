package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;

public class RottenHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;

    public RottenHeartListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player p)) return;
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.ROTTEN)) return;
        if (!(event.getEntity() instanceof Monster)) return;
        // Cancel hostile mob targeting
        event.setCancelled(true);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.ROTTEN)) return;

        var mat = event.getItem().getType();
        if (mat == org.bukkit.Material.ROTTEN_FLESH || mat == org.bukkit.Material.SPIDER_EYE) {
            // Schedule removal of bad effects after eating
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.HUNGER);
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
            }, 1L);
        }
    }
}
