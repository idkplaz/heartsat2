package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

public class GrowingHeartListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private static final UUID GROW_REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID GROW_HEALTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    // Track last Y position to detect jump apex
    private final java.util.Map<UUID, Double> lastY = new java.util.HashMap<>();
    private final java.util.Map<UUID, Boolean> wasAirborne = new java.util.HashMap<>();

    public GrowingHeartListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.GROWING)) return;
        if (data.isGrowLocked()) return;

        double prevY = lastY.getOrDefault(p.getUniqueId(), p.getLocation().getY());
        double currY = p.getLocation().getY();
        boolean airborne = !p.isOnGround();
        boolean wasAir = wasAirborne.getOrDefault(p.getUniqueId(), false);

        // Detect landing after jump (was airborne, now on ground, and was going up)
        if (wasAir && !airborne && currY <= prevY) {
            // Player landed — this counts as a jump completion
            grow(p, data, 1);
        }

        lastY.put(p.getUniqueId(), currY);
        wasAirborne.put(p.getUniqueId(), airborne);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasEquipped(HeartType.GROWING)) return;
        if (data.isGrowLocked()) return;
        if (!event.isSneaking()) return;

        // Shrink on crouch
        grow(p, data, -1);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (data.getGrowthLevel() != 0) {
            data.setGrowthLevel(0);
            applyGrowthEffects(p, data);
        }
    }

    private void grow(Player p, PlayerData data, int delta) {
        int current = data.getGrowthLevel();
        // +delta: max is +6 steps (3 extra blocks / 0.5 each)
        // -delta: min is -2 steps (player becomes 1 block tall from 2 block default)
        int maxGrow = 6;  // 6 * 0.5 = 3 extra blocks = 4 blocks total
        int minGrow = -2; // shrink to 1 block tall

        int newLevel = current + delta;
        newLevel = Math.max(minGrow, Math.min(maxGrow, newLevel));
        if (newLevel == current) return;

        data.setGrowthLevel(newLevel);
        applyGrowthEffects(p, data);

        double heightBlocks = 2.0 + (newLevel * 0.5);
        p.sendActionBar(c("&aGrowing Heart: &fHeight " + String.format("%.1f", heightBlocks) + " blocks"));
    }

    public void applyGrowthEffects(Player p, PlayerData data) {
        int level = data.getGrowthLevel();

        // Health modifier: +0.5 hearts (1 HP) per positive level, -0.5 hearts per negative
        double healthMod = level * 1.0; // 1 HP per growth level

        // Reach modifier: +0.75 per positive, -0.25 per negative
        double reachMod = level >= 0 ? level * 0.75 : level * 0.25;

        // Apply health attribute
        AttributeInstance healthAttr = p.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.getModifiers().stream()
                    .filter(m -> m.getUniqueId().equals(GROW_HEALTH_UUID))
                    .findFirst().ifPresent(healthAttr::removeModifier);
            if (healthMod != 0) {
                healthAttr.addModifier(new AttributeModifier(
                        GROW_HEALTH_UUID, "growing_heart_health", healthMod,
                        AttributeModifier.Operation.ADD_NUMBER));
            }
        }

        // Apply reach/block interaction attribute
        AttributeInstance reachAttr = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) {
            reachAttr.getModifiers().stream()
                    .filter(m -> m.getUniqueId().equals(GROW_REACH_UUID))
                    .findFirst().ifPresent(reachAttr::removeModifier);
            if (reachMod != 0) {
                reachAttr.addModifier(new AttributeModifier(
                        GROW_REACH_UUID, "growing_heart_reach", reachMod,
                        AttributeModifier.Operation.ADD_NUMBER));
            }
        }

        AttributeInstance entityReach = p.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (entityReach != null) {
            entityReach.getModifiers().stream()
                    .filter(m -> m.getUniqueId().equals(GROW_REACH_UUID))
                    .findFirst().ifPresent(entityReach::removeModifier);
            if (reachMod != 0) {
                entityReach.addModifier(new AttributeModifier(
                        GROW_REACH_UUID, "growing_heart_entity_reach", reachMod,
                        AttributeModifier.Operation.ADD_NUMBER));
            }
        }

        // Scale the player visually using the scale attribute (Paper 1.21)
        AttributeInstance scaleAttr = p.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) {
            double scale = 1.0 + (level * 0.25); // proportional scaling
            scale = Math.max(0.5, Math.min(2.0, scale));
            scaleAttr.setBaseValue(scale);
        }
    }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
