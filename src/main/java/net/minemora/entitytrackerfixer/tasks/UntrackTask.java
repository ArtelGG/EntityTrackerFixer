package net.minemora.entitytrackerfixer.tasks;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityEnderDragon;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityWither;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.NMSUtilities;
import org.bukkit.Bukkit;

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
        Set<Integer> toRemove = new HashSet<>();
        int removed = 0;
        try {
            for (EntityTracker entityTracker : NMSUtilities.getTrackedEntities(worldName).values()) {
                Entity entity = (Entity) NMSUtilities.getTrackerField().get(entityTracker);
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
            NMSUtilities.getTrackedEntities(worldName).remove(id);
        }
        if (Main.plugin.getConfig().getBoolean("log-to-console") && removed > 0) {
            Main.plugin.getLogger().info("Un-tracked " + removed + " " + (removed == 1 ? "entity" : "entities") + " in " + worldName);
        }
    }
}