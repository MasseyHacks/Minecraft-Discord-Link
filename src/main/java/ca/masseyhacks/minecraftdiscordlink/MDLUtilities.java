package ca.masseyhacks.minecraftdiscordlink;

import ca.masseyhacks.minecraftdiscordlink.structures.ParticipantInfo;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class MDLUtilities {
    public static void createLink(MinecraftDiscordLink plugin, UUID mcUUID, String secret) throws SQLException {
        Statement stmt = plugin.connection.createStatement();
        String sql = "UPDATE MinecraftDiscordLink SET mcUUID='" +
                mcUUID.toString() +
                "' WHERE secret='" +
                secret +
                "'";
        stmt.execute(sql);
    }

    public static void deleteLink(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException {
        System.out.println(mcUUID);

        Statement stmt = plugin.connection.createStatement();
        String sql = "UPDATE MinecraftDiscordLink SET mcUUID=NULL WHERE mcUUID='" +
                mcUUID.toString() +
                "'";
        stmt.execute(sql);
    }

    public static String getTagFromSecret(MinecraftDiscordLink plugin, String secret) throws SQLException {
        String sql = "SELECT discordTag FROM MinecraftDiscordLink WHERE `secret` = '" + secret + "' AND (mcUUID='' OR mcUUID is NULL) LIMIT 1";
        Statement stmt = plugin.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            return rs.getString("discordTag");
        }
        return "";
    }

    public static String getTagFromPlayer(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException {
        String sql = "SELECT discordTag FROM MinecraftDiscordLink WHERE `mcUUID` = '" + mcUUID.toString() + "' LIMIT 1";
        Statement stmt = plugin.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            return rs.getString("discordTag");
        }
        return "";
    }

    public static String getIDFromPlayer(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException{
        String sql = "SELECT discordID FROM MinecraftDiscordLink WHERE `mcUUID` = '" + mcUUID.toString() + "' LIMIT 1";
        Statement stmt = plugin.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            return rs.getString("discordID");
        }
        return "";
    }

    public static boolean getPlayerLinked(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException{
        String sql = "SELECT EXISTS(SELECT * FROM MinecraftDiscordLink WHERE mcUUID='" + mcUUID.toString() + "')";
        Statement stmt = plugin.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()){
            return rs.getInt(0) == 1;
        }
        return false;
    }

    public static void exportPlayerBalance(MinecraftDiscordLink plugin, Player player) throws SQLException{
        String discordID = getIDFromPlayer(plugin, player.getUniqueId());

        if(discordID.length() == 0){
            getLogger().info("Player " + player.getName() + " has no Discord link.");
        }

        double balance = plugin.econ.getBalance(player);

        String sql = "UPDATE EventEconomy SET balance = balance + " + balance + " WHERE discordID='" + discordID + "'";

        getLogger().info(sql);

        Statement stmt = plugin.connection.createStatement();
        stmt.execute(sql);

        plugin.econ.withdrawPlayer(player, balance);
    }

    public static HashMap<UUID, ParticipantInfo> getAllLinks(MinecraftDiscordLink plugin) throws SQLException{
        String sql = "SELECT mcUUID,discordTag,discordID FROM MinecraftDiscordLink";

        Statement stmt = plugin.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        HashMap<UUID, ParticipantInfo> returnInfo = new HashMap<>();

        while(rs.next()){
            String mcUUID = rs.getString("mcUUID");
            String discordTag = rs.getString("discordTag");
            String discordID = rs.getString("discordID");

            // make sure we have info for every field before adding
            // players without info get default stuff, no need to add them here
            if(mcUUID != null && discordTag != null && discordID != null){
                ParticipantInfo temp = new ParticipantInfo();
                temp.setDiscordTag(discordTag);
                temp.setDiscordID(discordID);

                returnInfo.put(UUID.fromString(mcUUID), temp);
            }

        }

        return returnInfo;
    }

    public static HashMap<String, ParticipantInfo> getAllBalances(MinecraftDiscordLink plugin) throws SQLException{
        String sql = "SELECT balance, discordID FROM EventEconomy";

        Statement stmt = plugin.connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        HashMap<String, ParticipantInfo> returnInfo = new HashMap<>();

        while(rs.next()){
            String discordID = rs.getString("discordID");
            double balance = rs.getDouble("balance");

            if(discordID != null){
                ParticipantInfo temp = new ParticipantInfo();
                temp.setBalance(balance);
                returnInfo.put(discordID, temp);
            }

        }

        return returnInfo;
    }

    public static void updateCache(MinecraftDiscordLink plugin) throws SQLException{
        HashMap<UUID, ParticipantInfo> newLinks = getAllLinks(plugin);
        HashMap<String, ParticipantInfo> newBalances = getAllBalances(plugin);
        plugin.placeholderInfoCache.clear();

        // merge the who Participant infos to get all the information about the player
        for(UUID key:newLinks.keySet()){
            ParticipantInfo temp = new ParticipantInfo(newLinks.get(key),
                    newBalances.getOrDefault(newLinks.get(key).getDiscordID(), new ParticipantInfo())); // uses getOrDefault as there may have been an error setting up the balance account
            plugin.placeholderInfoCache.put(key, temp);
        }
    }
}
