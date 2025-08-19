package me.ev.deathsdoor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class DeathsDoor {
    static boolean handlerRegistered = false;
    static DDEventHandler handler;
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    static final float DDHP = 0.01f;
    static final String NBT_onDeathsDoor = "DeathsDoor_onDeathsDoor";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        handler = new DDEventHandler();
        if (event.getSide() == Side.SERVER) {
            registerHandler();
        } else {
            MinecraftForge.EVENT_BUS.register(new BaseEventHandler());
        }


        //registerHandler();
    }

    static void registerHandler() {
        if (!handlerRegistered) {
            MinecraftForge.EVENT_BUS.register(handler);
            handlerRegistered = true;
        }
    }

    static void unregisterHandler() {
        if (handlerRegistered) {
            MinecraftForge.EVENT_BUS.unregister(handler);
            handlerRegistered = false;
        }
    }



    static void enterDeathsDoor(EntityPlayer player){
        if (player instanceof EntityPlayerMP) {
            player.getEntityData().setBoolean(NBT_onDeathsDoor, true);
            player.setHealth(DDHP);
            player.setAbsorptionAmount(0.0f);
            applyDeathsDoorEffects(player);
            playDeathsDoorFX(player);
        }
    }
    static void leaveDeathsDoor(EntityPlayer player) {
        player.getEntityData().setBoolean(NBT_onDeathsDoor, false);

        clearDeathsDoorEffects(player);
        applyDeathsDoorPenaltyEffects(player);
    }

    private static void applyDeathsDoorEffects(EntityPlayer player) {
        //Remove regeneration
        player.removePotionEffect(MobEffects.REGENERATION);
        //Push effects
        player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 9999999, 0));
        player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 9999999, 1));
        player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 9999999, 0));
        player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 9999999, 1));
        //Wither effect
        ((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), new PotionEffect(MobEffects.WITHER, 9999999, 0)));
    }

    private static void applyDeathsDoorPenaltyEffects(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 15*20, 1));
        player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 15*20, 0));
        player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 15*20, 0));
    }

    private static void clearDeathsDoorEffects(EntityPlayer player) {
        //Remove effects
        player.removePotionEffect(MobEffects.BLINDNESS);
        player.removePotionEffect(MobEffects.SLOWNESS);
        player.removePotionEffect(MobEffects.WEAKNESS);
        player.removePotionEffect(MobEffects.MINING_FATIGUE);
        player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1, 0));
        player.removePotionEffect(MobEffects.WITHER);
    }

    private static void playDeathsDoorFX(EntityPlayer player) {
        //world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 10.0f, 1.0f);

        player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 0.1f, 1.0f);
        //player.playSound(SoundEvents.BLOCK_NOTE_PLING);
    }
}
