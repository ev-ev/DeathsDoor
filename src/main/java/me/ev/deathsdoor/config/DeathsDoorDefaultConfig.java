package me.ev.deathsdoor.config;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

import static me.ev.deathsdoor.DeathsDoor.DD;
import static net.minecraft.entity.effect.StatusEffects.*;

public class DeathsDoorDefaultConfig implements DeathsDoorConfig {
    static final List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects = List.of(ImmutablePair.of(DD, 0),
        ImmutablePair.of(DARKNESS, 0),
        ImmutablePair.of(SLOWNESS, 1),
        ImmutablePair.of(WEAKNESS, 0),
        ImmutablePair.of(MINING_FATIGUE, 1));
    static final List<ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>>> ddPenaltyEffects =
        List.of(ImmutablePair.of(HUNGER, ImmutablePair.of(15 * 20, 1)),
            ImmutablePair.of(SLOWNESS, ImmutablePair.of(15 * 20, 0)),
            ImmutablePair.of(MINING_FATIGUE, ImmutablePair.of(15 * 20, 0)));
    static final Identifier ddSound = Identifier.of("block.bell.use");
    static final boolean ddPlaySoundAround = true;
    static final float ddSoundVolume = 1.0f;
    static final float ddSoundAroundVolume = 0.04f;
    static final float ddSoundPitch = 0.8f;
    static final Identifier ddAttackerSound = Identifier.of("block.glass.break");
    static final float ddAttackerSoundVolume = 1.0f;
    static final float ddAttackerSoundPitch = 0.8f;
    static final String ddTranslation = "{{name}} is on death's door!";
    static final String ddTranslationAttacker = "{{name}} is on death's door by {{attacker}}!";
    static final String ddTranslationResist = "{{name}} resists death!";
    static final int ddTranslationColor = 0xd12c2c;
    static final float ddResist = 0.33f;
    static final float ddMaxBroadcastDistance = 15.0f;
    static final boolean ddGlobalBroadcastMessage = false;
    static final boolean ddTotemMode = false;

    @Override
    public List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects() {
        System.out.println(ddEffects);
        return ddEffects;
    }

    @Override
    public List<ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>>> ddPenaltyEffects() {
        return ddPenaltyEffects;
    }

    @Override
    public Identifier ddSound() {
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
    public Identifier ddAttackerSound() {
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
    public int ddTranslationColor() {
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
