package com.customhearts.commands;

import com.customhearts.CustomHeartsPlugin;
import com.customhearts.hearts.HeartType;
import com.customhearts.managers.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HeartCommand implements CommandExecutor, TabCompleter {

    private final CustomHeartsPlugin plugin;

    public HeartCommand(CustomHeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "give" -> cmdGive(sender, args);
            case "equip" -> cmdEquip(sender, args);
            case "unequip" -> cmdUnequip(sender, args);
            case "list" -> cmdList(sender, args);
            case "listall" -> cmdListAll(sender);
            case "info" -> cmdInfo(sender, args);
            case "progress" -> cmdProgress(sender, args);
            case "reload" -> cmdReload(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void cmdGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customhearts.admin")) { noPerms(sender); return; }
        if (args.length < 3) { msg(sender, "&cUsage: /heart give <player> <heartType>"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { msg(sender, "&cPlayer not found."); return; }
        HeartType type;
        try { type = HeartType.valueOf(args[2].toUpperCase()); }
        catch (Exception e) { msg(sender, "&cUnknown heart type: &f" + args[2]); return; }

        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        data.unlockHeart(type);
        target.getInventory().addItem(plugin.getHeartManager().createHeartItem(type));
        msg(sender, "&aGave " + plugin.getHeartManager().getDisplayName(type) + " &ato &f" + target.getName());
        msg(target, "&aYou received: " + plugin.getHeartManager().getDisplayName(type));
        msg(target, "&7Use &f/heart equip " + type.name().toLowerCase() + " &7to equip it.");
    }

    private void cmdEquip(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) { msg(sender, "&cPlayers only."); return; }
        if (args.length < 2) { msg(sender, "&cUsage: /heart equip <heartType>"); return; }
        HeartType type;
        try { type = HeartType.valueOf(args[1].toUpperCase()); }
        catch (Exception e) { msg(sender, "&cUnknown heart type."); return; }

        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.hasUnlocked(type)) { msg(sender, "&cYou haven't unlocked that heart yet!"); return; }
        int maxSlots = plugin.getConfig().getInt("settings.max-heart-slots", 3);
        if (data.hasEquipped(type)) { msg(sender, "&cAlready equipped."); return; }
        if (!data.equipHeart(type, maxSlots)) {
            msg(sender, "&cHeart slots full! (&f" + maxSlots + " max&c). Unequip one first.");
            return;
        }
        msg(sender, "&aEquipped " + plugin.getHeartManager().getDisplayName(type)
                + " &a(" + data.getEquippedHearts().size() + "/" + maxSlots + " slots)");
        plugin.getLivesManager().updateAbsoluteHeart(p, data);
    }

    private void cmdUnequip(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) { msg(sender, "&cPlayers only."); return; }
        if (args.length < 2) { msg(sender, "&cUsage: /heart unequip <heartType>"); return; }
        HeartType type;
        try { type = HeartType.valueOf(args[1].toUpperCase()); }
        catch (Exception e) { msg(sender, "&cUnknown heart type."); return; }

        PlayerData data = plugin.getPlayerDataManager().get(p.getUniqueId());
        if (!data.unequipHeart(type)) { msg(sender, "&cYou don't have that equipped."); return; }
        msg(sender, "&aUnequipped " + plugin.getHeartManager().getDisplayName(type));
        plugin.getLivesManager().updateAbsoluteHeart(p, data);
    }

    private void cmdList(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 1);
        if (target == null) return;
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        int maxSlots = plugin.getConfig().getInt("settings.max-heart-slots", 3);

        msg(sender, "&7--- &f" + target.getName() + "'s Hearts &7---");
        msg(sender, "&7Lives: &f" + data.getLives() + " &7| Slots: &f"
                + data.getEquippedHearts().size() + "/" + maxSlots);
        msg(sender, "&aEquipped:");
        if (data.getEquippedHearts().isEmpty()) msg(sender, "  &8None");
        else data.getEquippedHearts().forEach(h ->
                msg(sender, "  &a✓ " + plugin.getHeartManager().getDisplayName(h) + " &7(&f" + h.name().toLowerCase() + "&7)"));

        msg(sender, "&eUnlocked (not equipped):");
        boolean any = false;
        for (HeartType h : data.getUnlockedHearts()) {
            if (!data.hasEquipped(h)) {
                msg(sender, "  &e◆ " + plugin.getHeartManager().getDisplayName(h) + " &7(&f" + h.name().toLowerCase() + "&7)");
                any = true;
            }
        }
        if (!any) msg(sender, "  &8None");
    }

    private void cmdListAll(CommandSender sender) {
        msg(sender, "&7--- All Heart Types ---");
        for (HeartType t : HeartType.values()) {
            msg(sender, "  &f" + t.name().toLowerCase() + " &7-> " + plugin.getHeartManager().getDisplayName(t));
        }
    }

    private void cmdInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { msg(sender, "&cUsage: /heart info <heartType>"); return; }
        HeartType type;
        try { type = HeartType.valueOf(args[1].toUpperCase()); }
        catch (Exception e) { msg(sender, "&cUnknown heart type."); return; }
        msg(sender, "&7--- " + plugin.getHeartManager().getDisplayName(type) + " &7---");
        msg(sender, "&7ID: &f" + type.name().toLowerCase());
    }

    private void cmdProgress(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 1);
        if (target == null) return;
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        msg(sender, "&7--- " + target.getName() + " Progress ---");
        msg(sender, "&7Lives: &f" + data.getLives());
        msg(sender, "&7Player kills: &f" + data.getPlayerKills());
        msg(sender, "&7Blocks swum: &f" + data.getBlocksSwum());
        msg(sender, "&7Ores mined: &f" + data.getOresMined());
        msg(sender, "&7Ice mined: &f" + data.getIceMined());
        msg(sender, "&7Seeds planted: &f" + data.getSeedsPlanted());
        msg(sender, "&7Mob kills:");
        if (data.getMobKillMap().isEmpty()) msg(sender, "  &8None");
        else data.getMobKillMap().forEach((mob, count) -> msg(sender, "  &f" + mob + " x" + count));
    }

    private void cmdReload(CommandSender sender) {
        if (!sender.hasPermission("customhearts.admin")) { noPerms(sender); return; }
        plugin.reloadConfig();
        plugin.getHeartManager().loadHearts();
        msg(sender, "&aReloaded!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1)
            return filter(List.of("give","equip","unequip","list","listall","info","progress","reload"), args[0]);
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "give" -> Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                case "equip","unequip","info" -> heartIds(args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) return heartIds(args[2]);
        return List.of();
    }

    private List<String> heartIds(String partial) {
        return Arrays.stream(HeartType.values()).map(t -> t.name().toLowerCase())
                .filter(s -> s.startsWith(partial.toLowerCase())).collect(Collectors.toList());
    }

    private Player resolveTarget(CommandSender sender, String[] args, int idx) {
        if (args.length > idx && sender.hasPermission("customhearts.admin")) {
            Player t = Bukkit.getPlayerExact(args[idx]);
            if (t == null) { msg(sender, "&cPlayer not found."); return null; }
            return t;
        }
        if (sender instanceof Player p) return p;
        msg(sender, "&cSpecify a player.");
        return null;
    }

    private void sendHelp(CommandSender s) {
        msg(s, "&7/heart give <player> <type> &8- Give heart");
        msg(s, "&7/heart equip <type> &8- Equip a heart");
        msg(s, "&7/heart unequip <type> &8- Unequip a heart");
        msg(s, "&7/heart list [player] &8- View hearts");
        msg(s, "&7/heart listall &8- List all heart types");
        msg(s, "&7/heart progress [player] &8- View unlock progress");
    }

    private void msg(CommandSender s, String text) { s.sendMessage(c(text)); }
    private void noPerms(CommandSender s) { msg(s, "&cNo permission."); }
    private List<String> filter(List<String> l, String p) {
        return l.stream().filter(s -> s.startsWith(p.toLowerCase())).collect(Collectors.toList());
    }
    private Component c(String s) { return LegacyComponentSerializer.legacyAmpersand().deserialize(s); }
}
