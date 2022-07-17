package me.simondumalski.singledungeon.commands;

import me.simondumalski.singledungeon.Main;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    private final Main plugin;

    public StartCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player player) {

            //Check if the player has permission to start a dungeon
            if (!player.hasPermission("singledungeon.start")) {
                player.sendMessage(ChatColor.RED + "Insufficient permissions!");
                return true;
            }

            //Check if the start location is set
            if (!plugin.getDungeonManager().isStartLocationSet()) {
                player.sendMessage(ChatColor.RED + "Start location not set!");
                return true;
            }

            //Check if the player is already in a dungeon
            if (plugin.getDungeonManager().isPlayerInDungeon(player)) {
                player.sendMessage(ChatColor.RED + "You are already in a dungeon!");
                return true;
            }

            //Start the dungeon
            plugin.getDungeonManager().startDungeon(player);

        } else {
            sender.sendMessage(ChatColor.RED + "Only players may use this command!");
        }

        return true;
    }

}
