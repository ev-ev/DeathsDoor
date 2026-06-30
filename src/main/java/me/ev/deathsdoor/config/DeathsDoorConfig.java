package me.ev.deathsdoor.config;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

public interface DeathsDoorConfig {


    List<ImmutablePair<Holder<MobEffect>, Integer>> ddEffects();

    List<ImmutablePair<Holder<MobEffect>, ImmutablePair<Integer, Integer>>> ddPenaltyEffects();

    Identifier ddSound();

    boolean ddPlaySoundAround();

    float ddSoundVolume();

    float ddSoundAroundVolume();

    float ddSoundPitch();

    Identifier ddAttackerSound();

    float ddAttackerSoundVolume();

    float ddAttackerSoundPitch();

    default Component ddMessageResist(Component playerName) {
        return Component.nullToEmpty(ddTranslationResist().replace("{{name}}", playerName.getString()))
            .toFlatList(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }

    String ddTranslationResist();

    int ddTranslationColor();

    default Component ddMessageResistNS(Component playerName) {
        return Component.nullToEmpty(ddTranslationResist().replace("{{name}}", playerName.getString()));
    }

    default Component ddMessage(Component playerName) {
        return Component.nullToEmpty(ddTranslation().replace("{{name}}", playerName.getString()))
            .toFlatList(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }

    String ddTranslation();

    default Component ddMessageNS(Component playerName) {
        return Component.nullToEmpty(ddTranslation().replace("{{name}}", playerName.getString()));
    }

    default Component ddMessage(Component playerName, Component attackerName) {
        return Component.nullToEmpty(ddTranslationAttacker().replace("{{name}}", playerName.getString())
                .replace("{{attacker}}", attackerName.getString()))
            .toFlatList(Style.EMPTY.withColor(ddTranslationColor())).getFirst();
    }

    String ddTranslationAttacker();

    default Component ddMessageNS(Component playerName, Component attackerName) {
        return Component.nullToEmpty(ddTranslationAttacker().replace("{{name}}", playerName.getString())
            .replace("{{attacker}}", attackerName.getString()));
    }

    float ddResist();

    float ddMaxBroadcastDistance();

    boolean ddGlobalBroadcastMessage();

    boolean ddTotemMode();

    default void reload() {
    }
}
