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

    default Text ddMessageResist(Text playerName) {
        return Text.of(ddTranslationResist().replace("{{name}}", playerName.getString()))
            .getWithStyle(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }

    default Text ddMessageResistNS(Text playerName) {
        return Text.of(ddTranslationResist().replace("{{name}}", playerName.getString()));
    }

    String ddTranslationResist();

    int ddTranslationColor();

    default Text ddMessage(Text playerName) {
        return Text.of(ddTranslation().replace("{{name}}", playerName.getString()))
            .getWithStyle(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }
    default Text ddMessageNS(Text playerName) {
        return Text.of(ddTranslation().replace("{{name}}", playerName.getString()));
    }

    String ddTranslation();

    default Text ddMessage(Text playerName, Text attackerName) {
        return Text.of(ddTranslationAttacker().replace("{{name}}", playerName.getString())
                .replace("{{attacker}}", attackerName.getString()))
            .getWithStyle(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }
    default Text ddMessageNS(Text playerName, Text attackerName) {
        return Text.of(ddTranslationAttacker().replace("{{name}}", playerName.getString())
                .replace("{{attacker}}", attackerName.getString()));
    }

    String ddTranslationAttacker();

    float ddResist();

    float ddMaxBroadcastDistance();

    boolean ddGlobalBroadcastMessage();

    default void reload() {
    }
}
