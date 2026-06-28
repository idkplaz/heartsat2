package com.customhearts.commands;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.managers.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class LivesCommand implements CommandExecutor, TabCompleter {

    private final CustomHeartsPlugin plugin;

    public LivesCommand(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            // Show own lives
            if (!(sender instanceof Player p)) { msg(sender, "&cPlayers only (or specify: /lives check <player>)"); return true; }
            PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
            msg(sender, "&7Lives: &f" + data.getLives() + " &7/ &f"
                    + plugin.getConfig().getInt("settings.max-lives", 5));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "check" -> {
                Player target = args.length >= 2 ? Bukkit.getPlayerExact(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) { msg(sender, "&cPlayer not found."); return true; }
                PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
                msg(sender, "&f" + target.getName() + " &7has &f" + data.getLives() + " &7lives.");
            }
            case "withdraw" -> {
                if (!(sender instanceof Player p)) { msg(sender, "&cPlayers only."); return true; }
                plugin.getLivesManager().withdrawLife(p);
            }
            case "set" -> {
                if (!sender.hasPermission("customhearts.admin")) { msg(sender, "&cNo permission."); return true; }
                if (args.length < 3) { msg(sender, "&cUsage: /lives set <player> <amount>"); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { msg(sender, "&cPlayer not found."); return true; }
                try {
                    int amount = Integer.parseInt(args[2]);
                    int max = plugin.getConfig().getInt("settings.max-lives", 5);
                    amount = Math.max(1, Math.min(max, amount));
                    PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
                    data.setLives(amount);
                    msg(sender, "&aSet &f" + target.getName() + "'s &alives to &f" + amount);
                    msg(target, "&7Your lives have been set to &f" + amount);
                    plugin.getLivesManager().updateAbsoluteHeart(target, data);
                } catch (NumberFormatException e) {
                    msg(sender, "&cInvalid number.");
                }
            }
            case "add" -> {
                if (!sender.hasPermission("customhearts.admin")) { msg(sender, "&cNo permission."); return true; }
                if (args.length < 2) { msg(sender, "&cUsage: /lives add <player>"); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { msg(sender, "&cPlayer not found."); return true; }
                plugin.getLivesManager().giveLife(target);
                msg(sender, "&aAdded a life to &f" + target.getName());
            }
            default -> {
                msg(sender, "&7/lives &8- Check your lives");
                msg(sender, "&7/lives check <player> &8- Check another player");
                msg(sender, "&7/lives withdraw &8- Spend a life (min 2 remaining)");
                msg(sender, "&7/lives set <player> <amount> &8- Admin set lives");
                msg(sender, "&7/lives add <player> &8- Admin add a life");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return List.of("check","withdraw","set","add").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2 && (args[0].equals("check") || args[0].equals("set") || args[0].equals("add")))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        return List.of();
    }

    private void msg(CommandSender s, String text) {
        s.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
    }
}
