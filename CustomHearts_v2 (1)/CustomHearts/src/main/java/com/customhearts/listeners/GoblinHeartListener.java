package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoblinHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;

    public GoblinHeartListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        // Tick every second to check held item
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
                if (!data.hasEquipped(HeartType.GOBLIN)) continue;
                String held = p.getInventory().getItemInMainHand().getType().name();
                if (held.endsWith("_PICKAXE") || held.endsWith("_SHOVEL")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, true, false));
                } else {
                    p.removePotionEffect(PotionEffectType.HASTE);
                }
            }
        }, 20L, 20L);
    }
}
