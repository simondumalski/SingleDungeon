package me.simondumalski.singledungeon.listeners;

import me.simondumalski.singledungeon.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        //Get the player that joined
        Player player = e.getPlayer();

        //Check if the player is queued to be teleported to spawn
        if (!plugin.getDungeonManager().getSpawnQueuedPlayers().contains(player.getUniqueId())) {
            return;
        }

        //Teleport the player
        plugin.getDungeonManager().teleportPlayer(player);

    }

}
