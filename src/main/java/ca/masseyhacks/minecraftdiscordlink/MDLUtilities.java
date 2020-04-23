package ca.masseyhacks.minecraftdiscordlink;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.print.DocFlavor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.bukkit.Bukkit.getLogger;

public class MDLUtilities {
    public static void createLink(String mcUUID, String secret, Connection connection) throws SQLException {

        Statement stmt = connection.createStatement();
        String sql = "UPDATE MinecraftDiscordLink SET mcUUID='" +
                mcUUID +
                "' WHERE secret='" +
                secret +
                "'";
        stmt.execute(sql);
    }

    public static void deleteLink(String mcUUID, Connection connection) throws SQLException {
        System.out.println(mcUUID);

        Statement stmt = connection.createStatement();
        String sql = "UPDATE MinecraftDiscordLink SET mcUUID='' WHERE mcUUID='" +
                mcUUID +
                "'";
        stmt.execute(sql);
    }

    public static String getTagFromSecret(String secret, Connection connection) throws SQLException {
        String sql = "SELECT discordTag FROM MinecraftDiscordLink WHERE `secret` = '" + secret + "' AND (mcUUID='' OR mcUUID is NULL) LIMIT 1";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            return rs.getString("discordTag");
        }
        return "";
    }

    public static String getTagFromPlayer(String uuid, Connection connection) throws SQLException {
        String sql = "SELECT discordTag FROM MinecraftDiscordLink WHERE `mcUUID` = '" + uuid + "' LIMIT 1";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            return rs.getString("discordTag");
        }
        return "";
    }

    public static String getIDFromPlayer(String uuid, Connection connection) throws SQLException{
        String sql = "SELECT discordID FROM MinecraftDiscordLink WHERE `mcUUID` = '" + uuid + "' LIMIT 1";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            return rs.getString("discordID");
        }
        return "";
    }

    public static boolean getPlayerLinked(String uuid, Connection connection) throws SQLException{
        String sql = "SELECT EXISTS(SELECT * FROM MinecraftDiscordLink)";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()){
            return rs.getInt(0) == 1;
        }
        return false;
    }

    public static void exportPlayerBalance(Player player, Connection connection, Economy econ) throws SQLException{
        String discordID = getIDFromPlayer(player.getUniqueId().toString(), connection);

        if(discordID.length() == 0){
            getLogger().info("Player " + player.getName() + " has no Discord link.");
        }

        double balance = econ.getBalance(player);

        String sql = "UPDATE EventEconomy SET balance = balance + " + balance + " WHERE discordID='" + discordID + "'";

        getLogger().info(sql);

        Statement stmt = connection.createStatement();
        stmt.execute(sql);

        econ.withdrawPlayer(player, balance);
    }
}
