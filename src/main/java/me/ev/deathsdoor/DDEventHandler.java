package me.ev.deathsdoor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static me.ev.deathsdoor.DeathsDoor.*;

public class DDEventHandler {
    private static boolean restoreRegen = false;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (player.getHealth() + player.getAbsorptionAmount() <= DDHP) {
                enterDeathsDoor(player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurtEvent(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (!player.getEntityData().getBoolean(NBT_onDeathsDoor)) {
                if (player.getHealth() + player.getAbsorptionAmount() - event.getAmount() <= DDHP) {
                    event.setCanceled(true);
                    enterDeathsDoor(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if (event.player == null || event.player.world.isRemote) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            if (event.player.getEntityData().getBoolean(NBT_onDeathsDoor)) {
                //Leave DD if healed over threshold
                if (event.player.getHealth() + event.player.getAbsorptionAmount() > DDHP) {
                    leaveDeathsDoor(event.player);
                //Disable food regen for this instance if on death's door (hack)
                } else if (event.player.world.getGameRules().getBoolean("naturalRegeneration")){
                    restoreRegen = true;
                    event.player.world.getGameRules().setOrCreateGameRule("naturalRegeneration", "false");
                }
            }
        } else {
            //Enable food regen if disabled earlier
            if (restoreRegen) {
                restoreRegen = false;
                event.player.world.getGameRules().setOrCreateGameRule("naturalRegeneration", "true");
            }
        }
    }
    //Disable onLivingHeal for peaceful mode
}
