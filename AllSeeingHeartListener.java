package com.customhearts.managers;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class LivesManager {

    private final CustomHeartsPlugin plugin;

    public LivesManager(CustomHeartsPlugin plugin) { this.plugin = plugin; }

    public boolean giveLife(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int max = plugin.getConfig().getInt("settings.max-lives", 5);
        if (data.getLives() >= max) {
            player.sendMessage(c("&cAlready at max lives (&f" + max + "&c)!"));
            return false;
        }
        data.addLife(max);
        player.sendMessage(c("&a+1 Life! You now have &f" + data.getLives() + " &alives."));
        updateAbsoluteHeart(player, data);
        return true;
    }

    public boolean withdrawLife(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int threshold = plugin.getConfig().getInt("settings.min-lives-withdraw-threshold", 2);
        if (data.getLives() <= threshold) {
            player.sendMessage(c("&cYou must keep at least &f" + threshold + " &clives!"));
            return false;
        }
        data.removeLife();
        player.sendMessage(c("&e-1 Life. You now have &f" + data.getLives() + " &elives."));
        updateAbsoluteHeart(player, data);
        return true;
    }

    public boolean onDeath(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data.getGrowthLevel() != 0) {
            data.setGrowthLevel(0);
        }
        if (data.getLives() <= 1) {
            player.sendMessage(c("&4&lYou have run out of lives!"));
            return true;
        }
        data.removeLife();
        player.sendMessage(c("&c&lYou died! &cLives remaining: &f" + data.getLives()));
        updateAbsoluteHeart(player, data);

        if (data.getLives() == 1 && !data.hasUnlocked(HeartType.ABSOLUTE)) {
            plugin.getUnlockManager().checkReachedOneLife(player);
        }
        return false;
    }

    public void updateAbsoluteHeart(Player player, PlayerData data) {
        if (!data.hasEquipped(HeartType.ABSOLUTE)) {
            player.setMaxHealth(20.0);
            return;
        }
        int lives = data.getLives();
        double hp = lives == 1
                ? plugin.getHeartManager().getMiscDouble(HeartType.ABSOLUTE, "hearts-at-one-life", 32.0)
                : lives == 2
                ? plugin.getHeartManager().getMiscDouble(HeartType.ABSOLUTE, "hearts-at-two-lives", 26.0)
                : 20.0;
        player.setMaxHealth(hp);
    }

    private Component c(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }
}
