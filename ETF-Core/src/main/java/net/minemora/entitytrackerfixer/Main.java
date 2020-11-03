package net.minemora.entitytrackerfixer;

import net.minemora.entitytrackerfixer.commands.EntityTrackerFixer;
import net.minemora.entitytrackerfixer.utilities.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;

public class Main extends JavaPlugin {
    public static Main pl;
    public BukkitScheduler bs = Bukkit.getScheduler();
    public PluginManager plm = Bukkit.getPluginManager();

    @Override
    public void onEnable() {
        pl = this;
        if (Reflection.getInstance().getNMS() == null) {
            getLogger().warning("We don't support your server version.");
            plm.disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        reloadConfig();
        Reflection.getInstance().getNMS().unTrackTask();
        Reflection.getInstance().getNMS().reTrackTask();
        getCommand("entitytrackerfixer").setExecutor(new EntityTrackerFixer());
    }

    public void stopTasks() {
        bs.cancelTasks(this);
    }

    public boolean tpsLimitReached(double limit) {
        if (limit > 20 || limit < 1) return false;
        return Reflection.getInstance().getTPS(0) > limit;
    }

    public boolean doWorldsContainGlobal(List<String> worldList) {
        return worldList.stream().anyMatch("*"::equals);
    }
}