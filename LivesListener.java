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
        data.incrementMobKills(mobType);

        tryGrant(player, data, HeartType.BLAZE, () -> data.getMobKills("BLAZE") >= 100);
        tryGrant(player, data, HeartType.WARDEN, () -> data.getMobKills("WARDEN") >= 1);
        tryGrant(player, data, HeartType.CLOUD, () -> data.getMobKills("BREEZE") >= 45);
        tryGrant(player, data, HeartType.SWIFT, () -> data.getMobKills("PHANTOM") >= 25);
        tryGrant(player, data, HeartType.CURSED, () -> data.getMobKills("WITCH") >= 50);
        tryGrant(player, data, HeartType.DRAGON, () -> data.getMobKills("ENDER_DRAGON") >= 1);
        tryGrant(player, data, HeartType.ENDERMAN, () ->
                data.getMobKills("ENDERMAN") >= 100 && data.getMobKills("ENDERMITE") >= 10);
        tryGrant(player, data, HeartType.ROTTEN, () ->
                data.getMobKills("ZOMBIE") >= 250 && data.getMobKills("SKELETON") >= 250
                        && data.getMobKills("SPIDER") >= 100);
        tryGrant(player, data, HeartType.OCEAN, () ->
                data.getMobKills("ELDER_GUARDIAN") >= 3 && data.getBlocksSwum() >= 10000);
    }

    public void checkPlayerKill(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.incrementPlayerKills();
        tryGrant(player, data, HeartType.HELL, () -> data.getPlayerKills() >= 5);
    }

    public void checkAdvancement(Player player, String key) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (key.equalsIgnoreCase("minecraft:nether/return_to_sender"))
            tryGrant(player, data, HeartType.SOUL, () -> true);
        if (key.equalsIgnoreCase("minecraft:adventure/caves_and_cliffs"))
            tryGrant(player, data, HeartType.RUNNER, () -> true);
    }

    public void checkBlockMined(Player player, String blockType) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.incrementBlocksMined(blockType);

        if (blockType.equals("ICE") || blockType.equals("PACKED_ICE") || blockType.equals("BLUE_ICE")) {
            data.incrementIceMined();
            tryGrant(player, data, HeartType.FROZEN, () -> data.getIceMined() >= 1000);
        }
        if (isOre(blockType)) {
            data.incrementOresMined();
            tryGrant(player, data, HeartType.MOLTEN, () -> data.getOresMined() >= 2000);
        }
        tryGrant(player, data, HeartType.GOBLIN, () ->
                data.getBlocksMined("STONE") >= 7500 && data.getBlocksMined("DEEPSLATE") >= 5250);
    }

    public void checkSeedPlanted(Player player, String seedType) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.addSeedPlanted(seedType);
        tryGrant(player, data, HeartType.GROWING, () -> data.hasAllSeeds());
    }

    public void checkSwim(Player player, long blocks) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.addBlocksSwum(blocks);
        tryGrant(player, data, HeartType.OCEAN, () ->
                data.getMobKills("ELDER_GUARDIAN") >= 3 && data.getBlocksSwum() >= 10000);
    }

    public void checkReachedOneLife(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        tryGrant(player, data, HeartType.ABSOLUTE, () -> true);
    }

    /**
     * Core grant method. Checks:
     * 1. Player hasn't already unlocked it
     * 2. Condition is met
     * 3. Server limit hasn't been reached
     */
    private void tryGrant(Player player, PlayerData data, HeartType type, java.util.function.BooleanSupplier condition) {
        if (data.hasUnlocked(type)) return;
        if (!condition.getAsBoolean()) return;

        LimitManager limits = plugin.getLimitManager();
        if (!limits.canObtain(type)) {
            // Limit reached — player met the condition but can't get it
            player.sendMessage(c("&cYou met the requirements for "
                    + plugin.getHeartManager().getDisplayName(type)
                    + " &cbut the server limit has been reached!"));
            return;
        }

        grantHeart(player, data, type, null);
    }

    public void grantHeart(Player player, PlayerData data, HeartType type, String customAnnouncement) {
        if (data.hasUnlocked(type)) return;
        data.unlockHeart(type);
        plugin.getLimitManager().recordObtained(type);

        String name = plugin.getHeartManager().getDisplayName(type);
        player.sendMessage(c("&aYou unlocked: " + name + "&a!"));
        player.sendMessage(c("&7Use &f/heart equip " + type.name().toLowerCase() + " &7to equip it."));

        String announcement = customAnnouncement != null ? customAnnouncement
                : "&7" + player.getName() + " &7unlocked " + name + "&7!";
        Bukkit.broadcast(c(announcement.replace("%player%", player.getName())));
    }

    public boolean isOre(String block) {
        return block.contains("_ORE") || block.equals("ANCIENT_DEBRIS");
    }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
