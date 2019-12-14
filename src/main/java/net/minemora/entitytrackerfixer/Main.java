package net.minemora.entitytrackerfixer;

import net.minemora.entitytrackerfixer.commands.EntityTrackerFixer;
import net.minemora.entitytrackerfixer.tasks.RetrackTask;
import net.minemora.entitytrackerfixer.tasks.UntrackTask;
import net.minemora.entitytrackerfixer.utilities.NMSUtilities;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        reloadConfig();
        getCommand("entitytrackerfixer").setExecutor(new EntityTrackerFixer());
        startUntrackTask(getConfig().getInt("untrack-ticks"));
        startRetrackTask(getConfig().getInt("retrack-ticks"));
    }

    // TODO: Add an option to make the tasks multi-threaded, because why not.
    public void startUntrackTask(int period) {
        Bukkit.getScheduler().runTaskTimer(this, UntrackTask.getInstance(), 0, period);
    }

    public void startRetrackTask(int period) {
        Bukkit.getScheduler().runTaskTimer(this, RetrackTask.getInstance(), 0, period);
    }

    public void stopTasks() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public boolean tpsLimitReached(double limit) {
        if (limit > 20 || limit < 1) return false;
        return NMSUtilities.getTPS() > limit;
    }
}