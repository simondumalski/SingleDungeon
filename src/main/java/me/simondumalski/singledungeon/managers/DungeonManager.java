package me.simondumalski.singledungeon.managers;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.simondumalski.singledungeon.DungeonZombie;
import me.simondumalski.singledungeon.Main;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DungeonManager {

    private final Main plugin;
    private Location startLocation = null;
    private final HashMap<UUID, List<DungeonZombie>> playersInDungeons = new HashMap<>();
    private final List<UUID> spawnQueuedPlayers;
    private final HashMap<UUID, Integer> playerTaskIds = new HashMap<>();

    public DungeonManager(Main plugin) {

        //Set the instance to the main class
        this.plugin = plugin;

        //Load the start location
        loadStartLocation();

        //Get the saved list of spawnQueuedPlayers
        spawnQueuedPlayers = plugin.getDataConfig().loadData();

    }

    /**
     * Returns true/false if the specified player is in a dungeon
     * @param player Player to check
     * @return True/false
     */
    public boolean isPlayerInDungeon(Player player) {
        return playersInDungeons.containsKey(player.getUniqueId());
    }


    /**
     * Starts a dungeon
     * @param player Player to start the dungeon for
     */
    public void startDungeon(Player player) {

        //Send the player a message saying they are being teleported
        player.sendMessage(ChatColor.GREEN + "Teleporting to dungeon...");

        //Teleport the player to the start location
        startLocation.getChunk().load();
        player.teleport(startLocation);

        //Add the player to the list of players in dungeons
        playersInDungeons.put(player.getUniqueId(), new ArrayList<>());

        //Hide the players and entities for all players in dungeons
        hideEntities();

        //Play some creepy sounds
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1, 1);

        //Tell the player they have 30 seconds to prepare
        player.sendMessage(ChatColor.RED + "You have 30 seconds to prepare to fight!");

        //Spawn the zombies 30 seconds later
        int taskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            //Send the player a message to start fighting and play the raid horn sound
            player.playSound(player.getLocation(), Sound.EVENT_RAID_HORN, 20, 1);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "May the odds ever be in your favour...");

            //Spawn the DungeonZombies
            ServerLevel world = ((CraftWorld) player.getLocation().getWorld()).getHandle();

            for (int i = 0; i < 5; i++) {

                DungeonZombie zombie = new DungeonZombie(startLocation, ((CraftPlayer) player).getHandle());
                playersInDungeons.get(player.getUniqueId()).add(zombie);
                world.addFreshEntity(zombie);

            }

            //Hide the entities again
            hideEntities();

            playerTaskIds.remove(player.getUniqueId());

        }, 600).getTaskId();

        //Add the runnable Task ID to the list in case the player logs out
        playerTaskIds.put(player.getUniqueId(), taskId);

    }

    /**
     * Stops a dungeon
     * @param player Player to stop the dungeon for
     */
    public void stopDungeon(Player player) {

        //Remove the player from the HashMap of players in dungeons
        playersInDungeons.remove(player.getUniqueId());

        //Send the player a success message
        player.sendMessage(ChatColor.GREEN + "You completed the dungeon!");

        //Teleport the player back to /spawn
        teleportPlayer(player);

        //Play a win sound
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);

        //Show the entities back to the player
        showEntities(player);

        //Show the player that won the dungeon to all the players still in dungeons
        showPlayer(player);

    }

    /**
     * Force-stops a dungeon. Use when a player disconnects from the server
     * @param player Player to force-stop the dungeon for
     */
    public void forceStopDungeon(Player player) {

        //Cancel any tasks scheduled for the player
        if (playerTaskIds.containsKey(player.getUniqueId())) {
            plugin.getServer().getScheduler().cancelTask(playerTaskIds.get(player.getUniqueId()));
            playerTaskIds.remove(player.getUniqueId());
        }

        //De-spawn the zombies
        for (DungeonZombie zombie : playersInDungeons.get(player.getUniqueId())) {
            zombie.remove(Entity.RemovalReason.DISCARDED);
        }

        //Queue the player to be teleported back to /spawn on rejoin
        spawnQueuedPlayers.add(player.getUniqueId());

        //Remove the player from the HashMap of players in dungeons
        playersInDungeons.remove(player.getUniqueId());

    }

    /**
     * Teleports the player back to /spawn
     * @param player Player to teleport
     */
    public void teleportPlayer(Player player) {

        //Check if the player was spawn queued
        if (spawnQueuedPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You logged out while in a dungeon and have been sent to spawn!");
            spawnQueuedPlayers.remove(player.getUniqueId());
        } else {
            player.sendMessage(ChatColor.GREEN + "Teleporting to spawn...");
        }

        //Teleport the player to /spawn
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "spawn " + player.getName());

    }

    /**
     * Returns the UUID of the player whose dungeon contains this zombie
     * @param zombie DungeonZombie to find the owner of
     * @return Owner of the DungeonZombie, null if none was found
     */
    public UUID findZombieOwner(DungeonZombie zombie) {

        //Loop through each player
        for (UUID uuid : playersInDungeons.keySet()) {
            if (playersInDungeons.get(uuid).contains(zombie)) {
                return uuid;
            }
        }

        return null;
    }

    /**
     * Returns the list of DungeonZombies that are in the player's dungeon
     * @param player Player whose zombies to get
     * @return List of DungeonZombies
     */
    public List<DungeonZombie> getDungeonZombies(Player player) {
        return playersInDungeons.get(player.getUniqueId());
    }

    /**
     * Loads the start location from the data.yml
     */
    public void loadStartLocation() {

        //Get the set start location from the config.yml
        String worldName = plugin.getConfig().getString("single-dungeon.start-location.world");
        int x = plugin.getConfig().getInt("single-dungeon.start-location.x");
        int y = plugin.getConfig().getInt("single-dungeon.start-location.y");
        int z = plugin.getConfig().getInt("single-dungeon.start-location.z");

        //Check if the world name is valid
        if (worldName == null) {
            plugin.getLogger().log(Level.WARNING, ChatColor.RED + "Error loading start location world! If this is a fresh install, please modify your config.yml");
            return;
        }

        //Get the world
        World world = plugin.getServer().getWorld(worldName);

        //Check if the world is valid
        if (world == null) {
            plugin.getLogger().log(Level.WARNING, ChatColor.RED + "Error loading start location world! If this is a fresh install, please modify your config.yml");
            return;
        }

        //Set the start location
        startLocation = new Location(world, x, y, z);

        //Log to console that the start location was loaded
        plugin.getLogger().log(Level.INFO, ChatColor.GREEN + "Dungeon start location set at location X: " + x + " Y: " + y + " Z: " + z + " in world " + worldName);

    }

    /**
     * Returns true/false if the start location is set
     * @return True/false
     */
    public boolean isStartLocationSet() {
        return startLocation != null;
    }


    /**
     * Returns the list of players who are queued to be teleported to spawn on join
     * @return List of player UUIDs
     */
    public List<UUID> getSpawnQueuedPlayers() {
        return spawnQueuedPlayers;
    }

    /**
     * Hides players and their dungeon enemies to other players in dungeons
     */
    private void hideEntities() {

        for (UUID uuid : playersInDungeons.keySet()) {

            //Get the player
            ServerPlayer player = ((CraftPlayer) plugin.getServer().getPlayer(uuid)).getHandle();

            //Create a list of entity IDs to hide
            IntList ids = new IntArrayList();

            for (UUID dungeonPlayerUUID : playersInDungeons.keySet()) {

                //Check if the player is the same player in the parent loop
                if (dungeonPlayerUUID == uuid) {
                    continue;
                }

                //Get the player
                ServerPlayer dungeonPlayer = ((CraftPlayer) plugin.getServer().getPlayer(dungeonPlayerUUID)).getHandle();

                //Get the player's zombies
                List<DungeonZombie> zombies = playersInDungeons.get(dungeonPlayerUUID);

                //Add all the IDs to the IntList
                ids.add(dungeonPlayer.getId());

                for (DungeonZombie zombie : zombies) {
                    ids.add(zombie.getId());
                }

            }

            //Create the EntityRemovePacket
            ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(ids);

            //Send the packet
            player.connection.send(packet);

        }

    }

    /**
     * Shows players and their dungeon enemies to the specified player
     */
    private void showEntities(Player player) {

        //Get the NMS player
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        //Show all online players
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {

            if (onlinePlayer == player) {
                continue;
            }

            //Get the NMS player
            ServerPlayer nmsPlayer = ((CraftPlayer) onlinePlayer).getHandle();

            serverPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, nmsPlayer));
            serverPlayer.connection.send(new ClientboundAddPlayerPacket(nmsPlayer));
            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(nmsPlayer.getId(), nmsPlayer.getEntityData(), true));
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), List.of(new Pair<>(EquipmentSlot.HEAD, nmsPlayer.getItemBySlot(EquipmentSlot.HEAD)))));
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), List.of(new Pair<>(EquipmentSlot.CHEST, nmsPlayer.getItemBySlot(EquipmentSlot.CHEST)))));
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), List.of(new Pair<>(EquipmentSlot.LEGS, nmsPlayer.getItemBySlot(EquipmentSlot.LEGS)))));
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), List.of(new Pair<>(EquipmentSlot.FEET, nmsPlayer.getItemBySlot(EquipmentSlot.FEET)))));
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), List.of(new Pair<>(EquipmentSlot.MAINHAND, nmsPlayer.getItemBySlot(EquipmentSlot.MAINHAND)))));
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), List.of(new Pair<>(EquipmentSlot.OFFHAND, nmsPlayer.getItemBySlot(EquipmentSlot.OFFHAND)))));

        }

        for (UUID uuid : playersInDungeons.keySet()) {

            //Get the player's zombies
            List<DungeonZombie> zombies = playersInDungeons.get(uuid);

            //Show each zombie
            for (DungeonZombie zombie : zombies) {
                serverPlayer.connection.send(new ClientboundAddEntityPacket(zombie));
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(zombie.getId(), zombie.getEntityData(), true));
            }

        }

    }

    /**
     * Shows the specified player to all players currently in a dungeon
     * @param player Player to show
     */
    private void showPlayer(Player player) {

        //Get the NMS player
        ServerPlayer playerToShow = ((CraftPlayer) player).getHandle();

        for (UUID uuid : playersInDungeons.keySet()) {

            if (uuid == player.getUniqueId()) {
                continue;
            }

            //Get the NMS player
            ServerPlayer nmsPlayer = ((CraftPlayer) plugin.getServer().getPlayer(uuid)).getHandle();

            //Show the player to show
            nmsPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, playerToShow));
            nmsPlayer.connection.send(new ClientboundAddPlayerPacket(playerToShow));
            nmsPlayer.connection.send(new ClientboundSetEntityDataPacket(playerToShow.getId(), playerToShow.getEntityData(), true));
            nmsPlayer.connection.send(new ClientboundSetEquipmentPacket(playerToShow.getId(), List.of(new Pair<>(EquipmentSlot.HEAD, playerToShow.getItemBySlot(EquipmentSlot.HEAD)))));
            nmsPlayer.connection.send(new ClientboundSetEquipmentPacket(playerToShow.getId(), List.of(new Pair<>(EquipmentSlot.CHEST, playerToShow.getItemBySlot(EquipmentSlot.CHEST)))));
            nmsPlayer.connection.send(new ClientboundSetEquipmentPacket(playerToShow.getId(), List.of(new Pair<>(EquipmentSlot.LEGS, playerToShow.getItemBySlot(EquipmentSlot.LEGS)))));
            nmsPlayer.connection.send(new ClientboundSetEquipmentPacket(playerToShow.getId(), List.of(new Pair<>(EquipmentSlot.FEET, playerToShow.getItemBySlot(EquipmentSlot.FEET)))));
            nmsPlayer.connection.send(new ClientboundSetEquipmentPacket(playerToShow.getId(), List.of(new Pair<>(EquipmentSlot.MAINHAND, playerToShow.getItemBySlot(EquipmentSlot.MAINHAND)))));
            nmsPlayer.connection.send(new ClientboundSetEquipmentPacket(playerToShow.getId(), List.of(new Pair<>(EquipmentSlot.OFFHAND, playerToShow.getItemBySlot(EquipmentSlot.OFFHAND)))));

        }

    }

}
