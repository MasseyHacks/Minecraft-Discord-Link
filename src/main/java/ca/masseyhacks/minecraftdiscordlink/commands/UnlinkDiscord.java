package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
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
import java.time.Instant;

import static org.bukkit.Bukkit.getLogger;

public class UnlinkDiscord implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                try {
                    String discordTagLinkTo = MDLUtilities.getTagFromPlayer(player.getUniqueId().toString());

                    if (discordTagLinkTo.length() == 0) {
                        player.sendMessage("Your Minecraft account is not linked to any Discord user.");
                        return true;
                    }

                    TextComponent front = new TextComponent(ChatColor.DARK_RED + "You are about to unlink your Minecraft account from " + discordTagLinkTo + ". " + ChatColor.WHITE + "This will make all in-game currencies unavailable to the Discord user. Type ");

                    TextComponent cmdClick = new TextComponent(ChatColor.DARK_PURPLE + "/unlinkdiscord confirm" + ChatColor.WHITE);
                    cmdClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command into your command bar.").create()));
                    cmdClick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unlinkdiscord confirm"));

                    TextComponent back = new TextComponent(" within 30 seconds to confirm this action.");

                    player.spigot().sendMessage(front, cmdClick, back);

                    MinecraftDiscordLink.confirmUnlinkStatus.put(player.getUniqueId().toString(),
                            Instant.now().getEpochSecond()
                    );

                } catch (SQLException e) {
                    player.sendMessage("There was an error retrieving link information from the database. Please try again later.");
                    e.printStackTrace();
                    return true;
                }
            } else if(args.length == 1 && args[0].equals("confirm")){

                if (!MinecraftDiscordLink.confirmUnlinkStatus.containsKey(player.getUniqueId().toString())) {
                    player.sendMessage("No confirmation token found! Try executing the unlink command again.");
                    return true;
                }

                try {
                    MDLUtilities.deleteLink(player.getUniqueId().toString());

                    player.sendMessage(ChatColor.GREEN + "Successfully unlinked! " + ChatColor.WHITE +" If this was done in error, simply run /linkdiscord again with the same secret.");
                    MinecraftDiscordLink.confirmUnlinkStatus.remove(player.getUniqueId().toString());

                } catch (SQLException e) {
                    player.sendMessage("There was an error unlinking your account. Please contact a team member for assistance.");
                    e.printStackTrace();
                }

                return true;
            }
        } else {
            getLogger().info("You must be a player to use this command!");
        }
        return false;
    }
}