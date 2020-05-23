package ca.masseyhacks.minecraftdiscordlink.events;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class OnConnect implements Listener {
    private final MinecraftDiscordLink plugin;
    public OnConnect(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onConnect(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            try{
                if(!MDLUtilities.getPlayerLinked(plugin, player.getUniqueId())){
                    player.sendMessage(" ");
                    player.spigot().sendMessage(
                            MDLUtilities.genCompletedTextComponentSet(
                                    MDLUtilities.genTextComponentColoured(ChatColor.RED, "Your Minecraft account is not currently linked to any Discord user."),
                                    MDLUtilities.genTextComponentColoured(ChatColor.WHITE, "All Activities points earned on this server will NOT count towards your main balance.")
                            ).toArray(new TextComponent[0])
                    );
                    player.sendMessage(" ");
                    player.spigot().sendMessage(
                            MDLUtilities.genCompletedTextComponentSet(
                                    MDLUtilities.genTextComponentColoured(ChatColor.GREEN, "Message"),
                                    MDLUtilities.genTextComponentColoured(ChatColor.DARK_PURPLE, "!linkmc"),
                                    MDLUtilities.genTextComponentColoured(ChatColor.GREEN, "to the Discord bot to link your accounts.")
                            ).toArray(new TextComponent[0])
                    );
                }
            }
            catch(SQLException e){
                e.printStackTrace();
                getLogger().warning("Unable to get player link status.");
            }
        }, 20L);


    }
}
