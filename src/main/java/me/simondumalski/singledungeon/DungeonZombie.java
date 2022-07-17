package me.simondumalski.singledungeon;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.event.entity.EntityTargetEvent;


public class DungeonZombie extends Zombie {

    /**
     * Constructor for a DungeonZombie
     * @param location Location to spawn the zombie at
     * @param serverPlayer Player the zombie will target
     */
    public DungeonZombie(Location location, ServerPlayer serverPlayer) {

        super(EntityType.ZOMBIE, ((CraftWorld)location.getWorld()).getHandle());
        this.setPos(location.getX(), location.getY(), location.getZ());

        this.setHealth(20);
        this.setCustomNameVisible(true);
        this.setCustomName(new TextComponent("§cDungeon §6Dweller"));

        //Removes the auto-targeting
        this.targetSelector.removeAllGoals();

        //Sets the target to the specified player
        this.setTarget(serverPlayer, EntityTargetEvent.TargetReason.CUSTOM, false);

    }

}
