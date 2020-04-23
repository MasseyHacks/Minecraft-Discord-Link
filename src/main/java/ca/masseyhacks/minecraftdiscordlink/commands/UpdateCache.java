package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class UpdateCache implements CommandExecutor {
    private final MinecraftDiscordLink plugin;

    public UpdateCache(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("masseyhacks.updatecache")){
            try{
                MDLUtilities.updateCache(plugin);
                sender.sendMessage("Cache updated successfully.");
            } catch (SQLException e){
                e.printStackTrace();
                sender.sendMessage("There was an error updating the discord linkage cache.");
            }
        }
        return true;
    }
}
