package com.customhearts.listeners;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class HeartAbilityListener implements Listener {

    private final CustomHeartsPlugin plugin;
    private final Random random = new Random();

    public HeartAbilityListener(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
        startPassiveTicker();
        startSwimTracker();
    }

    // ── PASSIVE TICKER ────────────────────────────────────────────────────────

    private void startPassiveTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
                applyPassives(p, data);
            }
        }, 40L, 40L);
    }

    private void applyPassives(Player p, PlayerData data) {
        if (data.hasEquipped(HeartType.BLAZE))
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, true, false));
        if (data.hasEquipped(HeartType.SOUL))
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, true, false));
        if (data.hasEquipped(HeartType.OCEAN)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 60, 0, true, false));
        }
        if (data.hasEquipped(HeartType.CLOUD))
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, true, false));
        if (data.hasEquipped(HeartType.SWIFT))
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, true, false));
        if (data.hasEquipped(HeartType.NETHERITE)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, true, false));
        }
        if (data.hasEquipped(HeartType.LUCKY))
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60, 2, true, false));
        if (data.hasEquipped(HeartType.EMERALD))
            p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 60, 4, true, false));
        if (data.hasEquipped(HeartType.HELL))
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, true, false));
        if (data.hasEquipped(HeartType.DRAGON))
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, true, false));
    }

    // ── SWIM TRACKER ─────────────────────────────────────────────────────────

    private void startSwimTracker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isSwimming() || p.getLocation().getBlock().getType() == Material.WATER) {
                    plugin.getUnlockManager().checkSwimProgress(p, 1);
                }
            }
        }, 20L, 20L);
    }

    // ── ON DAMAGE TAKEN ───────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());

        double healthAfter = p.getHealth() - event.getFinalDamage();
        double maxHealth = p.getMaxHealth();
        double pct = (healthAfter / maxHealth) * 100.0;

        // Heaven Heart — regen on hit taken
        if (data.hasEquipped(HeartType.HEAVEN) && !data.isOnCooldown("heaven")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false));
            // Cooldown starts after regen (5s regen + 5s wait = 10s total, scheduled)
            new BukkitRunnable() {
                @Override public void run() {
                    data.setCooldown("heaven", 5000);
                }
            }.runTaskLater(plugin, 100L);
        }

        boolean lowHealth = pct <= 15 && healthAfter > 0;

        // Totem Heart — 15% threshold
        if (data.hasEquipped(HeartType.TOTEM) && lowHealth && !data.isOnCooldown("totem")) {
            data.setCooldown("totem", 60000);
            totemProc(p);
            // 50% break chance
            if (random.nextBoolean()) {
                data.unequipHeart(HeartType.TOTEM);
                p.sendMessage(c("&cYour &aTotem Heart &cshattered!"));
                p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 0.5f);
            }
        }

        // Golden Heart — 15% threshold
        if (data.hasEquipped(HeartType.GOLDEN) && lowHealth && !data.isOnCooldown("golden")) {
            data.setCooldown("golden", 60000);
            goldenAppleEffects(p);
            if (random.nextBoolean()) {
                data.unequipHeart(HeartType.GOLDEN);
                p.sendMessage(c("&cYour &eGolden Heart &cshattered!"));
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
        }

        // Runner Heart — 15% threshold
        if (data.hasEquipped(HeartType.RUNNER) && lowHealth && !data.isOnCooldown("runner")) {
            data.setCooldown("runner", 480000); // 8 minutes
            runnerAbility(p);
        }
    }

    // ── ON HIT (deal damage) ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHitEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());

        // Blaze Heart — fire aspect 3 (15 seconds)
        if (data.hasEquipped(HeartType.BLAZE)) {
            target.setFireTicks(300);
        }

        // Frozen Heart — powder snow freeze effect on hit
        if (data.hasEquipped(HeartType.FROZEN) && !data.isOnCooldown("frozen")) {
            data.setCooldown("frozen", 45000);
            target.setFreezeTicks(140); // ~7 seconds of powder snow effect
            p.sendActionBar(c("&3Frozen Heart: &bFreeze applied!"));
        }

        // Withered Heart — wither 1 for 5s, 20s cd
        if (data.hasEquipped(HeartType.WITHERED) && !data.isOnCooldown("withered")) {
            data.setCooldown("withered", 20000);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
        }

        // Cursed Heart — random debuff, 60s cd
        if (data.hasEquipped(HeartType.CURSED) && target instanceof Player && !data.isOnCooldown("cursed")) {
            data.setCooldown("cursed", 60000);
            applyCursedDebuff((Player) target);
        }

        // Hell Heart — every 3rd hit applies wither 2 for 5s on self
        if (data.hasEquipped(HeartType.HELL)) {
            int hits = (int) data.getCooldownRemainingMs("hell_hits"); // repurpose as counter
            // Use a simple counter stored in cooldowns with special key
            incrementHellCounter(p, data);
        }

        // Enchanted Heart — double XP is handled in XP gain event
    }

    private void incrementHellCounter(Player p, PlayerData data) {
        // Use a simple int stored via a tag
        String key = "hell_hit_count";
        // We track via a separate mechanism: reuse cooldowns map as a counter trick
        // Store hit count as a very large "expiry" that we interpret as a counter
        long current = data.getCooldownRemainingMs("hell_counter_raw");
        int count = (int)(current % 10000); // encode count in low bits
        count++;
        if (count >= 3) {
            count = 0;
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            p.sendActionBar(c("&4Hell Heart: &cWither II!"));
        }
        // Store: encode count + large offset so it doesn't expire
        data.setCooldown("hell_counter_raw", count + (System.currentTimeMillis() + 999999999L - System.currentTimeMillis()));
    }

    // ── ON KILL ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String mob = event.getEntityType().name();
        plugin.getUnlockManager().checkMobKill(killer, mob);

        if (event.getEntity() instanceof Player) {
            plugin.getUnlockManager().checkPlayerKill(killer);
        }
    }

    // ── RIGHT CLICK ───────────────────────────────────────────────────────────

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;
        Player p = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        ItemStack held = p.getInventory().getItemInMainHand();
        boolean hasMeleeWeapon = isMeleeWeapon(held);

        // Enderman Heart — teleport 15 blocks in look direction
        if (data.hasEquipped(HeartType.ENDERMAN) && hasMeleeWeapon && !data.isOnCooldown("enderman")) {
            data.setCooldown("enderman", 3000);
            endermanTeleport(p);
            event.setCancelled(true);
        }

        // Cloud Heart — wind dash upward
        if (data.hasEquipped(HeartType.CLOUD) && hasMeleeWeapon && !data.isOnCooldown("cloud")) {
            data.setCooldown("cloud", 5000);
            cloudDash(p);
            event.setCancelled(true);
        }

        // Swift Heart — riptide forward dash
        if (data.hasEquipped(HeartType.SWIFT) && hasMeleeWeapon && !data.isOnCooldown("swift")) {
            data.setCooldown("swift", 5000);
            swiftDash(p);
            event.setCancelled(true);
        }

        // Warden Heart — sonic beam
        if (data.hasEquipped(HeartType.WARDEN) && !data.isOnCooldown("warden")) {
            data.setCooldown("warden", 60000);
            wardenBeam(p);
            event.setCancelled(true);
        }

        // Dragon Heart — dragon breath
        if (data.hasEquipped(HeartType.DRAGON) && !data.isOnCooldown("dragon")) {
            data.setCooldown("dragon", 10000);
            dragonBreath(p);
            event.setCancelled(true);
        }

        // Hardcore Heart — use item to gain a life
        HeartType handHeart = plugin.getHeartManager().getHeartType(held);
        if (handHeart == HeartType.HARDCORE) {
            if (plugin.getLivesManager().giveLife(p)) {
                held.setAmount(held.getAmount() - 1);
            }
            event.setCancelled(true);
        }
    }

    // ── XP GAIN ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onXpGain(PlayerExpChangeEvent event) {
        PlayerData data = plugin.getPlayerDataManager().get(event.getPlayer().getUniqueId());
        if (data.hasEquipped(HeartType.ENCHANTED)) {
            event.setAmount(event.getAmount() * 2);
        }
    }

    // ── GOBLIN HEART — equip state ────────────────────────────────────────────

    // (Handled in GoblinHeartListener)

    // ── ABILITY IMPLEMENTATIONS ───────────────────────────────────────────────

    private void endermanTeleport(Player p) {
        var dir = p.getLocation().getDirection().normalize();
        var loc = p.getLocation().clone();
        Location dest = null;

        for (double d = 1; d <= 15; d += 0.5) {
            Location check = loc.clone().add(dir.clone().multiply(d));
            // Must not be inside a solid block
            if (check.getBlock().getType().isSolid()) break;
            // Check feet and head
            Location feet = check.clone();
            Location head = check.clone().add(0, 1, 0);
            if (!feet.getBlock().getType().isSolid() && !head.getBlock().getType().isSolid()) {
                dest = feet;
            }
        }

        if (dest == null) {
            p.sendActionBar(c("&5Enderman Heart: &cNo valid teleport destination!"));
            return;
        }

        dest.setYaw(p.getLocation().getYaw());
        dest.setPitch(p.getLocation().getPitch());
        p.teleport(dest);
        p.getWorld().spawnParticle(Particle.PORTAL, dest, 20, 0.3, 0.5, 0.3, 0.1);
        p.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    private void cloudDash(Player p) {
        // Mimics wind charge upward launch
        Vector vel = new Vector(0, 1.8, 0);
        p.setVelocity(vel);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1f, 1f);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 20, 0.3, 0.3, 0.3, 0.05);
    }

    private void swiftDash(Player p) {
        // Riptide-style dash: launch in look direction with bounce behavior
        Vector dir = p.getLocation().getDirection().normalize().multiply(2.5);
        p.setVelocity(dir);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p.getLocation(), 10, 0.5, 0.3, 0.5, 0.1);
    }

    private void wardenBeam(Player p) {
        LivingEntity target = findNearest(p, 20.0);
        if (target == null) {
            p.sendActionBar(c("&5Warden Heart: &cNo target in range!"));
            return;
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1f);
        p.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation().add(0, 1, 0), 1);
        target.damage(6.0, p); // capped at 3 hearts = 6 HP
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
        drawBeam(p.getEyeLocation(), target.getLocation().add(0, 1, 0));
    }

    private void dragonBreath(Player p) {
        Location loc = p.getLocation();
        AreaEffectCloud cloud = (AreaEffectCloud) p.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(3.0f);
        cloud.setDuration(100);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.setColor(Color.fromRGB(101, 0, 164));
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1), true);
        cloud.setSource(p);
        p.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1f, 1f);
        p.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 1.5, 0.5, 1.5, 0.05);
    }

    private void totemProc(Player p) {
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 30, 0.5, 1, 0.5, 0.1);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0, true, false));
        if (p.getHealth() <= 2.0) p.setHealth(1.0);
    }

    private void goldenAppleEffects(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 3, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0, true, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
    }

    private void runnerAbility(Player p) {
        // True invisibility: hide armor and held item for 5 seconds
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, true, false)); // 10s speed 3

        // Hide equipment from other players temporarily
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == p) continue;
            other.hidePlayer(plugin, p);
        }
        new BukkitRunnable() {
            @Override public void run() {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (other == p) continue;
                    other.showPlayer(plugin, p);
                }
            }
        }.runTaskLater(plugin, 100L);

        p.sendActionBar(c("&fRunner Heart: &7True Invisibility active!"));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 2f);
    }

    private void applyCursedDebuff(Player target) {
        int roll = random.nextInt(3);
        switch (roll) {
            case 0 -> target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 0));
            case 1 -> target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 0));
            case 2 -> target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 0));
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private boolean isMeleeWeapon(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE");
    }

    private LivingEntity findNearest(Player p, double range) {
        LivingEntity closest = null;
        double dist = Double.MAX_VALUE;
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), range, range, range)) {
            if (e == p || !(e instanceof LivingEntity le)) continue;
            double d = e.getLocation().distanceSquared(p.getLocation());
            if (d < dist) { dist = d; closest = le; }
        }
        return closest;
    }

    private void drawBeam(Location from, Location to) {
        double steps = from.distance(to) / 0.5;
        double dx = (to.getX() - from.getX()) / steps;
        double dy = (to.getY() - from.getY()) / steps;
        double dz = (to.getZ() - from.getZ()) / steps;
        World world = from.getWorld();
        for (int i = 0; i < (int) steps; i++) {
            Location loc = from.clone().add(dx * i, dy * i, dz * i);
            world.spawnParticle(Particle.SCULK_SOUL, loc, 1, 0, 0, 0, 0);
        }
    }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
