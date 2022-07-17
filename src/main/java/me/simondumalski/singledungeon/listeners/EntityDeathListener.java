package me.simondumalski.singledungeon.listeners;

import me.simondumalski.singledungeon.DungeonZombie;
import me.simondumalski.singledungeon.Main;
import net.minecraft.world.entity.Entity;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class EntityDeathListener implements Listener {

    private final Main plugin;

    public EntityDeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {

        //Get the NMS version of the entity that died
        Entity entity = ((CraftEntity) e.getEntity()).getHandle();

        //Check if the entity is a DungeonZombie
        if (!(entity instanceof DungeonZombie dungeonZombie)) {
            return;
        }

        //Get the owner of the dungeon that the zombie is in
        Player player = plugin.getServer().getPlayer(plugin.getDungeonManager().findZombieOwner(dungeonZombie));

        //Check if the owner is valid
        if (player == null) {
            return;
        }

        //Get the list of DungeonZombies belonging to the player
        List<DungeonZombie> zombies = plugin.getDungeonManager().getDungeonZombies(player);

        //Remove the entity that died from the list of zombies
        zombies.remove(dungeonZombie);

        //Check if the zombie that died was the last one
        if (!zombies.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "" + zombies.size() + "/5 mobs left!");
            return;
        }

        //If that zombie that just died is the last one, stop the dungeon
        plugin.getDungeonManager().stopDungeon(player);

    }

}
