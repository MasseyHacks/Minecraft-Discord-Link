package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DepositMultiplier implements CommandExecutor {
    private final MinecraftDiscordLink plugin;
    public DepositMultiplier(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender.hasPermission("masseyhacks.economy.changeMultiplier")){
            if(args.length == 0){
                sender.sendMessage("Current deposit multiplier: " + plugin.depositMultiplier);
            }
            else{
                try{
                    plugin.depositMultiplier = Double.parseDouble(args[0]);
                    sender.sendMessage("Successfully changed multiplier. Current deposit multiplier: " + plugin.depositMultiplier);
                } catch(NumberFormatException e){
                    sender.sendMessage("Invalid multiplier! Please ensure that it is a double!");
                }
            }
        }
        else{
            sender.sendMessage("You do not have permission to perform this action!");
        }
        return true;
    }
}
