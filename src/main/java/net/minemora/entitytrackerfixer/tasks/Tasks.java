package net.minemora.entitytrackerfixer.tasks;

import net.minecraft.server.v1_14_R1.*;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.NMSUtilities;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Tasks {
    private boolean unTrackRunning, reTrackRunning = false;

    public static Tasks getInstance() {
        return new Tasks();
    }

    public void unTrackTask() {
        int period = Main.pl.getConfig().getInt("untrack-ticks");
        Main.pl.bs.runTaskTimer(Main.pl, new Runnable() {
            @Override
            public void run() {
                unTrackRunning = true;
                for (String worldName : Main.pl.getConfig().getStringList("worlds")) {
                    unTrackProcess(worldName);
                }
                unTrackRunning = false;
            }
        }, 0, period);
    }

    public void reTrackTask() {
        int period = Main.pl.getConfig().getInt("retrack-ticks");
        Main.pl.bs.runTaskTimer(Main.pl, new Runnable() {
            @Override
            public void run() {
                reTrackRunning = true;
                for (String worldName : Main.pl.getConfig().getStringList("worlds")) {
                    reTrackProcess(worldName);
                }
                reTrackRunning = false;
            }
        }, 0, period);
    }

    private void unTrackProcess(String worldName) {
        if (Main.pl.tpsLimitReached(Main.pl.getConfig().getDouble("tps-limit"))) {
            return;
        }
        if (reTrackRunning) {
            return;
        }
        if (Bukkit.getWorld(worldName) == null) {
            return;
        }
        Set<Integer> toRemove = new HashSet<>();
        int removed = 0;
        try {
            for (PlayerChunkMap.EntityTracker entityTracker : NMSUtilities.getTrackedEntities(worldName).values()) {
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
        if (Main.pl.getConfig().getBoolean("log-to-console") && removed > 0) {
            Main.pl.getLogger().info("Un-tracked " + removed + " " + (removed == 1 ? "entity" : "entities") + " in " + worldName);
        }
    }

    private void reTrackProcess(String worldName) {
        if (unTrackRunning) {
            return;
        }
        if (Bukkit.getWorld(worldName) == null) {
            return;
        }
        Set<net.minecraft.server.v1_14_R1.Entity> entities = new HashSet<>();
        int range = Main.pl.getConfig().getInt("retrack-range");
        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(range, range, range)) {
                if (!NMSUtilities.getTrackedEntities(worldName).containsKey(entity.getEntityId())) {
                    entities.add(((CraftEntity) entity).getHandle());
                }
            }
        }
        NMSUtilities.reTrackEntities(NMSUtilities.getChunkProvider(worldName), entities);
    }
}