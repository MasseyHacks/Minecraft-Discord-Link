package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ExportBalance implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;

            try {
                MDLUtilities.exportPlayerBalance(player);
                player.sendMessage("Your point balance of " + MinecraftDiscordLink.econ.getBalance(player) + " has been exported.");
            }
            catch(SQLException e){
                player.sendMessage("There was an issue updating our database. Your balance has not been exported.");
            }
            return true;
        }
        else {
            return false;
        }

    }
}
