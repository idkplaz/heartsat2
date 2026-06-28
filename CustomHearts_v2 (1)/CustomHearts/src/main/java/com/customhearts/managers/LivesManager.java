package com.customhearts.managers;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class LivesManager {

    private final CustomHeartsPlugin plugin;

    public LivesManager(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Try to give the player a life (from Hardcore Heart use).
     */
    public boolean giveLife(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int max = plugin.getConfig().getInt("settings.max-lives", 5);
        if (data.getLives() >= max) {
            player.sendMessage(c("&cYou are already at the maximum of &f" + max + " &clives!"));
            return false;
        }
        data.addLife();
        player.sendMessage(c("&a+1 Life! You now have &f" + data.getLives() + " &alives."));
        updateAbsoluteHeart(player, data);
        return true;
    }

    /**
     * Withdraw a life (player-initiated). Cannot go below 2 via withdrawal.
     */
    public boolean withdrawLife(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int threshold = plugin.getConfig().getInt("settings.min-lives-withdraw-threshold", 2);
        if (data.getLives() <= threshold) {
            player.sendMessage(c("&cYou can't withdraw a life — you must keep at least &f" + threshold + " &clives!"));
            return false;
        }
        data.removeLife();
        player.sendMessage(c("&e-1 Life withdrawn. You now have &f" + data.getLives() + " &elives."));
        return true;
    }

    /**
     * Called on player death — removes a life. Returns true if player is out of lives.
     */
    public boolean onDeath(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        // Reset growth heart on death
        if (data.getGrowthLevel() != 0) {
            data.setGrowthLevel(0);
            resetGrowthState(player);
        }

        if (data.getLives() <= 1) {
            // Out of lives — handle as needed (kick, spectator, etc.)
            player.sendMessage(c("&4&lYou have run out of lives! &cGame over."));
            return true;
        }

        data.removeLife();
        player.sendMessage(c("&c&lYou died! &cLives remaining: &f" + data.getLives()));

        // Check if absolute heart should now trigger
        updateAbsoluteHeart(player, data);

        // Check if reaching 1 life unlocks absolute heart
        if (data.getLives() == 1 && !data.hasUnlocked(HeartType.ABSOLUTE)) {
            plugin.getUnlockManager().grantHeart(player, data, HeartType.ABSOLUTE,
                    "&f%player% &7has reached &f1 life&7 and unlocked the &fAbsolute Heart&7!");
        }

        return false;
    }

    /**
     * Updates max health based on Absolute Heart and growth level.
     */
    public void updateAbsoluteHeart(Player player, PlayerData data) {
        if (!data.hasEquipped(HeartType.ABSOLUTE)) {
            // Reset to default 20 HP
            player.setMaxHealth(20.0);
            return;
        }
        int lives = data.getLives();
        double base = 20.0;
        if (lives == 1) base = 32.0;       // 16 hearts
        else if (lives == 2) base = 26.0;  // 13 hearts (+3 bonus)
        player.setMaxHealth(base);
    }

    private void resetGrowthState(Player player) {
        player.setMaxHealth(20.0);
        // Reset bounding box via attribute — handled in GrowingHeartListener
    }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
