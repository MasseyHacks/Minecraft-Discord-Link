package ca.masseyhacks.minecraftdiscordlink.commands;

import ca.masseyhacks.minecraftdiscordlink.MDLUtilities;
import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import ca.masseyhacks.minecraftdiscordlink.structures.LinkConfirmData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.sql.SQLException;
import java.time.Instant;

import static org.bukkit.Bukkit.getLogger;

public class LinkDiscord implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(args.length != 1){
                return false;
            }
            else {
                String secret = args[0];

                if(secret.equals("confirm")){
                    LinkConfirmData confirmInfo = MinecraftDiscordLink.confirmStatus.getOrDefault(player.getUniqueId().toString(), null);

                    if(confirmInfo == null){
                        sender.sendMessage("No confirmation token found! Try executing the link command again.");
                        return true;
                    }

                    try {
                        MDLUtilities.createLink(player.getUniqueId().toString(), confirmInfo.secret);
                        MinecraftDiscordLink.confirmStatus.remove(player.getUniqueId().toString());

                        TextComponent front = new TextComponent(ChatColor.GREEN + "Successfully linked! " + ChatColor.WHITE + "If this was in error, run ");

                        TextComponent cmdClick = new TextComponent(ChatColor.DARK_PURPLE + "/unlinkdiscord" + ChatColor.WHITE);
                        cmdClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command into your command bar.").create()));
                        cmdClick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unlinkdiscord"));

                        TextComponent back = new TextComponent(" to unlink your Minecraft account.");

                        player.spigot().sendMessage(front, cmdClick, back);


                    } catch(SQLException e){
                        player.sendMessage("There was an error linking your account. Please contact a team member for assistance.");
                    }

                    return true;
                }
                else {
                    try{
                        String discordTagFromPlayer = MDLUtilities.getTagFromPlayer(player.getUniqueId().toString());

                        if(discordTagFromPlayer.length() > 0){
                            TextComponent front = new TextComponent("You have already linked this account to " + discordTagFromPlayer + ". To unlink your Minecraft account, use ");

                            TextComponent cmdClick = new TextComponent(ChatColor.DARK_PURPLE + "/unlinkdiscord" + ChatColor.WHITE);
                            cmdClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command into your command bar.").create()));
                            cmdClick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unlinkdiscord"));

                            TextComponent back = new TextComponent(".");

                            player.spigot().sendMessage(front, cmdClick, back);
                            return true;
                        }

                        String discordTagLinkTo = MDLUtilities.getTagFromSecret(secret);

                        if(discordTagLinkTo.length() == 0){
                            player.sendMessage("No link request found with that secret. Make sure you have initiated a link through Discord and that the secret has not already been linked.");
                            return true;
                        }

                        TextComponent front = new TextComponent(ChatColor.DARK_RED + "You are about to link your Minecraft account to " + discordTagLinkTo + ". " + ChatColor.WHITE + "This will allow them to withdraw your in-game currency and associate themselves with you! Use ");

                        TextComponent cmdClick = new TextComponent(ChatColor.DARK_PURPLE + "/linkdiscord confirm" + ChatColor.WHITE);
                        cmdClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command into your command bar.").create()));
                        cmdClick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/linkdiscord confirm"));

                        TextComponent back = new TextComponent(" within 30 seconds to confirm this action.");

                        player.spigot().sendMessage(front, cmdClick, back);

                        MinecraftDiscordLink.confirmStatus.put(player.getUniqueId().toString(),
                                new LinkConfirmData(secret, Instant.now().getEpochSecond())
                        );

                    } catch (SQLException e){
                        player.sendMessage("There was an error retrieving link information from the database. Please try again later.");
                        e.printStackTrace();
                        return true;
                    }
                }

                return true;
            }
        }
        else{
            getLogger().info("You must be a player to use this command!");
        }
        return false;
    }
}
