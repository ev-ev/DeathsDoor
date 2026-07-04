package me.ev.deathsdoor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import static me.ev.deathsdoor.DeathsDoor.*;
import static me.ev.deathsdoor.DeathsDoor.applyStatuses;

public class BaseEventHandler {
    private static EntityPlayer thisPlayer = null;

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;
        if (thisPlayer == null) {
            thisPlayer = event.player;
            registerHandler();
            if (thisPlayer.getEntityData().getBoolean(NBT_onDeathsDoor)) {
                applyStatuses(thisPlayer);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player == thisPlayer) {
            unregisterHandler();
            thisPlayer = null;
        }
    }
}
