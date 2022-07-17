package me.simondumalski.singledungeon.data;

import me.simondumalski.singledungeon.Main;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final Main plugin;

    private Connection connection;

    private String host;
    private int port;
    private String database;

    private String user;
    private String pass;

    public DatabaseManager(Main plugin) {

        //Set the instance to the main class
        this.plugin = plugin;

        //Load the database configuration
        host = plugin.getConfig().getString("sql.host");
        port = plugin.getConfig().getInt("sql.port");
        database = plugin.getConfig().getString("sql.database");

        user = plugin.getConfig().getString("sql.user");
        pass = plugin.getConfig().getString("sql.pass");

        //Connect to the database
        connect();

    }

    public void connect() {

        plugin.getLogger().log(Level.INFO, "Connecting to database...");

        try {

            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + database, user, pass);
            plugin.getLogger().log(Level.INFO, "Connected to database!");

        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error getting database driver!");
            ex.printStackTrace();
        } catch (PSQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error connecting to database! Check your database credentials...");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Error connecting to database! ");
            ex.printStackTrace();
        }

    }

    public void disconnect() {

        plugin.getLogger().log(Level.INFO, "Disconnecting from database...");

        try {

            if (connection == null) {
                return;
            }

            connection.close();
            plugin.getLogger().log(Level.INFO, "Disconnected from database!");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

}
