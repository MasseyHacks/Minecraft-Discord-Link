package ca.masseyhacks.minecraftdiscordlink.tasks;

import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import ca.masseyhacks.minecraftdiscordlink.structures.LinkConfirmData;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;

public class MDLCleanup extends BukkitRunnable {

    @Override
    public void run() {
        //System.out.println("Running cleanup task");
        for(String key : MinecraftDiscordLink.confirmStatus.keySet()){
            LinkConfirmData confirmInfo = MinecraftDiscordLink.confirmStatus.getOrDefault(key, null);

            if(confirmInfo.timestamp != -1L){
                if(Instant.now().getEpochSecond() - confirmInfo.timestamp >= 30L){
                    MinecraftDiscordLink.confirmStatus.remove(key);
                }
            }

        }

        for(String key : MinecraftDiscordLink.confirmUnlinkStatus.keySet()){
            Long timestamp = MinecraftDiscordLink.confirmUnlinkStatus.getOrDefault(key, -1L);

            if(timestamp != -1L){
                if(Instant.now().getEpochSecond() - timestamp >= 30L){
                    MinecraftDiscordLink.confirmUnlinkStatus.remove(key);
                }
            }

        }
    }
}
