package me.simondumalski.singledungeon;

import me.simondumalski.singledungeon.commands.StartCommand;
import me.simondumalski.singledungeon.data.DataConfig;
import me.simondumalski.singledungeon.data.DatabaseManager;
import me.simondumalski.singledungeon.listeners.CommandListener;
import me.simondumalski.singledungeon.listeners.EntityDeathListener;
import me.simondumalski.singledungeon.listeners.PlayerJoinListener;
import me.simondumalski.singledungeon.listeners.PlayerQuitListener;
import me.simondumalski.singledungeon.managers.DungeonManager;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private DataConfig dataConfig;
    private DatabaseManager databaseManager;
    private DungeonManager dungeonManager;

    @Override
    public void onEnable() {

        //Initialize the config.yml
        saveDefaultConfig();
        reloadConfig();

        //Initialize the data.yml
        dataConfig = new DataConfig(this);

        //Initialize the database connection
        databaseManager = new DatabaseManager(this);

        //Initialize the DungeonManager
        dungeonManager = new DungeonManager(this);

        //Register the event listeners
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        //Set the command executors
        getCommand("start").setExecutor(new StartCommand(this));

    }

    @Override
    public void onDisable() {

        //Disconnect from the database
        databaseManager.disconnect();

        //Save the list of players to teleport to /spawn on rejoin
        dataConfig.saveData(dungeonManager.getSpawnQueuedPlayers());

    }

    /**
     * Returns the instance of the plugin's DataConfig file
     * @return DataConfig
     */
    public DataConfig getDataConfig() {
        return dataConfig;
    }

    /**
     * Returns the instance of the plugin's DatabaseManager
     * @return DatabaseManager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Returns the instance of the plugin's DungeonManager
     * @return DungeonManager
     */
    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

}
