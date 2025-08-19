package me.ev.deathsdoor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import static me.ev.deathsdoor.DeathsDoor.registerHandler;
import static me.ev.deathsdoor.DeathsDoor.unregisterHandler;

public class BaseEventHandler {
    /*
    @SubscribeEvent
    public void onWorldLoadEvent(WorldEvent.Load event) {
        if (event.getWorld() == null)
            unregisterHandler();
        else {
            if (event.getWorld().isRemote)
                unregisterHandler();
            else {
                registerHandler();
                world = event.getWorld();
            }
        }
    }*/

    private static EntityPlayer thisPlayer = null;

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (thisPlayer == null) {
            thisPlayer = event.player;
            registerHandler();
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
