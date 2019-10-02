package net.minemora.entitytrackerfixer.tasks;

import net.minecraft.server.v1_14_R1.ChunkProviderServer;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class UntrackTask extends BukkitRunnable {
    private static boolean running = false;
    private static Field trackerField;

    static {
        try {
            trackerField = Reflection.getClassPrivateField(EntityTracker.class, "tracker");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (MinecraftServer.getServer().recentTps[0] > Main.plugin.getConfig().getDouble("tps-limit")) {
            return;
        }
        running = true;
        for (String worldName : Main.plugin.getConfig().getStringList("worlds")) {
            untrackProcess(worldName);
        }
        running = false;
    }

    private void untrackProcess(String worldName) {
        if (Bukkit.getWorld(worldName) == null) {
            return;
        }
        Set<Integer> toRemove = new HashSet<>();
        int removed = 0;
        WorldServer ws = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        ChunkProviderServer cps = ws.getChunkProvider();
        try {
            for (EntityTracker et : cps.playerChunkMap.trackedEntities.values()) {
                net.minecraft.server.v1_14_R1.Entity nmsEnt = (net.minecraft.server.v1_14_R1.Entity) trackerField.get(et);
                if (nmsEnt instanceof EntityPlayer) {
                    continue;
                }
                if (nmsEnt.getBukkitEntity().getCustomName() != null) {
                    continue;
                }
                boolean remove = false;
                if (et.trackedPlayers.size() == 0) {
                    remove = true;
                } else if (et.trackedPlayers.size() == 1) {
                    for (EntityPlayer ep : et.trackedPlayers) {
                        if (!ep.getBukkitEntity().isOnline()) {
                            remove = true;
                        }
                    }
                    if (!remove) {
                        continue;
                    }
                }
                if (remove) {
                    toRemove.add(nmsEnt.getId());
                    removed++;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        for (int id : toRemove) {
            cps.playerChunkMap.trackedEntities.remove(id);
        }
        if (Main.plugin.getConfig().getBoolean("log-to-console") && removed > 0) {
            Main.plugin.getLogger().info("Un-tracked " + removed + " " + (removed == 1 ? "entity" : "entities") + " in " + worldName);
        }
    }

	public static boolean isRunning() {
		return running;
	}
}