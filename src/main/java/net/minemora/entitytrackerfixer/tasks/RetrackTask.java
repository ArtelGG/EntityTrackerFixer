package net.minemora.entitytrackerfixer.tasks;

import net.minemora.entitytrackerfixer.Main;
import net.minemora.entitytrackerfixer.utilities.NMSUtilities;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class RetrackTask implements Runnable {
    public boolean running = false;

    public static RetrackTask getInstance() {
        return new RetrackTask();
    }

    @Override
    public void run() {
        for (String worldName : Main.plugin.getConfig().getStringList("worlds")) {
            retrackProcess(worldName);
        }
    }

    private void retrackProcess(String worldName) {
        if (UntrackTask.getInstance().running) {
            return;
        }
        if (Bukkit.getWorld(worldName) == null) {
            return;
        }
        Set<net.minecraft.server.v1_14_R1.Entity> entities = new HashSet<>();
        int range = Main.plugin.getConfig().getInt("retrack-range");
        for (Player player : Bukkit.getWorld(worldName).getPlayers()) {
            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                if (!NMSUtilities.getTrackedEntities(worldName).containsKey(entity.getEntityId())) {
                    entities.add(((CraftEntity) entity).getHandle());
                }
            }
        }
        NMSUtilities.retrackEntities(NMSUtilities.getChunkProvider(worldName), entities);
    }
}