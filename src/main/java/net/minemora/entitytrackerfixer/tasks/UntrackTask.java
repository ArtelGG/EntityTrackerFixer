package net.minemora.entitytrackerfixer.tasks;

import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.NMSEntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

import java.util.HashSet;
import java.util.Set;

public class UntrackTask implements Runnable {
    public boolean running = false;

    public static UntrackTask getInstance() {
        return new UntrackTask();
    }

    @Override
    public void run() {
        running = true;
        for (String worldName : Main.plugin.getConfig().getStringList("worlds")) {
            untrackProcess(worldName);
        }
        running = false;
    }

    private void untrackProcess(String worldName) {
        if (Main.plugin.tpsLimitReached(Main.plugin.getConfig().getDouble("tps-limit"))) {
            return;
        }
        if (RetrackTask.getInstance().running) {
            return;
        }
        if (Bukkit.getWorld(worldName) == null) {
            return;
        }
        WorldServer worldServer = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        ChunkProviderServer chunkProvider = worldServer.getChunkProvider();
        Set<Integer> toRemove = new HashSet<>();
        int removed = 0;
        try {
            for (EntityTracker entityTracker : chunkProvider.playerChunkMap.trackedEntities.values()) {
                Entity entity = (Entity) NMSEntityTracker.getTrackerField().get(entityTracker);
                if (entity instanceof EntityPlayer || entity instanceof EntityWither || entity instanceof EntityEnderDragon) {
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
}