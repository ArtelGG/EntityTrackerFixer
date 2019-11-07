package net.minemora.entitytrackerfixer.commands;

import net.minemora.entitytrackerfixer.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EntityTrackerFixer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] arguments) {
        if (!commandSender.hasPermission("entitytrackerfixer.admin")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
            return true;
        }
        if (arguments.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + commandLabel + " <reload|debug>");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("reload")) {
            Main.plugin.stopTasks();
            Main.plugin.reloadConfig();
            Main.plugin.startUntrackTask(Main.plugin.getConfig().getInt("untrack-ticks"));
            Main.plugin.startRetrackTask(Main.plugin.getConfig().getInt("retrack-ticks"));
            commandSender.sendMessage(ChatColor.GREEN + "Successfully reloaded EntityTrackerFixer.");
        } else if (arguments[0].equalsIgnoreCase("debug")) {
            commandSender.sendMessage("TPS: " + String.format("%.2f", Main.plugin.getTPS()));
            commandSender.sendMessage("TPS limit: " + Main.plugin.getConfig().getDouble("tps-limit"));
            commandSender.sendMessage("Un-track ticks: " + Main.plugin.getConfig().getInt("untrack-ticks"));
            commandSender.sendMessage("Re-track ticks: " + Main.plugin.getConfig().getInt("retrack-ticks"));
            commandSender.sendMessage("Re-track range: " + Main.plugin.getConfig().getInt("retrack-range"));
            commandSender.sendMessage("Enabled worlds: " + Main.plugin.getConfig().getStringList("worlds"));
        } else {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + commandLabel + " <reload|debug>");
            return true;
        }
        return true;
    }
}