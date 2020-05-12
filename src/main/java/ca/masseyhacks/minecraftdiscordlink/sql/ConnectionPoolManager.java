package ca.masseyhacks.minecraftdiscordlink.sql;

import ca.masseyhacks.minecraftdiscordlink.MinecraftDiscordLink;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final MinecraftDiscordLink plugin;

    private HikariDataSource dataSource;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    private int minimumConnections;
    private int maximumConnections;
    private long connectionTimeout;

    public ConnectionPoolManager(MinecraftDiscordLink plugin) {
        this.plugin = plugin;
        init();
        setupPool();
    }

    private void init() {
        hostname = plugin.getConfig().getString("mysql.Host");
        port = plugin.getConfig().getInt("mysql.Port");
        database = plugin.getConfig().getString("mysql.Db");
        username = plugin.getConfig().getString("mysql.User");
        password = plugin.getConfig().getString("mysql.Password");
        minimumConnections = plugin.getConfig().getInt("mysqlPool.MinConnections");
        maximumConnections = plugin.getConfig().getInt("mysqlPool.MaxConnections");
        connectionTimeout = plugin.getConfig().getInt("mysqlPool.Timeout");
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" +
                        hostname +
                        ":" +
                        port +
                        "/" +
                        database
        );
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(minimumConnections);
        config.setMaximumPoolSize(maximumConnections);
        config.setConnectionTimeout(connectionTimeout);
        //config.setConnectionTestQuery(testQuery);
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (res != null) try { res.close(); } catch (SQLException ignored) {}
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}