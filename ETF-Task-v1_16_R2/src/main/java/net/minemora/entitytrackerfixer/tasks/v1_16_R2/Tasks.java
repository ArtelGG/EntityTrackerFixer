package net.minemora.entitytrackerfixer.tasks.v1_16_R2;

import net.minecraft.server.v1_16_R2.*;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.nms.NMS;
import net.minemora.entitytrackerfixer.utilities.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tasks implements NMS {
    private final Method addEntityMethod = Reflection.getInstance().getPrivateMethod(PlayerChunkMap.class, "addEntity", new Class[]{Entity.class});
    private final Method removeEntityMethod = Reflection.getInstance().getPrivateMethod(PlayerChunkMap.class, "removeEntity", new Class[]{Entity.class});
    private final Field trackerField = Reflection.getInstance().getClassPrivateField(PlayerChunkMap.EntityTracker.class, "tracker");
    private boolean unTrackRunning, reTrackRunning = false;

    @Override
    public void unTrackTask() {
        int period = Main.pl.getConfig().getInt("untrack-ticks");
        Main.pl.bs.runTaskTimer(Main.pl, () -> {
            unTrackRunning = true;
            if (Main.pl.doWorldsContainGlobal(Main.pl.getConfig().getStringList("worlds"))) {
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    unTrackProcess(world.getName());
                }
            } else {
                for (String worldName : Main.pl.getConfig().getStringList("worlds")) {
                    unTrackProcess(worldName);
                }
            }
            unTrackRunning = false;
        }, 0, period);
    }

    @Override
    public void reTrackTask() {
        int period = Main.pl.getConfig().getInt("retrack-ticks");
        Main.pl.bs.runTaskTimer(Main.pl, () -> {
            reTrackRunning = true;
            if (Main.pl.doWorldsContainGlobal(Main.pl.getConfig().getStringList("worlds"))) {
                for (World world : Bukkit.getWorlds()) {
                    reTrackProcess(world.getName());
                }
            } else {
                for (String worldName : Main.pl.getConfig().getStringList("worlds")) {
                    reTrackProcess(worldName);
                }
            }
            reTrackRunning = false;
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
            for (PlayerChunkMap.EntityTracker entityTracker : getTrackedEntities(worldName).values()) {
                Entity entity = (Entity) getTrackerField().get(entityTracker);
                if (entity instanceof EntityPlayer || entity instanceof EntityWither || entity instanceof EntityEnderDragon || entity instanceof EntityComplexPart) {
                    continue;
                }
                if (entity.getBukkitEntity().getCustomName() != null) {
                    continue;
                }
                if (!entity.valid) {
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
            getTrackedEntities(worldName).remove(id);
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
        Set<net.minecraft.server.v1_16_R2.Entity> entities = new HashSet<>();
        int counter = 0;
        int range = Main.pl.getConfig().getInt("retrack-range");
        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(range, range, range)) {
                if (!getTrackedEntities(worldName).containsKey(entity.getEntityId())) {
                    entities.add(((CraftEntity) entity).getHandle());
                    counter++;
                }
            }
        }
        reTrackEntities(getChunkProvider(worldName), entities);
        if (Main.pl.getConfig().getBoolean("log-to-console") && counter > 0) {
            Main.pl.getLogger().info("Re-tracked " + counter + " " + (counter == 1 ? "entity" : "entities") + " in " + worldName);
        }
    }

    public void unTrackEntities(ChunkProviderServer chunkProviderServer, Set<Entity> entities) {
        try {
            for (Entity entity : entities) {
                removeEntityMethod.invoke(chunkProviderServer.playerChunkMap, entity);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void reTrackEntities(ChunkProviderServer chunkProviderServer, Set<Entity> entities) {
        try {
            for (Entity entity : entities) {
                if (chunkProviderServer.playerChunkMap.trackedEntities.containsKey(entity.getId()) || !entity.valid) {
                    continue;
                }
                addEntityMethod.invoke(chunkProviderServer.playerChunkMap, entity);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Field getTrackerField() {
        return trackerField;
    }

    public ChunkProviderServer getChunkProvider(String worldName) {
        WorldServer worldServer = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        return worldServer.getChunkProvider();
    }

    public Map<Integer, PlayerChunkMap.EntityTracker> getTrackedEntities(String worldName) {
        return getChunkProvider(worldName).playerChunkMap.trackedEntities;
    }
}