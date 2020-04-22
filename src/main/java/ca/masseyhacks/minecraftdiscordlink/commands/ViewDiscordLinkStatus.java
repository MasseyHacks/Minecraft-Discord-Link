package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class ViewDiscordLinkStatus implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            try{
                String discordTag = MDLUtilities.getTagFromPlayer(player.getUniqueId().toString());

                if(discordTag.length() > 0){
                    TextComponent front = new TextComponent(player.getName() + " is linked to " + discordTag + ". To unlink your Minecraft account, use ");

                    TextComponent cmdClick = new TextComponent(ChatColor.DARK_PURPLE + "/unlinkdiscord" + ChatColor.WHITE);
                    cmdClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command into your command bar.").create()));
                    cmdClick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unlinkdiscord"));

                    TextComponent back = new TextComponent(".");

                    player.spigot().sendMessage(front, cmdClick, back);

                }
                else{
                    player.sendMessage("No Discord user linked to " + player.getName());
                }
            } catch (SQLException e){
                player.sendMessage("Unable to fetch link status.");
                e.printStackTrace();
            }
            return true;
        }
        else{
            getLogger().info("You must be a player to use this command!");
        }
        return false;
    }
}
