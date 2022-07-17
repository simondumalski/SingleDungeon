package me.simondumalski.singledungeon.listeners;

import me.simondumalski.singledungeon.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Main plugin;

    public PlayerQuitListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {

        //Get the player that quit
        Player player = e.getPlayer();

        //Check if the player was in a dungeon
        if (!plugin.getDungeonManager().isPlayerInDungeon(player)) {
            return;
        }

        //Force-stop the dungeon for the player
        plugin.getDungeonManager().forceStopDungeon(player);

    }

}
