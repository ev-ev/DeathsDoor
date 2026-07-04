package me.ev.deathsdoor.config;

import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

public interface DeathsDoorConfig {


    List<ImmutablePair<Potion, Integer>> ddEffects();

    List<ImmutablePair<Potion, ImmutablePair<Integer, Integer>>> ddPenaltyEffects();

    SoundEvent ddSound();

    boolean ddPlaySoundAround();

    float ddSoundVolume();

    float ddSoundAroundVolume();

    float ddSoundPitch();

    SoundEvent ddAttackerSound();

    float ddAttackerSoundVolume();

    float ddAttackerSoundPitch();

    default TextComponentString ddMessageResist(String playerName) {
        return new TextComponentString(ddTranslationColor() +
                                       ddTranslationResist().replace("{{name}}", playerName));
    }

    String ddTranslationColor();

    String ddTranslationResist();

    //What is the difference between Resist and ResistNS???
    default TextComponentString ddMessageResistNS(String playerName) {
        return new TextComponentString(ddTranslationResist().replace("{{name}}", playerName));
    }

    default TextComponentString ddMessage(String playerName) {
        return new TextComponentString(ddTranslationColor() +
                                       ddTranslation().replace("{{name}}", playerName));
    }

    String ddTranslation();

    default TextComponentString ddMessageNS(String playerName) {
        return new TextComponentString(ddTranslation().replace("{{name}}", playerName));
    }

    default TextComponentString ddMessage(String playerName, String attackerName) {
        return new TextComponentString(ddTranslationColor() +
                                       ddTranslationAttacker().replace("{{name}}", playerName)
                                           .replace("{{attacker}}", attackerName));
    }

    String ddTranslationAttacker();

    default TextComponentString ddMessageNS(String playerName, String attackerName) {
        return new TextComponentString(ddTranslationAttacker().replace("{{name}}", playerName)
            .replace("{{attacker}}", attackerName));
    }

    float ddResist();

    float ddMaxBroadcastDistance();

    boolean ddGlobalBroadcastMessage();

    boolean ddTotemMode();

    default void reload() {
    }
}