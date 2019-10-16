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
        if (UntrackTask.isRunning()) {
            return;
        }
        for (String worldName : Main.plugin.getConfig().getStringList("worlds")) {
            if (Bukkit.getWorld(worldName) == null) {
                continue;
            }
            checkWorld(worldName);
        }
    }

    private void checkWorld(String worldName) {
        WorldServer ws = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
        ChunkProviderServer cps = ws.getChunkProvider();
        Set<net.minecraft.server.v1_14_R1.Entity> trackAgain = new HashSet<>();
        int d = Main.plugin.getConfig().getInt("retrack-range");
        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            for (Entity ent : player.getNearbyEntities(d, d, d)) {
                if (!cps.playerChunkMap.trackedEntities.containsKey(ent.getEntityId())) {
                    trackAgain.add(((CraftEntity) ent).getHandle());
                }
            }
        }
        NMSEntityTracker.trackEntities(cps, trackAgain);
    }
}