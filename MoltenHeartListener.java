package com.customhearts.managers;

import com.customhearts.hearts.HeartType;
import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private int lives = 3;
    private final List<HeartType> equippedHearts = new ArrayList<>();
    private final Set<HeartType> unlockedHearts = new HashSet<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<String, Integer> mobKills = new HashMap<>();
    private final Map<String, Integer> blocksMined = new HashMap<>();
    private int playerKills = 0;
    private long blocksSwum = 0;
    private int oresMined = 0;
    private int iceMined = 0;
    private final Set<String> seedsPlanted = new HashSet<>();
    private int growthLevel = 0;
    private boolean growLocked = false;

    public PlayerData(UUID uuid) { this.uuid = uuid; }

    public UUID getUuid() { return uuid; }

    // Lives
    public int getLives() { return lives; }
    public void setLives(int v) { lives = Math.max(1, v); }
    public void addLife(int max) { lives = Math.min(lives + 1, max); }
    public boolean removeLife() { if (lives <= 1) return false; lives--; return true; }

    // Equipped
    public List<HeartType> getEquippedHearts() { return Collections.unmodifiableList(equippedHearts); }
    public List<HeartType> getEquippedHeartsMutable() { return equippedHearts; }
    public boolean equipHeart(HeartType t, int max) {
        if (equippedHearts.contains(t) || equippedHearts.size() >= max) return false;
        equippedHearts.add(t); return true;
    }
    public boolean unequipHeart(HeartType t) { return equippedHearts.remove(t); }
    public boolean hasEquipped(HeartType t) { return equippedHearts.contains(t); }

    // Unlocked
    public Set<HeartType> getUnlockedHearts() { return Collections.unmodifiableSet(unlockedHearts); }
    public Set<HeartType> getUnlockedHeartsMutable() { return unlockedHearts; }
    public void unlockHeart(HeartType t) { unlockedHearts.add(t); }
    public boolean hasUnlocked(HeartType t) { return unlockedHearts.contains(t); }

    // Cooldowns
    public boolean isOnCooldown(String key) {
        Long exp = cooldowns.get(key); return exp != null && System.currentTimeMillis() < exp;
    }
    public long getCooldownRemainingMs(String key) {
        Long exp = cooldowns.get(key); return exp == null ? 0 : Math.max(0, exp - System.currentTimeMillis());
    }
    public void setCooldown(String key, long ms) { cooldowns.put(key, System.currentTimeMillis() + ms); }

    // Mob kills
    public int getMobKills(String mob) { return mobKills.getOrDefault(mob.toUpperCase(), 0); }
    public int incrementMobKills(String mob) {
        int v = mobKills.getOrDefault(mob.toUpperCase(), 0) + 1;
        mobKills.put(mob.toUpperCase(), v); return v;
    }
    public Map<String, Integer> getMobKillMap() { return mobKills; }

    // Block mining
    public int getBlocksMined(String b) { return blocksMined.getOrDefault(b.toUpperCase(), 0); }
    public int incrementBlocksMined(String b) {
        int v = blocksMined.getOrDefault(b.toUpperCase(), 0) + 1;
        blocksMined.put(b.toUpperCase(), v); return v;
    }
    public Map<String, Integer> getBlocksMinedMap() { return blocksMined; }

    // Misc counters
    public int getPlayerKills() { return playerKills; }
    public int incrementPlayerKills() { return ++playerKills; }
    public void setPlayerKills(int v) { playerKills = v; }
    public long getBlocksSwum() { return blocksSwum; }
    public void addBlocksSwum(long v) { blocksSwum += v; }
    public void setBlocksSwum(long v) { blocksSwum = v; }
    public int getOresMined() { return oresMined; }
    public void incrementOresMined() { oresMined++; }
    public void setOresMined(int v) { oresMined = v; }
    public int getIceMined() { return iceMined; }
    public void incrementIceMined() { iceMined++; }
    public void setIceMined(int v) { iceMined = v; }

    // Seeds
    public Set<String> getSeedsPlanted() { return seedsPlanted; }
    public void addSeedPlanted(String s) { seedsPlanted.add(s); }
    public boolean hasAllSeeds() {
        return seedsPlanted.containsAll(Set.of("MELON_SEEDS","PUMPKIN_SEEDS","WHEAT_SEEDS",
                "BEETROOT_SEEDS","CARROT","POTATO","SUGAR_CANE"));
    }

    // Growing heart
    public int getGrowthLevel() { return growthLevel; }
    public void setGrowthLevel(int v) { growthLevel = v; }
    public boolean isGrowLocked() { return growLocked; }
    public void setGrowLocked(boolean v) { growLocked = v; }
}
