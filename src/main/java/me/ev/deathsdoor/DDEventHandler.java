package me.ev.deathsdoor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static me.ev.deathsdoor.DeathsDoor.*;

public class DDEventHandler {
    private static boolean restoreRegen = false;

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.getEntityData().getBoolean(NBT_onDeathsDoor)) {
            applyStatuses(event.player);
        }
    }

    @SubscribeEvent
    public void onLivingHurtEvent(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
            if (CONFIG.ddTotemMode()) {
                if (player.getHealth() + player.getAbsorptionAmount() - event.getAmount() <= ddHealth &&
                    tryUseDeathProtectorAccessor(player, event.getSource())) {
                    event.setCanceled(true);
                    if (player.getEntityData().getBoolean(NBT_onDeathsDoor))
                        resistDeathsDoor(player, event.getSource());
                    else enterDeathsDoor(player, event.getSource());
                }
            } else {
                if (player.getEntityData().getBoolean(NBT_onDeathsDoor)) {
                    if (CONFIG.ddResist() != 0 &&
                        (CONFIG.ddResist() > RAND.nextFloat()) &&
                        player.getHealth() + player.getAbsorptionAmount() - event.getAmount() <= 0.0f) {
                        event.setCanceled(true);
                        resistDeathsDoor(player, event.getSource());
                    }
                } else if (player.getHealth() + player.getAbsorptionAmount() - event.getAmount() <= ddHealth) {
                    event.setCanceled(true);
                    enterDeathsDoor(player, event.getSource());
                }
            }
        }
    }

    private boolean tryUseDeathProtectorAccessor(EntityPlayerMP player, DamageSource source) {
        try {
            Method method = EntityLivingBase.class.getDeclaredMethod("checkTotemDeathProtection", DamageSource.class);
            method.setAccessible(true);
            return (boolean) method.invoke(player, source);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Player {} does not have checkTotemDeathProtection method", player.getName());
        }
        return false;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player == null || event.player.world.isRemote) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            if (event.player.getEntityData().getBoolean(NBT_onDeathsDoor)) {
                //Leave DD if healed over threshold
                if (event.player.getHealth() + event.player.getAbsorptionAmount() > ddHealth) {
                    leaveDeathsDoor(event.player);
                    //Disable food regen for this instance if on death's door (hack)
                } else if (event.player.world.getGameRules().getBoolean("naturalRegeneration")) {
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

        if (event.player.isEntityAlive() && event.player.getEntityData().getBoolean(NBT_onDeathsDoor) && event.player.ticksExisted % 3 == 0) {
            ((WorldServer) event.player.getEntityWorld()).spawnParticle(
                EnumParticleTypes.REDSTONE,
                event.player.posX,
                event.player.posY + 1.0,
                event.player.posZ,
                1,
                0.3, 0.5, 0.3,
                0);
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (event.getCommand().getName().equals("reload")) {
            DeathsDoor.CONFIG.reload();
            LOGGER.info("{} reloaded config from disk.", Tags.MOD_NAME);
        }
    }

    //Disable onLivingHeal for peaceful mode
}
