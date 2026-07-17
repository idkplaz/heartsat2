package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.*;

public class GrowingHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private static final UUID REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID HEALTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private final Map<UUID, Boolean> wasAirborne = new HashMap<>();
    private final Map<UUID, Double> lastY = new HashMap<>();

    public GrowingHeartListener(CustomHeartsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.GROWING) || data.isGrowLocked()) return;
        boolean airborne = !p.isOnGround();
        boolean wasAir = wasAirborne.getOrDefault(p.getUniqueId(), false);
        double currY = p.getLocation().getY();
        double prevY = lastY.getOrDefault(p.getUniqueId(), currY);
        if (wasAir && !airborne && currY <= prevY) grow(p, data, 1);
        lastY.put(p.getUniqueId(), currY);
        wasAirborne.put(p.getUniqueId(), airborne);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.GROWING) || data.isGrowLocked() || !event.isSneaking()) return;
        grow(p, data, -1);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (data.getGrowthLevel() != 0) { data.setGrowthLevel(0); applyGrowth(p, data); }
    }

    private void grow(Player p, PlayerData data, int delta) {
        int next = Math.max(-2, Math.min(6, data.getGrowthLevel() + delta));
        if (next == data.getGrowthLevel()) return;
        data.setGrowthLevel(next);
        applyGrowth(p, data);
        double h = 2.0 + (next * 0.5);
        p.sendActionBar(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&aHeight: &f" + String.format("%.1f", h) + " blocks"));
    }

    public void applyGrowth(Player p, PlayerData data) {
        int level = data.getGrowthLevel();
        double healthMod = level * 1.0;
        // Reach: scales up to +1.5 extra blocks at max height (level 6), giving 4.5 total (3 base + 1.5)
        // Negative levels slightly reduce reach by 0.1 per level.
        double reachMod = level >= 0 ? Math.min(level * (1.5 / 6.0), 1.5) : level * 0.1;

        AttributeInstance hp = p.getAttribute(Attribute.MAX_HEALTH);
        if (hp != null) {
            hp.getModifiers().stream().filter(m -> m.getUniqueId().equals(HEALTH_UUID)).findFirst().ifPresent(hp::removeModifier);
            if (healthMod != 0) hp.addModifier(new AttributeModifier(HEALTH_UUID, "growing_hp", healthMod, AttributeModifier.Operation.ADD_NUMBER));
        }
        AttributeInstance reach = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (reach != null) {
            reach.getModifiers().stream().filter(m -> m.getUniqueId().equals(REACH_UUID)).findFirst().ifPresent(reach::removeModifier);
            if (reachMod != 0) reach.addModifier(new AttributeModifier(REACH_UUID, "growing_reach", reachMod, AttributeModifier.Operation.ADD_NUMBER));
        }
        AttributeInstance scale = p.getAttribute(Attribute.SCALE);
        if (scale != null) scale.setBaseValue(Math.max(0.5, Math.min(2.0, 1.0 + level * 0.25)));
    }
}
