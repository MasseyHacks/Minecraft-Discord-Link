package ca.masseyhacks.minecraftdiscordlink;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.print.DocFlavor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        String sql = "UPDATE MinecraftDiscordLink SET mcUUID='' WHERE mcUUID='" +
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
}
