package me.ev.deathsdoor;

import me.ev.deathsdoor.config.DeathsDoorConfig;
import me.ev.deathsdoor.config.DeathsDoorLoadableConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DeathsDoor implements ModInitializer {
    public static final String MOD_ID = "deaths-door";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Random R = new Random();
    public static final float ddHealth = 0.01f;
    public static final Random RAND = new Random();
    public static RegistryEntry<StatusEffect> DD;
    public static DeathsDoorConfig CONFIG;

    @Override
    public void onInitialize() {


        //Do not register effect so that clients without the mod installed can connect
        DD = new RegistryEntry.Direct<>(new DeathsDoorEffect());

        CONFIG = new DeathsDoorLoadableConfig();
        LOGGER.info("DeathsDoor initialized");
    }
}