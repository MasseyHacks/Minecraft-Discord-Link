package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.Instant;

import static org.bukkit.Bukkit.getLogger;

public class UnlinkDiscord implements CommandExecutor {
    private final MinecraftDiscordLink plugin;
    public UnlinkDiscord(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {  // start the process, make the player confirm
                try {
                    String discordTagLinkTo = MDLUtilities.getTagFromPlayer(plugin, player.getUniqueId());

                    // make sure the Minecraft account is actually linked to something
                    if (discordTagLinkTo.length() == 0) {
                        player.sendMessage("Your Minecraft account is not linked to any Discord user.");
                        return true;
                    }

                    /*
                    TextComponent front = new TextComponent(ChatColor.DARK_RED + "You are about to unlink your Minecraft account from " + discordTagLinkTo + ". " + ChatColor.WHITE + "This will make all in-game currencies unavailable to the Discord user. Type ");

                    TextComponent cmdClick = new TextComponent(ChatColor.DARK_PURPLE + "/unlinkdiscord confirm" + ChatColor.WHITE);
                    cmdClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command into your command bar.").create()));
                    cmdClick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unlinkdiscord confirm"));

                    TextComponent back = new TextComponent(" within 30 seconds to confirm this action.");

                    player.spigot().sendMessage(front, cmdClick, back);
                    player.spigot().sendMessage();*/

                    player.spigot().sendMessage(
                            MDLUtilities.genCompletedTextComponentSet(
                                    MDLUtilities.genTextComponentColoured(ChatColor.DARK_RED, "You are about to unlink your Minecraft account from " + discordTagLinkTo + "."),
                                    MDLUtilities.genTextComponentColoured(ChatColor.WHITE, "This will make all in-game currencies unavailable to the Discord user. Type"),
                                    MDLUtilities.genTextCommandComponents(ChatColor.DARK_PURPLE, "/unlinkdiscord confirm", "/unlinkdiscord confirm"),
                                    MDLUtilities.genTextComponentColoured(ChatColor.WHITE, "within 30 seconds to confirm this action.")
                            ).toArray(new TextComponent[0])
                    );

                    plugin.confirmUnlinkStatus.put(player.getUniqueId(),
                            Instant.now().getEpochSecond()
                    );

                } catch (SQLException e) {
                    player.sendMessage("There was an error retrieving link information from the database. Please try again later.");
                    e.printStackTrace();
                    return true;
                }
            } else if(args.length == 1 && args[0].equals("confirm")){ // confirm stage, actual unlink here

                if (!plugin.confirmUnlinkStatus.containsKey(player.getUniqueId())) {
                    player.sendMessage("No confirmation token found! Try executing the unlink command again.");
                    return true;
                }

                try {
                    MDLUtilities.deleteLink(plugin, player.getUniqueId());

                    player.sendMessage(ChatColor.GREEN + "Successfully unlinked! " + ChatColor.WHITE +" If this was done in error, simply run /linkdiscord again with the same secret.");
                    plugin.confirmUnlinkStatus.remove(player.getUniqueId());

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
