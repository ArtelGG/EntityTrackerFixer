package net.minemora.entitytrackerfixer.commands;

import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EntityTrackerFixer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (arguments.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " <reload|debug>");
        } else if (arguments[0].equalsIgnoreCase("reload")) {
            Main.pl.stopTasks();
            Main.pl.reloadConfig();
            Main.pl.startTasks();
            commandSender.sendMessage(ChatColor.GREEN + "Successfully reloaded EntityTrackerFixer.");
        } else if (arguments[0].equalsIgnoreCase("debug")) {
            commandSender.sendMessage("NMS: " + Reflection.getInstance().getServerVersion());
            commandSender.sendMessage("TPS: " + String.format("%.2f", Reflection.getInstance().getTPS(0)));
            commandSender.sendMessage("TPS limit: " + ((Main.pl.getConfig().getDouble("tps-limit") > 20) ? "Disabled" : String.valueOf(Main.pl.getConfig().getDouble("tps-limit"))));
            commandSender.sendMessage("Un-track ticks: " + Main.pl.getConfig().getInt("untrack-ticks"));
            commandSender.sendMessage("Re-track ticks: " + Main.pl.getConfig().getInt("retrack-ticks"));
            commandSender.sendMessage("Re-track range: " + Main.pl.getConfig().getInt("retrack-range"));
            commandSender.sendMessage("Enabled worlds: " + ((Main.pl.doWorldsContainGlobal(Main.pl.getConfig().getStringList("worlds"))) ? "Global" : Main.pl.getConfig().getStringList("worlds").toString()));
        } else {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " <reload|debug>");
        }
        return true;
    }
}