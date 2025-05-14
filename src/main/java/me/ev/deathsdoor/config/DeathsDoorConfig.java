package me.ev.deathsdoor.config;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

public interface DeathsDoorConfig {
    List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects();

    List<ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>>> ddPenaltyEffects();

    Identifier ddSound();

    Identifier ddAttackerSound();

    default Text ddMessage(Text playerName) {
        return Text.of(ddTranslation().replace("{{name}}", playerName.getString()))
                   .getWithStyle(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }

    String ddTranslation();

    int ddTranslationColor();

    default void reload() {

    }
}
