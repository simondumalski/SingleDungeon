package me.simondumalski.singledungeon.data;

import me.simondumalski.singledungeon.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DataConfig {

    private final Main plugin;
    private File file;
    private YamlConfiguration config;
    private final String FILE_NAME = "data.yml";

    public DataConfig(Main plugin) {

        //Set the instance to the main plugin class
        this.plugin = plugin;

        //Initialize the file
        initialize();

    }

    /**
     * Initializes the file
     */
    private void initialize() {

        try {

            file = new File(plugin.getDataFolder(), FILE_NAME);
            config = YamlConfiguration.loadConfiguration(file);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Saves the spawn queued players to file
     */
    public void saveData(List<UUID> spawnQueuedPlayers) {

        try {

            if (!config.isConfigurationSection("data")) {
                config.createSection("data");
            }

            //Convert the UUIDs to Strings
            List<String> uuidStrings = new ArrayList<>();

            for (UUID uuid : spawnQueuedPlayers) {
                uuidStrings.add(uuid.toString());
            }

            config.set("data", uuidStrings);
            config.save(file);

            plugin.getLogger().log(Level.INFO, ChatColor.GREEN + "Data.yml saved.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Loads the list of player UUIDs that are to be teleported to /spawn on join
     * @return List of UUIDs
     */
    public List<UUID> loadData() {

        //Get a list ready for storing the UUIDs of players who are to be teleported to /spawn on rejoin
        List<UUID> spawnQueuedPlayers = new ArrayList<>();

        try {

            //Get the spawn queued players from the data.yml
            List<String> uuidStrings = config.getStringList("data");

            //Convert the Strings to UUIDs
            for (String uuidString : uuidStrings) {
                spawnQueuedPlayers.add(UUID.fromString(uuidString));
            }

            plugin.getLogger().log(Level.INFO, ChatColor.GREEN + "Data.yml loaded.");

        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, ChatColor.RED + "Error loading data.yml!");
            ex.printStackTrace();
        }

        return spawnQueuedPlayers;
    }

}
