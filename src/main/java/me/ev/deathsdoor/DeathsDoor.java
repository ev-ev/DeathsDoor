package me.ev.deathsdoor;

import me.ev.deathsdoor.config.DeathsDoorConfig;
import me.ev.deathsdoor.config.DeathsDoorDefaultConfig;
import me.ev.deathsdoor.config.DeathsDoorLoadableConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class DeathsDoor {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final Random RAND = new Random();
    static final float ddHealth = 0.01f;
    static final String NBT_onDeathsDoor = "DeathsDoor_onDeathsDoor";
    public static DeathsDoorConfig CONFIG;
    static boolean handlerRegistered = false;
    static DDEventHandler handler;

    static void unregisterHandler() {
        if (handlerRegistered) {
            MinecraftForge.EVENT_BUS.unregister(handler);
            handlerRegistered = false;
        }
    }

    static void enterDeathsDoor(EntityPlayerMP player, DamageSource source) {
        player.getEntityData().setBoolean(NBT_onDeathsDoor, true);
        player.setHealth(ddHealth);
        player.setAbsorptionAmount(0.0f);
        applyStatuses(player);
        onDeathsDoorFX(player, source);
        onDeathsDoorChat(player, source);
    }

    static void resistDeathsDoor(EntityPlayerMP player, DamageSource source) {

        player.getEntityData().setBoolean(NBT_onDeathsDoor, true);
        player.setHealth(ddHealth);
        player.setAbsorptionAmount(0.0f);
        applyStatuses(player);
        onDeathsDoorFX(player, source);
        resistDeathsDoorChat(player, source);
    }

    static void applyStatuses(EntityPlayer player) {
        //Remove regeneration
        player.removePotionEffect(MobEffects.REGENERATION);
        //Push effects
        for (ImmutablePair<Potion, Integer> entry : CONFIG.ddEffects()) {
            player.removePotionEffect(entry.left);
            player.addPotionEffect(new PotionEffect(entry.left, 9999999, entry.right, false, false));
        }
        //Wither effect
        ((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEffect(player.getEntityId(),
            new PotionEffect(MobEffects.WITHER, 9999999, 0, false, false)));
    }

    private static void onDeathsDoorFX(EntityPlayerMP player, DamageSource source) {
        if (CONFIG.ddPlaySoundAround()) {
            player.playSound(CONFIG.ddSound(), CONFIG.ddSoundAroundVolume(), CONFIG.ddSoundPitch());
        }
        playSoundToPlayer(player,
            CONFIG.ddSound(),
            SoundCategory.PLAYERS,
            CONFIG.ddSoundVolume(),
            CONFIG.ddSoundPitch());

        if (source != null &&
            source.getTrueSource() != null &&
            !source.getTrueSource().equals(player) &&
            source.getTrueSource() instanceof EntityPlayerMP) {
            playSoundToPlayer((EntityPlayerMP) source.getTrueSource(),
                CONFIG.ddAttackerSound(),
                SoundCategory.PLAYERS,
                CONFIG.ddAttackerSoundVolume(),
                CONFIG.ddAttackerSoundPitch());
        }

    }

    private static void onDeathsDoorChat(EntityPlayerMP player, DamageSource source) {
        if (source != null && source.getTrueSource() != null && !source.getTrueSource().equals(player)) {
            broadcast(player, CONFIG.ddMessage(player.getName(), source.getTrueSource().getName()), source);
            if (CONFIG.ddGlobalBroadcastMessage()) {
                player.server.getPlayerList().sendMessage(CONFIG.ddMessageNS(player.getName(), source.getTrueSource().getName()), false);
            }
        } else if (!CONFIG.ddTranslation().isEmpty()) {
            broadcast(player, CONFIG.ddMessage(player.getName()), source);
            if (CONFIG.ddGlobalBroadcastMessage()) {
                player.server.getPlayerList().sendMessage(CONFIG.ddMessageNS(player.getName()), false);
            }
        }
    }

    private static void resistDeathsDoorChat(EntityPlayerMP player, DamageSource source) {
        broadcast(player, CONFIG.ddMessageResist(player.getName()), source);
        if (CONFIG.ddGlobalBroadcastMessage()) {
            player.server.getPlayerList().sendMessage(CONFIG.ddMessageResistNS(player.getName()), false);
        }
    }

    private static void playSoundToPlayer(EntityPlayerMP player, SoundEvent sound, SoundCategory category, float volume,
                                          float pitch) {
        player.connection.sendPacket(new SPacketSoundEffect(sound,
            category,
            player.posX,
            player.posY,
            player.posZ,
            volume,
            pitch));
    }

    private static void broadcast(EntityPlayerMP player, TextComponentString message, DamageSource source) {
        EntityPlayerMP src;
        if (source != null && source.getTrueSource() != null && source.getTrueSource() instanceof EntityPlayerMP) {
            src = (EntityPlayerMP) source.getTrueSource();
        } else {
            src = null;
        }

        if (CONFIG.ddMaxBroadcastDistance() == 0.0f) {
            player.sendStatusMessage(message, true);
            if (src != null) src.sendStatusMessage(message, true);
        } else if (CONFIG.ddMaxBroadcastDistance() == -1.0f) {
            player.server.getPlayerList().sendPacketToAllPlayers(new SPacketChat(message, ChatType.GAME_INFO));
        } else {
            player.world.getPlayers(EntityPlayerMP.class,
                    t -> t == src || t.getDistance(player) <= CONFIG.ddMaxBroadcastDistance())
                .forEach(t -> t.connection.sendPacket(new SPacketChat(message, ChatType.GAME_INFO)));
        }
    }

    static void leaveDeathsDoor(EntityPlayer player) {
        player.getEntityData().setBoolean(NBT_onDeathsDoor, false);

        clearStatuses(player);
        applyPenalty(player);

        ((WorldServer) player.getEntityWorld()).spawnParticle(
            EnumParticleTypes.END_ROD,
            player.posX,
            player.posY + 1.0,
            player.posZ,
            15,
            0.3, 0.5, 0.3,
            0.2);
    }

    private static void clearStatuses(EntityPlayer player) {
        //Remove effects
        for (ImmutablePair<Potion, Integer> entry : CONFIG.ddEffects()) {
            player.removePotionEffect(entry.left);
        }
        player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1, 0));
        player.removePotionEffect(MobEffects.WITHER);
    }

    private static void applyPenalty(EntityPlayer player) {
        for (ImmutablePair<Potion, ImmutablePair<Integer, Integer>> entry : CONFIG.ddPenaltyEffects()) {
            player.removePotionEffect(entry.left);
            player.addPotionEffect(new PotionEffect(entry.left, entry.right.left, entry.right.right, false, true));
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        handler = new DDEventHandler();
        if (event.getSide() == Side.SERVER) {
            registerHandler();
        } else {
            MinecraftForge.EVENT_BUS.register(new BaseEventHandler());
        }
        CONFIG = new DeathsDoorLoadableConfig();
        LOGGER.info("{} initialized.", Tags.MOD_NAME);
        //registerHandler();
    }

    static void registerHandler() {
        if (!handlerRegistered) {
            MinecraftForge.EVENT_BUS.register(handler);
            handlerRegistered = true;
        }
    }
}
