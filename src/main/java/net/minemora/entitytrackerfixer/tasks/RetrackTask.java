package net.minemora.entitytrackerfixer.tasks;

import net.minecraft.server.v1_14_R1.ChunkProviderServer;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.NMSEntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class RetrackTask extends BukkitRunnable {

    @Override
    public void run() {
        if (UntrackTask.running) {
            return;
        }
        for (String worldName : Main.plugin.getConfig().getStringList("worlds")) {
            if (Bukkit.getWorld(worldName) == null) {
                continue;
            }
            retrackProcess(worldName);
        }
    }

    private void retrackProcess(String worldName) {
        WorldServer worldServer = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        ChunkProviderServer chunkProvider = worldServer.getChunkProvider();
        Set<net.minecraft.server.v1_14_R1.Entity> entities = new HashSet<>();
        int range = Main.plugin.getConfig().getInt("retrack-range");
        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                if (!chunkProvider.playerChunkMap.trackedEntities.containsKey(entity.getEntityId())) {
                    entities.add(((CraftEntity) entity).getHandle());
                }
            }
        }
        NMSEntityTracker.retrackEntities(chunkProvider, entities);
    }
}