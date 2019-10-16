package net.minemora.entitytrackerfixer;

import net.minemora.entitytrackerfixer.commands.EntityTrackerFixer;
import net.minemora.entitytrackerfixer.tasks.RetrackTask;
import net.minemora.entitytrackerfixer.tasks.UntrackTask;
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

    public void startUntrackTask(int period) {
        new UntrackTask().runTaskTimer(this, 0, period);
    }

    public void startRetrackTask(int period) {
        new RetrackTask().runTaskTimer(this, 0, period);
    }

    public void stopTasks() {
        Bukkit.getScheduler().cancelTasks(this);
    }
}