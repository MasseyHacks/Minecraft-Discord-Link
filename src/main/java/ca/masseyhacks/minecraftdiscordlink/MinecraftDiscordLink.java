package ca.masseyhacks.minecraftdiscordlink;

import ca.masseyhacks.minecraftdiscordlink.commands.*;
import ca.masseyhacks.minecraftdiscordlink.events.OnDisconnect;
import ca.masseyhacks.minecraftdiscordlink.expansions.MasseyHacksInfoExpansion;
import ca.masseyhacks.minecraftdiscordlink.sql.SQLManager;
import ca.masseyhacks.minecraftdiscordlink.structures.LinkConfirmData;
import ca.masseyhacks.minecraftdiscordlink.structures.ParticipantInfo;
import ca.masseyhacks.minecraftdiscordlink.tasks.MDLCleanup;
import ca.masseyhacks.minecraftdiscordlink.tasks.MDLPlaceholderInfoRefresh;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinecraftDiscordLink extends JavaPlugin{

    //public Connection connection;
    private SQLManager sql;

    public ConcurrentHashMap<UUID, LinkConfirmData> confirmStatus;
    public ConcurrentHashMap<UUID, Long> confirmUnlinkStatus;
    public ConcurrentHashMap<UUID, ParticipantInfo> placeholderInfoCache;

    public Economy econ = null;
    public Permission perms = null;

    private BukkitRunnable bgScheduleTaskCleanup;
    private BukkitRunnable bgUpdatePlaceholderCache;

    public double depositMultiplier;

    public MinecraftDiscordLink(){
        confirmStatus = new ConcurrentHashMap<>();
        confirmUnlinkStatus = new ConcurrentHashMap<>();
        placeholderInfoCache = new ConcurrentHashMap<>();

        depositMultiplier = 1.0D;
    }

    private void initDb(){
        sql = new SQLManager(this);
    }

//    private boolean initMySQL(String url, String username, String password){
//        try { //We use a try catch to avoid errors, hopefully we don't get any.
//            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            getLogger().severe("jdbc driver unavailable!");
//            return false;
//        }
//        try { //Another try catch to get any SQL errors (for example connections errors)
//            connection = DriverManager.getConnection(url, username, password);
//            //with the method getConnection() from DriverManager, we're trying to set
//            //the connection's url, username, password to the variables we made earlier and
//            //trying to get a connection at the same time. JDBC allows us to do this.
//            initDb();
//        } catch (SQLException e) { //catching errors)
//            e.printStackTrace(); //prints out SQLException errors to the console (if any)
//            return false;
//        }
//        return true;
//    }

    private String buildDBURL(String host, String database){

        return "jdbc:mysql://" +
                host +
                ":3306/" +
                database;
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
        config.addDefault("mysql.Host", "127.0.0.1");
        config.addDefault("mysql.Port", 3306);
        config.addDefault("mysql.User", "minecraft");
        config.addDefault("mysql.Password", "password");
        config.addDefault("mysql.Db", "minecraftDB");
        config.addDefault("mysqlPool.MinConnections", 3);
        config.addDefault("mysqlPool.MaxConnections", 5);
        config.addDefault("mysqlPool.Timeout", 30000);
        config.addDefault("placeholderCacheRefreshTime", 600);

        config.options().copyDefaults(true);
        saveConfig();

        initDb();

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupPermissions();

        getLogger().info("Connection to database established.");

        getLogger().info("Registering commands.");
        getCommand("linkdiscord").setExecutor(new LinkDiscord(this));
        getCommand("linkstatus").setExecutor(new ViewDiscordLinkStatus(this));
        getCommand("unlinkdiscord").setExecutor(new UnlinkDiscord(this));
        getCommand("exportbalance").setExecutor(new ExportBalance(this));
        getCommand("updatecache").setExecutor(new UpdateCache(this));
        getCommand("depositmultiplier").setExecutor(new DepositMultiplier(this));

        getLogger().info("Registering event handlers.");
        getServer().getPluginManager().registerEvents(new OnDisconnect(this), this);

        getLogger().info("Registering background tasks.");
        bgScheduleTaskCleanup = new MDLCleanup(this);
        bgScheduleTaskCleanup.runTaskTimer(this, 0, 100);

        bgUpdatePlaceholderCache = new MDLPlaceholderInfoRefresh(this);
        bgUpdatePlaceholderCache.runTaskTimer(this, 0, config.getInt("placeholderCacheRefreshTime"));

        getLogger().info("Registering placeholders.");
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            new MasseyHacksInfoExpansion(this).register();
        }
        else{
            getLogger().warning("Failed to register placeholders. PlaceholderAPI not installed.");
        }
    }



    @Override
    public void onDisable(){
        //Fired when the server stops and disables all plugins
        sql.onDisable();
        bgScheduleTaskCleanup.cancel();
        bgUpdatePlaceholderCache.cancel();
    }

    public SQLManager getSQLManager() {
        return sql;
    }
}
