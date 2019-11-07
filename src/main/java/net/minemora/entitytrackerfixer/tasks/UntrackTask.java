package net.minemora.entitytrackerfixer.tasks;

import net.minecraft.server.v1_14_R1.ChunkProviderServer;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.NMSEntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class UntrackTask extends BukkitRunnable {
    static boolean running = false;

    @Override
    public void run() {
        if (tpsLimitReached(Main.plugin.getConfig().getDouble("tps-limit"))) {
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
        WorldServer worldServer = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        ChunkProviderServer chunkProvider = worldServer.getChunkProvider();
        try {
            for (EntityTracker entityTracker : chunkProvider.playerChunkMap.trackedEntities.values()) {
                net.minecraft.server.v1_14_R1.Entity entity = (net.minecraft.server.v1_14_R1.Entity) NMSEntityTracker.getTrackerField().get(entityTracker);
                if (entity instanceof EntityPlayer) {
                    continue;
                }
                if (entity.getBukkitEntity().getCustomName() != null) {
                    continue;
                }
                boolean remove = false;
                if (entityTracker.trackedPlayers.size() == 0) {
                    remove = true;
                } else if (entityTracker.trackedPlayers.size() == 1) {
                    for (EntityPlayer entityPlayer : entityTracker.trackedPlayers) {
                        if (!entityPlayer.getBukkitEntity().isOnline()) {
                            remove = true;
                        }
                    }
                    if (!remove) {
                        continue;
                    }
                }
                if (remove) {
                    toRemove.add(entity.getId());
                    removed++;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        for (int id : toRemove) {
            chunkProvider.playerChunkMap.trackedEntities.remove(id);
        }
        if (Main.plugin.getConfig().getBoolean("log-to-console") && removed > 0) {
            Main.plugin.getLogger().info("Un-tracked " + removed + " " + (removed == 1 ? "entity" : "entities") + " in " + worldName);
        }
    }

    private boolean tpsLimitReached(double limit) {
        if (limit > 20 || limit < 1) return false; // Allows the user to disable the TPS check entirely.
        return Main.plugin.getTPS() > limit;
    }
}