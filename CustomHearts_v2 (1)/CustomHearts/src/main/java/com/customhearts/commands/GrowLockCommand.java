package com.customhearts.commands;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.managers.PlayerData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GrowLockCommand implements CommandExecutor {

    private final CustomHeartsPlugin plugin;

    public GrowLockCommand(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        boolean lock = label.equalsIgnoreCase("growlock");
        data.setGrowLocked(lock);

        p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                lock ? "&aGrowing Heart height is now &lLOCKED&a."
                     : "&aGrowing Heart height is now &lUNLOCKED&a."
        ));
        return true;
    }
}
