package net.minemora.entitytrackerfixer.utilities;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public final class NMSUtilities {
    private static Method addEntityMethod;
    private static Method removeEntityMethod;
    private static Field trackerField;

    static {
        try {
            addEntityMethod = Reflection.getPrivateMethod(PlayerChunkMap.class, "addEntity", new Class[]{Entity.class});
            removeEntityMethod = Reflection.getPrivateMethod(PlayerChunkMap.class, "removeEntity", new Class[]{Entity.class});
            trackerField = Reflection.getClassPrivateField(PlayerChunkMap.EntityTracker.class, "tracker");
        } catch (IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void unTrackEntities(ChunkProviderServer chunkProviderServer, Set<Entity> entities) {
        try {
            for (Entity entity : entities) {
                removeEntityMethod.invoke(chunkProviderServer.playerChunkMap, entity);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void reTrackEntities(ChunkProviderServer chunkProviderServer, Set<Entity> entities) {
        try {
            for (Entity entity : entities) {
                if (chunkProviderServer.playerChunkMap.trackedEntities.containsKey(entity.getId())) {
                    continue;
                }
                addEntityMethod.invoke(chunkProviderServer.playerChunkMap, entity);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Field getTrackerField() {
        return trackerField;
    }

    public static ChunkProviderServer getChunkProvider(String worldName) {
        WorldServer worldServer = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        return worldServer.getChunkProvider();
    }

    public static Map<Integer, PlayerChunkMap.EntityTracker> getTrackedEntities(String worldName) {
        return getChunkProvider(worldName).playerChunkMap.trackedEntities;
    }

    public static double getTPS() {
        return MinecraftServer.getServer().recentTps[0];
    }
}