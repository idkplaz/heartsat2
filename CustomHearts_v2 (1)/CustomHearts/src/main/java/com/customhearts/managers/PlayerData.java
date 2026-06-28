package com.customhearts.managers;

import com.customhearts.hearts.HeartType;

import java.util.*;

public class PlayerData {

    private final UUID uuid;

    // Lives
    private int lives = 3;

    // Equipped hearts (max 3 slots)
    private final List<HeartType> equippedHearts = new ArrayList<>();

    // Unlocked hearts
    private final Set<HeartType> unlockedHearts = new HashSet<>();

    // Cooldowns: key -> expiry millis
    private final Map<String, Long> cooldowns = new HashMap<>();

    // Mob kill tracking
    private final Map<String, Integer> mobKills = new HashMap<>();

    // Block mine tracking
    private final Map<String, Integer> blocksMined = new HashMap<>();

    // Misc counters
    private int playerKills = 0;
    private long blockSwum = 0;        // blocks swum for ocean heart
    private int oresMined = 0;         // total ores for molten heart
    private int iceMined = 0;          // ice for frozen heart

    // Seeds planted flags (for growing heart)
    private final Set<String> seedsPlanted = new HashSet<>();

    // Growing heart state
    private int growthLevel = 0;       // 0 = normal, positive = taller, negative = shorter
    private boolean growLocked = false;

    // All-Seeing Heart scanner task id (runtime only)
    private transient int allSeeingTaskId = -1;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    // ── LIVES ────────────────────────────────────────────────────────────────

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = Math.max(1, lives); }
    public void addLife() { lives = Math.min(lives + 1, 5); }
    public boolean removeLife() {
        if (lives <= 1) return false;
        lives--;
        return true;
    }

    // ── EQUIPPED HEARTS ──────────────────────────────────────────────────────

    public List<HeartType> getEquippedHearts() { return Collections.unmodifiableList(equippedHearts); }

    public boolean equipHeart(HeartType type, int maxSlots) {
        if (equippedHearts.contains(type)) return false;
        if (equippedHearts.size() >= maxSlots) return false;
        equippedHearts.add(type);
        return true;
    }

    public boolean unequipHeart(HeartType type) { return equippedHearts.remove(type); }
    public boolean hasEquipped(HeartType type) { return equippedHearts.contains(type); }
    public List<HeartType> getEquippedHeartsMutable() { return equippedHearts; }

    // ── UNLOCKED HEARTS ──────────────────────────────────────────────────────

    public Set<HeartType> getUnlockedHearts() { return Collections.unmodifiableSet(unlockedHearts); }
    public void unlockHeart(HeartType type) { unlockedHearts.add(type); }
    public boolean hasUnlocked(HeartType type) { return unlockedHearts.contains(type); }
    public Set<HeartType> getUnlockedHeartsMutable() { return unlockedHearts; }

    // ── COOLDOWNS ────────────────────────────────────────────────────────────

    public boolean isOnCooldown(String key) {
        Long exp = cooldowns.get(key);
        return exp != null && System.currentTimeMillis() < exp;
    }

    public long getCooldownRemainingMs(String key) {
        Long exp = cooldowns.get(key);
        return exp == null ? 0 : Math.max(0, exp - System.currentTimeMillis());
    }

    public void setCooldown(String key, long ms) {
        cooldowns.put(key, System.currentTimeMillis() + ms);
    }

    public void setCooldownTicks(String key, int ticks) {
        setCooldown(key, ticks * 50L);
    }

    // ── MOB KILLS ────────────────────────────────────────────────────────────

    public int getMobKills(String mob) { return mobKills.getOrDefault(mob.toUpperCase(), 0); }
    public int incrementMobKills(String mob) {
        int v = mobKills.getOrDefault(mob.toUpperCase(), 0) + 1;
        mobKills.put(mob.toUpperCase(), v);
        return v;
    }
    public Map<String, Integer> getMobKillMap() { return mobKills; }

    // ── BLOCK MINING ─────────────────────────────────────────────────────────

    public int getBlocksMined(String block) { return blocksMined.getOrDefault(block.toUpperCase(), 0); }
    public int incrementBlocksMined(String block) {
        int v = blocksMined.getOrDefault(block.toUpperCase(), 0) + 1;
        blocksMined.put(block.toUpperCase(), v);
        return v;
    }
    public Map<String, Integer> getBlocksMinedMap() { return blocksMined; }

    // ── MISC COUNTERS ────────────────────────────────────────────────────────

    public int getPlayerKills() { return playerKills; }
    public int incrementPlayerKills() { return ++playerKills; }
    public void setPlayerKills(int v) { playerKills = v; }

    public long getBlocksSwum() { return blockSwum; }
    public void addBlocksSwum(long v) { blockSwum += v; }
    public void setBlocksSwum(long v) { blockSwum = v; }

    public int getOresMined() { return oresMined; }
    public void incrementOresMined() { oresMined++; }
    public void setOresMined(int v) { oresMined = v; }

    public int getIceMined() { return iceMined; }
    public void incrementIceMined() { iceMined++; }
    public void setIceMined(int v) { iceMined = v; }

    // ── SEEDS ────────────────────────────────────────────────────────────────

    public Set<String> getSeedsPlanted() { return seedsPlanted; }
    public void addSeedPlanted(String seed) { seedsPlanted.add(seed); }
    public boolean hasAllSeeds() {
        Set<String> required = Set.of("MELON_SEEDS", "PUMPKIN_SEEDS", "WHEAT_SEEDS",
                "BEETROOT_SEEDS", "CARROT", "POTATO", "SUGAR_CANE");
        return seedsPlanted.containsAll(required);
    }

    // ── GROWING HEART ────────────────────────────────────────────────────────

    public int getGrowthLevel() { return growthLevel; }
    public void setGrowthLevel(int level) { growthLevel = level; }
    public boolean isGrowLocked() { return growLocked; }
    public void setGrowLocked(boolean locked) { growLocked = locked; }

    // ── ALL-SEEING ───────────────────────────────────────────────────────────

    public int getAllSeeingTaskId() { return allSeeingTaskId; }
    public void setAllSeeingTaskId(int id) { allSeeingTaskId = id; }
}
