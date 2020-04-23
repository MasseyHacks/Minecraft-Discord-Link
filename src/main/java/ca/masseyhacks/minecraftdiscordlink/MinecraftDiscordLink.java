package ca.masseyhacks.minecraftdiscordlink;

import ca.masseyhacks.minecraftdiscordlink.commands.ExportBalance;
import ca.masseyhacks.minecraftdiscordlink.commands.LinkDiscord;
import ca.masseyhacks.minecraftdiscordlink.commands.UnlinkDiscord;
import ca.masseyhacks.minecraftdiscordlink.commands.ViewDiscordLinkStatus;
import ca.masseyhacks.minecraftdiscordlink.events.OnDisconnect;
import ca.masseyhacks.minecraftdiscordlink.structures.LinkConfirmData;
import ca.masseyhacks.minecraftdiscordlink.tasks.MDLCleanup;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class MinecraftDiscordLink extends JavaPlugin{

    public Connection connection;
    public ConcurrentHashMap<String, LinkConfirmData> confirmStatus;
    public ConcurrentHashMap<String, Long> confirmUnlinkStatus;
    //public ConcurrentHashMap<UUID, >

    public Economy econ = null;
    public Permission perms = null;

    private BukkitRunnable bgScheduleTask;

    public MinecraftDiscordLink(){
        confirmStatus = new ConcurrentHashMap<>();
        confirmUnlinkStatus = new ConcurrentHashMap<>();
    }

    private void initDb() throws SQLException{
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + "MinecraftDiscordLink"
                + "  (mcUUID           VARCHAR(36),"
                + "   discordID            VARCHAR(20),"
                + "   discordTag          VARCHAR(40),"
                + "   secret           VARCHAR(36), PRIMARY KEY (`secret`))";
        Statement stmt = connection.createStatement();
        stmt.execute(sqlCreate);
    }

    private boolean initMySQL(String url, String username, String password){
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            getLogger().severe("jdbc driver unavailable!");
            return false;
        }
        try { //Another try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(url, username, password);
            //with the method getConnection() from DriverManager, we're trying to set
            //the connection's url, username, password to the variables we made earlier and
            //trying to get a connection at the same time. JDBC allows us to do this.
            initDb();
        } catch (SQLException e) { //catching errors)
            e.printStackTrace(); //prints out SQLException errors to the console (if any)
            return false;
        }
        return true;
    }

    private String buildDBURL(String host, String database){

        String url = "jdbc:mysql://" +
                host +
                ":3306/" +
                database;
        return url;
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onEnable(){
        //Fired when the server enables the plugin
        FileConfiguration config = this.getConfig();

        // Default config values
        config.addDefault("mysqlHost", "127.0.0.1");
        config.addDefault("mysqlUser", "minecraft");
        config.addDefault("mysqlPassword", "password");
        config.addDefault("mysqlDb", "minecraftDB");

        config.options().copyDefaults(true);
        saveConfig();

        if(!initMySQL(
                buildDBURL(config.getString("mysqlHost"), config.getString("mysqlDb")),
                config.getString("mysqlUser"),
                config.getString("mysqlPassword"))
        ){
            // halt setup if MySQL cannot connect
            return;
        }

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Connection to database established.");

        getLogger().info("Registering commands.");
        getCommand("linkdiscord").setExecutor(new LinkDiscord(this));
        getCommand("linkstatus").setExecutor(new ViewDiscordLinkStatus(this));
        getCommand("unlinkdiscord").setExecutor(new UnlinkDiscord(this));
        getCommand("exportbalance").setExecutor(new ExportBalance(this));

        getLogger().info("Registering event handlers.");
        getServer().getPluginManager().registerEvents(new OnDisconnect(this), this);

        getLogger().info("Registering background tasks.");
        bgScheduleTask = new MDLCleanup(this);
        bgScheduleTask.runTaskTimer(this, 0, 100);
    }



    @Override
    public void onDisable(){
        //Fired when the server stops and disables all plugins
        bgScheduleTask.cancel();
    }
}
