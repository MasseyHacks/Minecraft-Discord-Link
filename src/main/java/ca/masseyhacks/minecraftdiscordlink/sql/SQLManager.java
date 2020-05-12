package ca.masseyhacks.minecraftdiscordlink.sql;

import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import ca.masseyhacks.minecraftdiscordlink.structures.ParticipantInfo;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class SQLManager {

    private final MinecraftDiscordLink plugin;
    private final ConnectionPoolManager pool;

    public SQLManager(MinecraftDiscordLink plugin) {
        this.plugin = plugin;
        pool = new ConnectionPoolManager(plugin);
        makeTable();
    }

    private void makeTable() {
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + "MinecraftDiscordLink"
                            + "  (mcUUID           VARCHAR(36),"
                            + "   discordID            VARCHAR(20),"
                            + "   discordTag          VARCHAR(40),"
                            + "   secret           VARCHAR(36), PRIMARY KEY (`secret`))"
            );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().severe("Exception in SQL: \n"+ ex);
        }
    }

    public void createLink(MinecraftDiscordLink plugin, UUID mcUUID, String secret) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE MinecraftDiscordLink SET mcUUID='" +
                            mcUUID.toString() +
                            "' WHERE secret='" +
                            secret +
                            "'"
            );
            ps.executeUpdate();
        }
    }

    public void deleteLink(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE MinecraftDiscordLink SET mcUUID=NULL WHERE mcUUID='" +
                            mcUUID.toString() +
                            "'"
            );
            ps.executeUpdate();
        }
    }

    public String getTagFromSecret(MinecraftDiscordLink plugin, String secret) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT discordTag FROM MinecraftDiscordLink WHERE `secret` = '" + secret + "' AND (mcUUID='' OR mcUUID is NULL) LIMIT 1"
            );
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return rs.getString("discordTag");
            }
            return "";
        }

    }

    public String getTagFromPlayer(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT discordTag FROM MinecraftDiscordLink WHERE `mcUUID` = '" + mcUUID.toString() + "' LIMIT 1"
            );
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return rs.getString("discordTag");
            }
            return "";
        }

    }

    public String getIDFromPlayer(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT discordID FROM MinecraftDiscordLink WHERE `mcUUID` = '" + mcUUID.toString() + "' LIMIT 1"
            );
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return rs.getString("discordID");
            }

            return "";

        }
    }

    public boolean getPlayerLinked(MinecraftDiscordLink plugin, UUID mcUUID) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT EXISTS(SELECT * FROM MinecraftDiscordLink WHERE mcUUID='" + mcUUID.toString() + "')"
            );
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                return rs.getInt(0) == 1;
            }
            return false;

        }
    }

    public void exportPlayerBalance(MinecraftDiscordLink plugin, Player player) throws SQLException{
        try (Connection conn = pool.getConnection()){
            String discordID = getIDFromPlayer(plugin, player.getUniqueId());

            if(discordID.length() == 0){
                getLogger().info("Player " + player.getName() + " has no Discord link.");
            }

            double balance = plugin.econ.getBalance(player);

            double finalExport = balance * plugin.depositMultiplier;

            //plugin.getLogger().info("UPDATE EventEconomy SET balance = balance + " + finalExport + " WHERE discordID='" + discordID + "'");

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE EventEconomy SET balance = balance + " + finalExport + " WHERE discordID='" + discordID + "'"
            );
            ps.executeUpdate();

            plugin.econ.withdrawPlayer(player, balance);
        }
    }

    public HashMap<UUID, ParticipantInfo> getAllLinks(MinecraftDiscordLink plugin) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT mcUUID,discordTag,discordID FROM MinecraftDiscordLink"
            );
            ResultSet rs = ps.executeQuery();

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
    }

    public HashMap<String, ParticipantInfo> getAllBalances(MinecraftDiscordLink plugin) throws SQLException{
        try (Connection conn = pool.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance, discordID FROM EventEconomy"
            );
            ResultSet rs = ps.executeQuery();

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
    }

    public void onDisable() {
        pool.closePool();
    }

}