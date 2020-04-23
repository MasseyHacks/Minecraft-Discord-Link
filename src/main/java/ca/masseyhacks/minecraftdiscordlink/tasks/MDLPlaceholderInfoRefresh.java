package ca.masseyhacks.minecraftdiscordlink.tasks;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;


public class MDLPlaceholderInfoRefresh extends BukkitRunnable {
    private final MinecraftDiscordLink plugin;

    public MDLPlaceholderInfoRefresh(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try{
            MDLUtilities.updateCache(plugin);
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}
