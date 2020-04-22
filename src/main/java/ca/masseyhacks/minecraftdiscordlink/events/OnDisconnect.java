package ca.masseyhacks.minecraftdiscordlink.events;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class OnDisconnect implements Listener {
    @EventHandler
    public void onDisconnect(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if(!MinecraftDiscordLink.perms.has(player, "masseyhacks.economy.keepBalance")){
            try{
                MDLUtilities.exportPlayerBalance(player);
            }
            catch (SQLException e){
                getLogger().warning("There was an error exporting a player's economy balance.");
                e.printStackTrace();
            }
        }
    }
}
