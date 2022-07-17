package me.simondumalski.singledungeon.listeners;

import me.simondumalski.singledungeon.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener implements Listener {

    private final Main plugin;

    public CommandListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {

        //Get the player sending the command
        Player player = e.getPlayer();

        //Check if the player is in a dungeon
        if (!plugin.getDungeonManager().isPlayerInDungeon(player)) {
            return;
        }

        //Get the command being sent
        String command = e.getMessage().substring(1);

        //Get the list of disabled commands from the config.yml
        List<String> disabledCommands = plugin.getConfig().getStringList("single-dungeon.disabled-commands");

        //Check if the command being sent is a disabled command
        for (String disabledCommand : disabledCommands) {
            if (disabledCommand.contains(command)) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can't use that command while in a dungeon!");
            }
        }

    }

}
