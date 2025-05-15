package me.ev.deathsdoor.config;

import me.ev.deathsdoor.DeathsDoor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathsDoorLoadableConfig implements DeathsDoorConfig {
    private static final Path configPath = Path.of("config/deaths-door-config.kv");

    private static List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects = new ArrayList<>();
    private static List<ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>>> ddPenaltyEffects =
            new ArrayList<>();
    private static Identifier ddSound;
    private static Identifier ddAttackerSound;
    private static String ddTranslation;
    private static String ddTranslationAttacker;
    private static Integer ddTranslationColor;

    public DeathsDoorLoadableConfig() {
        loadConfig();
    }

    private void loadConfig() {
        try (BufferedReader r = Files.newBufferedReader(configPath)) {
            String line;

            ddEffects.clear();
            ddPenaltyEffects.clear();
            while ((line = r.readLine()) != null) {
                line = line.strip();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(":", 2);
                if (parts.length < 2) {
                    continue;
                }
                String key = parts[0].strip();
                String value = parts[1].strip();
                loadKeyValue(key, value);
            }
            if (ddEffects == null)
                ddEffects = new ArrayList<>(DeathsDoorDefaultConfig.ddEffects);
            if (ddPenaltyEffects == null)
                ddPenaltyEffects = new ArrayList<>(DeathsDoorDefaultConfig.ddPenaltyEffects);
            if (ddSound == null)
                ddSound = DeathsDoorDefaultConfig.ddSound;
            if (ddAttackerSound == null)
                ddAttackerSound = DeathsDoorDefaultConfig.ddAttackerSound;
            if (ddTranslation == null)
                ddTranslation = DeathsDoorDefaultConfig.ddTranslation;
            if (ddTranslationAttacker == null)
                ddTranslationAttacker = DeathsDoorDefaultConfig.ddTranslationAttacker;
            if (ddTranslationColor == null)
                ddTranslationColor = DeathsDoorDefaultConfig.ddTranslationColor;


            saveConfig();
        } catch (IOException e) {
            DeathsDoor.LOGGER.info("Config file missing / inaccessible, creating.");
            ddEffects = new ArrayList<>(DeathsDoorDefaultConfig.ddEffects);
            ddPenaltyEffects = new ArrayList<>(DeathsDoorDefaultConfig.ddPenaltyEffects);
            ddSound = DeathsDoorDefaultConfig.ddSound;
            ddAttackerSound = DeathsDoorDefaultConfig.ddAttackerSound;
            ddTranslation = DeathsDoorDefaultConfig.ddTranslation;
            ddTranslationAttacker = DeathsDoorDefaultConfig.ddTranslationAttacker;
            ddTranslationColor = DeathsDoorDefaultConfig.ddTranslationColor;
            saveConfig();
        }
    }

    private void loadKeyValue(String key, String value) {
        Method method = Arrays.stream(DeathsDoorDefaultConfig.class.getMethods())
                              .filter(t -> t.getName().equals(key) && !key.equals("ddMessage")).findFirst()
                              .orElse(null);
        if (method == null) return;
        try {
            switch (method.getName()) {
                case "ddEffects":
                    boolean ddPresent = false;
                    String[] effects = value.split("\\.");
                    for (String effect : effects) {
                        String[] effect_data = effect.strip().split(",");
                        if (effect_data.length != 2) {
                            DeathsDoor.LOGGER.error("Error in config file, ddEffects malformed KV pair : {}",
                                    Arrays.toString(effect_data));
                            continue;
                        }
                        RegistryEntry<StatusEffect> status;
                        if (effect_data[0].strip().equals("deathsdoor:dd")) {
                            status = DeathsDoor.DD;
                            ddPresent = true;
                        } else {
                            status = Registries.STATUS_EFFECT.getEntry(Identifier.of(effect_data[0].strip()))
                                                             .orElse(null);
                        }
                        if (status == null) {
                            DeathsDoor.LOGGER.error("Error in config file, ddEffects no such effect : {}",
                                    effect_data[0]);
                            continue;
                        }
                        int strength = Integer.parseInt(effect_data[1].strip());
                        ddEffects.add(ImmutablePair.of(status, strength));
                    }
                    if (!ddPresent) {
                        DeathsDoor.LOGGER.info("DD effect missing from config! Is this a mistake? (deathsdoor:dd)");
                    }
                    break;
                case "ddPenaltyEffects":
                    String[] penalties = value.split("\\.");
                    for (String penalty : penalties) {
                        String[] penalty_data = penalty.strip().split(",");
                        if (penalty_data.length != 3) {
                            DeathsDoor.LOGGER.error("Error in config file, ddPenaltyEffects malformed KV pair : {}",
                                    Arrays.toString(penalty_data));
                            continue;
                        }
                        RegistryEntry<StatusEffect> status;
                        if (penalty_data[0].strip().equals("deathsdoor:dd")) {
                            status = DeathsDoor.DD;
                        } else {
                            status = Registries.STATUS_EFFECT.getEntry(Identifier.of(penalty_data[0].strip()))
                                                             .orElse(null);
                        }
                        if (status == null) {
                            DeathsDoor.LOGGER.error("Error in config file, ddPenaltyEffects no such effect : {}",
                                    penalty_data[0]);
                            continue;
                        }

                        int duration = Integer.parseInt(penalty_data[1].strip());
                        int strength = Integer.parseInt(penalty_data[2].strip());

                        ddPenaltyEffects.add(ImmutablePair.of(status, ImmutablePair.of(duration, strength)));
                    }
                    break;
                case "ddSound":
                    ddSound = Identifier.of(value);
                    break;
                case "ddAttackerSound":
                    ddAttackerSound = Identifier.of(value);
                    break;
                case "ddTranslation":
                    ddTranslation = value;
                    break;
                case "ddTranslationAttacker":
                    ddTranslationAttacker = value;
                    break;
                case "ddTranslationColor":
                    ddTranslationColor = Integer.parseInt(value, 16);
                    break;
            }
        } catch (NumberFormatException e) {
            DeathsDoor.LOGGER.error("Error in config file, {} failed to parse integer", key);
        }


    }

    private void saveConfig() {
        try (BufferedWriter w = Files.newBufferedWriter(configPath)) {
            w.write("# Effects to apply on death's door: EffectID,strength. The special DD effect applies the " +
                    "psuedo-wither effect clientside\n");
            writeEffects(w);
            w.write("\n# Penalty effects after exiting death's door: EffectID,duration,strength.\n");
            writePenaltyEffects(w);
            w.write("\n# Sound to play for player hitting death's door\n");
            w.write("ddSound : " + ddSound + "\n");
            w.write("\n# Sound to play for attacker putting player on death's door\n");
            w.write("ddAttackerSound : " + ddAttackerSound + "\n");
            w.write("\n# Message to broadcast when player hitting death's door. Leave empty to disable. Use {{name}} " +
                    "for player name\n");
            w.write("ddTranslation : " + ddTranslation + "\n");
            w.write("\n# Like above but when player has a damage source. Use {{attacker}} for damage source.\n");
            w.write("ddTranslationAttacker : " + ddTranslationAttacker + "\n");
            w.write("\n# Color for death's door broadcast message (RGB as hex)\n");
            w.write("ddTranslationColor : " + String.format("%06X", ddTranslationColor) + "\n");
        } catch (IOException e) {
            DeathsDoor.LOGGER.error("Failed to make config file!");
        }
    }

    private static void writeEffects(BufferedWriter w) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("ddEffects : ");
        for (ImmutablePair<RegistryEntry<StatusEffect>, Integer> effect : ddEffects) {
            if (effect.left == DeathsDoor.DD) {
                sb.append("deathsdoor:dd, ").append(effect.right).append(". ");
                continue;
            }
            sb.append(effect.left.getIdAsString()).append(", ").append(effect.right).append(". ");
        }
        w.write(sb + "\n");
    }

    private static void writePenaltyEffects(BufferedWriter w) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("ddPenaltyEffects : ");
        for (ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>> penalty : ddPenaltyEffects) {
            if (penalty.left == DeathsDoor.DD) {
                sb.append("deathsdoor:dd, ").append(penalty.right.left).append(", ").append(penalty.right.right)
                  .append(". ");
                continue;
            }
            sb.append(penalty.left.getIdAsString()).append(", ").append(penalty.right.left).append(", ")
              .append(penalty.right.right)
              .append(". ");
        }
        w.write(sb + "\n");
    }

    @Override
    public List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects() {
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
    public String ddTranslationAttacker() {
        return ddTranslationAttacker;
    }

    @Override
    public int ddTranslationColor() {
        return ddTranslationColor;
    }

    @Override
    public void reload() {
        loadConfig();
    }
}
