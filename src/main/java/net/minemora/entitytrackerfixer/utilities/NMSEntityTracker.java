package net.minemora.entitytrackerfixer.utilities;

import net.minecraft.server.v1_14_R1.ChunkProviderServer;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public final class NMSEntityTracker {
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

    public static void retrackEntities(ChunkProviderServer chunkProviderServer, Set<Entity> entities) {
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

    public static void untrackEntities(ChunkProviderServer chunkProviderServer, Set<Entity> entities) {
        try {
            for (Entity entity : entities) {
                removeEntityMethod.invoke(chunkProviderServer.playerChunkMap, entity);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Field getTrackerField() {
        return trackerField;
    }
}