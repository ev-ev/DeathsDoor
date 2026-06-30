package me.ev.deathsdoor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Display;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;

import static me.ev.deathsdoor.DeathsDoor.R;

/**
 * Intended to be an "attack" or "damage" particle. WIP.
 */
public class DDisplayEntity extends Display.TextDisplay {
    private int ticksAlive = 20;
    private final ServerLevel world;
    private static final MutableComponent
        text = Component.literal("DEATH'S DOOR").setStyle(Style.EMPTY.withColor(0x9c0606));


    //If the server crashes before the text de-spawns it will stay forever
    public DDisplayEntity(ServerPlayer player) {
        super(EntityTypes.TEXT_DISPLAY, player.level());
        world = player.level();

        this.setText(text);
        this.setPos(player.position().add(0, 1, 0));
        this.absSnapRotationTo(player.getYRot(),0);
        this.setDeltaMovement(
                        new Vec3(-1 + R.nextFloat() * 2, -1 + R.nextFloat() * 2 , -1 + R.nextFloat() * 2).normalize().scale(0.05));
        world.addFreshEntity(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticksAlive <= 0) {
            this.kill(world);
            return;
        }
        this.setPos(this.position().add(this.getDeltaMovement()));
        this.setDeltaMovement(this.getDeltaMovement().add(0, -0.01f, 0));

        ticksAlive -= 1;
    }
}
