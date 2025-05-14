package me.ev.deathsdoor.config;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

import static me.ev.deathsdoor.DeathsDoor.DD;
import static net.minecraft.entity.effect.StatusEffects.*;

public class DeathsDoorDefaultConfig implements DeathsDoorConfig {
    static final List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects =
            List.of(ImmutablePair.of(DD, 0),
                    ImmutablePair.of(DARKNESS, 0),
                    ImmutablePair.of(SLOWNESS, 1),
                    ImmutablePair.of(WEAKNESS, 0),
                    ImmutablePair.of(MINING_FATIGUE, 1));
    static final List<ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>>>
            ddPenaltyEffects = List.of(ImmutablePair.of(HUNGER, ImmutablePair.of(15 * 20, 1)),
            ImmutablePair.of(SLOWNESS, ImmutablePair.of(15 * 20, 0)),
            ImmutablePair.of(MINING_FATIGUE, ImmutablePair.of(15 * 20, 0)));
    static final Identifier ddSound = Identifier.of("block.bell.use");
    static final Identifier ddAttackerSound = Identifier.of("block.glass.break");
    static final String ddTranslation = "{{name}} is on death's door!";
    static final int ddTranslationColor = 0xd12c2c;

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
    public Identifier ddAttackerSound() {
        return ddAttackerSound;
    }

    @Override
    public String ddTranslation() {
        return ddTranslation;
    }

    @Override
    public int ddTranslationColor() {
        return ddTranslationColor;
    }
}
