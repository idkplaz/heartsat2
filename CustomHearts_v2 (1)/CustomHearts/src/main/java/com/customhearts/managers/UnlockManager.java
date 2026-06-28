package com.customhearts.managers;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UnlockManager {

    private final CustomHeartsPlugin plugin;

    public UnlockManager(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkMobKill(Player player, String mobType) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int kills = data.incrementMobKills(mobType);

        // Single mob hearts
        checkSingle(player, data, HeartType.BLAZE, "BLAZE", kills, 100);
        checkSingle(player, data, HeartType.WARDEN, "WARDEN", kills, 1);
        checkSingle(player, data, HeartType.CLOUD, "BREEZE", kills, 45);
        checkSingle(player, data, HeartType.SWIFT, "PHANTOM", kills, 25);
        checkSingle(player, data, HeartType.CURSED, "WITCH", kills, 50);
        checkSingle(player, data, HeartType.DRAGON, "ENDER_DRAGON", kills, 1);

        // Multi-mob hearts
        checkEndermanHeart(player, data);
        checkRottenHeart(player, data);
        checkOceanHeart(player, data);
        checkHellHeart(player, data);
    }

    public void checkPlayerKill(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int kills = data.incrementPlayerKills();
        if (!data.hasUnlocked(HeartType.HELL) && kills >= 5) {
            grantHeart(player, data, HeartType.HELL, "&4%player% &7earned the &4Hell Heart&7!");
        }
    }

    public void checkAdvancement(Player player, String key) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (!data.hasUnlocked(HeartType.SOUL) && key.equalsIgnoreCase("minecraft:nether/return_to_sender")) {
            grantHeart(player, data, HeartType.SOUL, "&8%player% &7earned the &8Soul Heart&7!");
        }
        if (!data.hasUnlocked(HeartType.RUNNER) && key.equalsIgnoreCase("minecraft:adventure/caves_and_cliffs")) {
            grantHeart(player, data, HeartType.RUNNER, "&f%player% &7earned the &fRunner Heart&7!");
        }
    }

    public void checkBlockMined(Player player, String blockType) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int count = data.incrementBlocksMined(blockType);

        // Frozen heart — ice
        if (!data.hasUnlocked(HeartType.FROZEN) && blockType.equals("ICE")) {
            data.incrementIceMined();
            if (data.getIceMined() >= 1000) {
                grantHeart(player, data, HeartType.FROZEN, "&3%player% &7earned the &3Frozen Heart&7!");
            }
        }

        // Goblin heart — stone & deepslate
        if (!data.hasUnlocked(HeartType.GOBLIN)) {
            int stone = data.getBlocksMined("STONE");
            int deep = data.getBlocksMined("DEEPSLATE");
            if (stone >= 7500 && deep >= 5250) {
                grantHeart(player, data, HeartType.GOBLIN, "&2%player% &7earned the &2Goblin Heart&7!");
            }
        }

        // Molten heart — total ores
        if (!data.hasUnlocked(HeartType.MOLTEN) && isOre(blockType)) {
            data.incrementOresMined();
            if (data.getOresMined() >= 2000) {
                grantHeart(player, data, HeartType.MOLTEN, "&6%player% &7earned the &6Molten Heart&7!");
            }
        }
    }

    public void checkSeedPlanted(Player player, String seedType) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data.hasUnlocked(HeartType.GROWING)) return;
        data.addSeedPlanted(seedType);
        if (data.hasAllSeeds()) {
            grantHeart(player, data, HeartType.GROWING, "&a%player% &7earned the &aGrowing Heart&7!");
        }
    }

    public void checkSwimProgress(Player player, long blocks) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.addBlocksSwum(blocks);
        checkOceanHeart(player, data);
    }

    // ── MULTI-CONDITION CHECKS ────────────────────────────────────────────────

    private void checkEndermanHeart(Player player, PlayerData data) {
        if (data.hasUnlocked(HeartType.ENDERMAN)) return;
        if (data.getMobKills("ENDERMAN") >= 100 && data.getMobKills("ENDERMITE") >= 10) {
            grantHeart(player, data, HeartType.ENDERMAN, "&5%player% &7earned the &5Enderman Heart&7!");
        }
    }

    private void checkRottenHeart(Player player, PlayerData data) {
        if (data.hasUnlocked(HeartType.ROTTEN)) return;
        if (data.getMobKills("ZOMBIE") >= 250 && data.getMobKills("SKELETON") >= 250
                && data.getMobKills("SPIDER") >= 100) {
            grantHeart(player, data, HeartType.ROTTEN, "&2%player% &7earned the &2Rotten Heart&7!");
        }
    }

    private void checkOceanHeart(Player player, PlayerData data) {
        if (data.hasUnlocked(HeartType.OCEAN)) return;
        if (data.getMobKills("ELDER_GUARDIAN") >= 3 && data.getBlocksSwum() >= 10000) {
            grantHeart(player, data, HeartType.OCEAN, "&1%player% &7earned the &1Ocean Heart&7!");
        }
    }

    private void checkHellHeart(Player player, PlayerData data) {
        if (data.hasUnlocked(HeartType.HELL)) return;
        if (data.getPlayerKills() >= 5) {
            grantHeart(player, data, HeartType.HELL, "&4%player% &7earned the &4Hell Heart&7!");
        }
    }

    private void checkSingle(Player player, PlayerData data, HeartType type,
                              String mob, int currentKills, int required) {
        if (data.hasUnlocked(type)) return;
        if (!mob.equals(mob) || data.getMobKills(mob) < required) return; // re-fetch
        if (data.getMobKills(mob) >= required) {
            grantHeart(player, data, type, null);
        }
    }

    public void grantHeart(Player player, PlayerData data, HeartType type, String announcement) {
        if (data.hasUnlocked(type)) return;
        data.unlockHeart(type);

        String name = plugin.getHeartManager().getDisplayName(type);
        player.sendMessage(c("&aYou unlocked: " + name + "&a!"));
        player.sendMessage(c("&7Use &f/heart equip " + type.name().toLowerCase() + " &7to equip it."));

        if (announcement != null && !announcement.isEmpty()) {
            String msg = announcement.replace("%player%", player.getName());
            Bukkit.broadcast(c(msg));
        }
    }

    public boolean isOre(String block) {
        return block.contains("_ORE") || block.equals("ANCIENT_DEBRIS");
    }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
