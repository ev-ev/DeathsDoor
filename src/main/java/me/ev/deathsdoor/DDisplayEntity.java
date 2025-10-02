package me.ev.deathsdoor;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.math.Vec3d;

import static me.ev.deathsdoor.DeathsDoor.R;

/**
 * Intended to be a "attack" or "damage" particle. WIP.
 */
public class DDisplayEntity extends DisplayEntity.TextDisplayEntity {
    private int ticksAlive = 20;
    private final ServerWorld world;
    private static final MutableText text = Text.literal("DEATH'S DOOR").setStyle(Style.EMPTY.withColor(0x9c0606));


    //If the server crashes before the text de-spawns it will stay forever
    public DDisplayEntity(ServerPlayerEntity player) {
        super(EntityType.TEXT_DISPLAY, player.getEntityWorld());
        world = player.getEntityWorld();

        this.setText(text);
        this.setPosition(player.getEntityPos().add(0, 1, 0));
        this.setAngles(player.getYaw(),0);
        this.setVelocity(
                        new Vec3d(-1 + R.nextFloat()*2, -1 + R.nextFloat()*2 ,-1 + R.nextFloat()*2).normalize().multiply(0.05));
        world.spawnEntity(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticksAlive <= 0) {
            this.kill(world);
            return;
        }
        this.setPosition(this.getEntityPos().add(this.getVelocity()));
        this.setVelocity(this.getVelocity().add(0, -0.01f, 0));

        ticksAlive -= 1;
    }
}
