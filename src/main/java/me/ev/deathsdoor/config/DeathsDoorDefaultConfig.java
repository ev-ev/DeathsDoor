package me.ev.deathsdoor.config;

import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.init.MobEffects.*;

public class DeathsDoorDefaultConfig implements DeathsDoorConfig {
    static final List<ImmutablePair<Potion, Integer>> ddEffects = Arrays.asList(ImmutablePair.of(BLINDNESS, 0),
        ImmutablePair.of(SLOWNESS, 1),
        ImmutablePair.of(WEAKNESS, 0),
        ImmutablePair.of(MINING_FATIGUE, 1));
    static final List<ImmutablePair<Potion, ImmutablePair<Integer, Integer>>> ddPenaltyEffects = Arrays.asList(
        ImmutablePair.of(HUNGER, ImmutablePair.of(15 * 20, 1)),
        ImmutablePair.of(SLOWNESS, ImmutablePair.of(15 * 20, 0)),
        ImmutablePair.of(MINING_FATIGUE, ImmutablePair.of(15 * 20, 0)));
    static final SoundEvent ddSound = SoundEvents.ENTITY_GHAST_SCREAM;
    static final boolean ddPlaySoundAround = true;
    static final float ddSoundVolume = 1.0f;
    static final float ddSoundAroundVolume = 0.04f;
    static final float ddSoundPitch = 0.8f;
    static final SoundEvent ddAttackerSound = SoundEvents.BLOCK_GLASS_BREAK;
    static final float ddAttackerSoundVolume = 1.0f;
    static final float ddAttackerSoundPitch = 0.8f;
    static final String ddTranslation = "{{name}} is on death's door!";
    static final String ddTranslationAttacker = "{{name}} is on death's door by {{attacker}}!";
    static final String ddTranslationResist = "{{name}} resists death!";
    static final String ddTranslationColor = "§c";
    static final float ddResist = 0.33f;
    static final float ddMaxBroadcastDistance = 15.0f;
    static final boolean ddGlobalBroadcastMessage = false;
    static final boolean ddTotemMode = false;

    @Override
    public List<ImmutablePair<Potion, Integer>> ddEffects() {
        System.out.println(ddEffects);
        return ddEffects;
    }

    @Override
    public List<ImmutablePair<Potion, ImmutablePair<Integer, Integer>>> ddPenaltyEffects() {
        return ddPenaltyEffects;
    }

    @Override
    public SoundEvent ddSound() {
        return ddSound;
    }

    @Override
    public boolean ddPlaySoundAround() {
        return ddPlaySoundAround;
    }

    @Override
    public float ddSoundVolume() {
        return ddSoundVolume;
    }

    @Override
    public float ddSoundAroundVolume() {
        return ddSoundAroundVolume;
    }

    @Override
    public float ddSoundPitch() {
        return ddSoundPitch;
    }

    @Override
    public SoundEvent ddAttackerSound() {
        return ddAttackerSound;
    }

    @Override
    public float ddAttackerSoundVolume() {
        return ddAttackerSoundVolume;
    }

    @Override
    public float ddAttackerSoundPitch() {
        return ddAttackerSoundPitch;
    }

    @Override
    public String ddTranslationResist() {
        return ddTranslationResist;
    }

    @Override
    public String ddTranslationColor() {
        return ddTranslationColor;
    }

    @Override
    public String ddTranslation() {
        return ddTranslation;
    }

    @Override
    public String ddTranslationAttacker() {
        return ddTranslationAttacker;
    }

    @Override
    public float ddResist() {
        return ddResist;
    }

    @Override
    public float ddMaxBroadcastDistance() {
        return ddMaxBroadcastDistance;
    }

    @Override
    public boolean ddGlobalBroadcastMessage() {
        return ddGlobalBroadcastMessage;
    }

    @Override
    public boolean ddTotemMode() {
        return ddTotemMode;
    }
}