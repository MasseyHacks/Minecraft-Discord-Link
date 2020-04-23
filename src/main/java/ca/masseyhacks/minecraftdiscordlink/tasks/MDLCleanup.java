package ca.masseyhacks.minecraftdiscordlink.tasks;

import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import ca.masseyhacks.minecraftdiscordlink.structures.LinkConfirmData;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.UUID;

public class MDLCleanup extends BukkitRunnable {
    private final MinecraftDiscordLink plugin;

    public MDLCleanup(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        //System.out.println("Running cleanup task");
        for(UUID key : plugin.confirmStatus.keySet()){
            LinkConfirmData confirmInfo = plugin.confirmStatus.getOrDefault(key, null);

            if(confirmInfo.timestamp != -1L){
                if(Instant.now().getEpochSecond() - confirmInfo.timestamp >= 30L){
                    plugin.confirmStatus.remove(key);
                }
            }

        }

        for(UUID key : plugin.confirmUnlinkStatus.keySet()){
            Long timestamp = plugin.confirmUnlinkStatus.getOrDefault(key, -1L);

            if(timestamp != -1L){
                if(Instant.now().getEpochSecond() - timestamp >= 30L){
                    plugin.confirmUnlinkStatus.remove(key);
                }
            }

        }
    }
}
