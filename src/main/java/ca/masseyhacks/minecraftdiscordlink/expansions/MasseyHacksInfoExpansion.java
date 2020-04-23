package ca.masseyhacks.minecraftdiscordlink.expansions;

import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import ca.masseyhacks.minecraftdiscordlink.structures.ParticipantInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class MasseyHacksInfoExpansion extends PlaceholderExpansion {
    private final MinecraftDiscordLink plugin;

    public MasseyHacksInfoExpansion(MinecraftDiscordLink plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "masseyhacks";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if(player == null){
            return "";
        }

        switch (identifier) {
            case "discordTag":
                return plugin.placeholderInfoCache.getOrDefault(player.getUniqueId(), new ParticipantInfo()).getDiscordTag();
            case "eventBalance":
                return Double.toString(plugin.placeholderInfoCache.getOrDefault(player.getUniqueId(), new ParticipantInfo()).getBalance());
            case "totalBalance":
                double eventBalance = plugin.placeholderInfoCache.getOrDefault(player.getUniqueId(), new ParticipantInfo()).getBalance();
                double playerBalance = plugin.econ.getBalance(player);

                if (eventBalance != -1) {
                    return Double.toString(playerBalance + eventBalance);
                } else {
                    return Double.toString(playerBalance);
                }
        }

        return null;
    }
}
