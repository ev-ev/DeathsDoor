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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.ev.deathsdoor.DeathsDoor.LOGGER;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DeathsDoorLoadableConfig implements DeathsDoorConfig {
    private static final Path configPath = Path.of("config/deaths-door-config.kv");

    private static final List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> ddEffects = new ArrayList<>();
    private static final List<ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>>>
        ddPenaltyEffects = new ArrayList<>();
    private static Identifier ddSound;
    private static Boolean ddPlaySoundAround;
    private static Float ddSoundVolume;
    private static Float ddSoundAroundVolume;
    private static Float ddSoundPitch;
    private static Identifier ddAttackerSound;
    private static Float ddAttackerSoundVolume;
    private static Float ddAttackerSoundPitch;
    private static String ddTranslation;
    private static String ddTranslationAttacker;
    private static String ddTranslationResist;
    private static Integer ddTranslationColor;
    private static Float ddResist;
    private static Float ddMaxBroadcastDistance;
    private static Boolean ddGlobalBroadcastMessage;
    private static Boolean ddTotemMode;

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
            //If a field is null, initialize it from defaults
            for (Field field : getClass().getDeclaredFields()) {
                try {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    Object value = field.get(null);
                    Field defaultField = DeathsDoorDefaultConfig.class.getDeclaredField(field.getName());
                    if (value instanceof List<?> v) {
                        if (!v.isEmpty()) continue;
                        v.addAll((List) defaultField.get(null));
                    } else if (value == null) {
                        field.set(null, defaultField.get(null));
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {

                }
            }

            saveConfig();
        } catch (IOException e) {
            LOGGER.info("Config file missing / inaccessible, creating.");
            //Initialize fields from default
            for (Field field : getClass().getDeclaredFields()) {
                try {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    Object value = field.get(null);
                    Field defaultField = DeathsDoorDefaultConfig.class.getDeclaredField(field.getName());
                    if (value instanceof List<?> v) v.addAll((List) defaultField.get(null));
                    else field.set(null, defaultField.get(null));
                } catch (NoSuchFieldException | IllegalAccessException ignored) {

                }
            }
            saveConfig();
        }
    }

    private void loadKeyValue(String key, String value) {
        Method method = Arrays.stream(DeathsDoorDefaultConfig.class.getMethods())
            .filter(t -> t.getName().equals(key) && !key.equals("ddMessage")).findFirst().orElse(null);
        if (method == null) return;
        try {
            switch (method.getName()) {
                case "ddEffects":
                    boolean ddPresent = false;
                    String[] effects = value.split("\\.");
                    for (String effect : effects) {
                        String[] effect_data = effect.strip().split(",");
                        if (effect_data.length != 2) {
                            LOGGER.error("Error in config file, ddEffects malformed KV pair : {}",
                                Arrays.toString(effect_data));
                            continue;
                        }
                        RegistryEntry<StatusEffect> status;
                        if (effect_data[0].strip().equals("deathsdoor:dd")) {
                            status = DeathsDoor.DD;
                            ddPresent = true;
                        } else {
                            status =
                                Registries.STATUS_EFFECT.getEntry(Identifier.of(effect_data[0].strip())).orElse(null);
                        }
                        if (status == null) {
                            LOGGER.error("Error in config file, ddEffects no such effect : {}", effect_data[0]);
                            continue;
                        }
                        int strength = Integer.parseInt(effect_data[1].strip());
                        ddEffects.add(ImmutablePair.of(status, strength));
                    }
                    if (!ddPresent) {
                        LOGGER.info("DD effect missing from config! Is this a mistake? (deathsdoor:dd)");
                    }
                    break;
                case "ddPenaltyEffects":
                    String[] penalties = value.split("\\.");
                    for (String penalty : penalties) {
                        String[] penalty_data = penalty.strip().split(",");
                        if (penalty_data.length != 3) {
                            LOGGER.error("Error in config file, ddPenaltyEffects malformed KV pair : {}",
                                Arrays.toString(penalty_data));
                            continue;
                        }
                        RegistryEntry<StatusEffect> status;
                        if (penalty_data[0].strip().equals("deathsdoor:dd")) {
                            status = DeathsDoor.DD;
                        } else {
                            status =
                                Registries.STATUS_EFFECT.getEntry(Identifier.of(penalty_data[0].strip())).orElse(null);
                        }
                        if (status == null) {
                            LOGGER.error("Error in config file, ddPenaltyEffects no such effect : {}", penalty_data[0]);
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
                case "ddPlaySoundAround":
                    ddPlaySoundAround = Boolean.parseBoolean(value);
                    break;
                case "ddSoundVolume":
                    ddSoundVolume = Float.parseFloat(value);
                    break;
                case "ddSoundAroundVolume":
                    ddSoundAroundVolume = Float.parseFloat(value);
                    break;
                case "ddSoundPitch":
                    ddSoundPitch = Float.parseFloat(value);
                    break;
                case "ddAttackerSound":
                    ddAttackerSound = Identifier.of(value);
                    break;
                case "ddAttackerSoundVolume":
                    ddAttackerSoundVolume = Float.parseFloat(value);
                    break;
                case "ddAttackerSoundPitch":
                    ddAttackerSoundPitch = Float.parseFloat(value);
                    break;
                case "ddTranslation":
                    ddTranslation = value;
                    break;
                case "ddTranslationAttacker":
                    ddTranslationAttacker = value;
                    break;
                case "ddTranslationResist":
                    ddTranslationResist = value;
                    break;
                case "ddTranslationColor":
                    ddTranslationColor = Integer.parseInt(value, 16);
                    break;
                case "ddResist":
                    ddResist = Float.parseFloat(value);
                    break;
                case "ddMaxBroadcastDistance":
                    ddMaxBroadcastDistance = Float.parseFloat(value);
                    break;
                case "ddGlobalBroadcastMessage":
                    ddGlobalBroadcastMessage = Boolean.parseBoolean(value);
                    break;
                case "ddTotemMode":
                    ddTotemMode = Boolean.parseBoolean(value);
                    break;
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Error in config file, {} failed to parse", key);
        }


    }

    @SuppressWarnings("TextBlockMigration")
    private void saveConfig() {
        try (BufferedWriter w = Files.newBufferedWriter(configPath)) {
            w.write("# Effects to apply on death's door: EffectID,strength. The special DD effect applies the " +
                    "psuedo-wither effect clientside\n");
            writeEffects(w);

            w.write("\n# Penalty effects after exiting death's door: EffectID,duration,strength.\n");
            writePenaltyEffects(w);

            w.write("\n# Sound to play for player hitting death's door\n");
            w.write("ddSound : " + ddSound + "\n");

            w.write("\n# Play sound to all players nearby (true) or only to involved player (false)\n");
            w.write("ddPlaySoundAround : " + ddPlaySoundAround + "\n");

            w.write("\n# Volume of sound to target player\n");
            w.write("ddSoundVolume : " + ddSoundVolume + "\n");

            w.write("\n# Volume of sound to players nearby target player\n");
            w.write("ddSoundAroundVolume : " + ddSoundAroundVolume + "\n");

            w.write("\n# Pitch of sound to be played\n");
            w.write("ddSoundPitch : " + ddSoundPitch + "\n");

            w.write("\n# Sound to play for attacker putting player on death's door\n");
            w.write("ddAttackerSound : " + ddAttackerSound + "\n");

            w.write("\n# Volume of attacker sound to be played\n");
            w.write("ddAttackerSoundVolume : " + ddAttackerSoundVolume + "\n");

            w.write("\n# Pitch of attacker sound to be played\n");
            w.write("ddAttackerSoundPitch : " + ddAttackerSoundPitch + "\n");

            w.write("\n# Message to broadcast when player hitting death's door. Leave empty to disable. Use {{name}} " +
                    "for player name\n");
            w.write("ddTranslation : " + ddTranslation + "\n");

            w.write("\n# Like above but when player has a damage source. Use {{attacker}} for damage source.\n");
            w.write("ddTranslationAttacker : " + ddTranslationAttacker + "\n");

            w.write("\n# Like ddTranslation but when player resists death (see ddResist).\n");
            w.write("ddTranslationResist : " + ddTranslationResist + "\n");

            w.write("\n# Color for death's door broadcast message (RGB as hex)\n");
            w.write("ddTranslationColor : " + String.format("%06X", ddTranslationColor) + "\n");

            w.write("\n# Probability of resisting deaths door (value between 0.0 and 1.0, with 0 being no resistance)" +
                    ". " +
                    "Not applicable with ddTotemMode" +
                    "\n");
            w.write("ddResist : " + ddResist + "\n");

            w.write(
                "\n# Max distance to other players that will receive DD messages (0 : broadcast only for main player," +
                " -1 : broadcast to all players)\n");
            w.write("ddMaxBroadcastDistance : " + ddMaxBroadcastDistance + "\n");

            w.write("\n# Should action bar messages also be sent in global chat (true / false)\n");
            w.write("ddGlobalBroadcastMessage : " + ddGlobalBroadcastMessage + "\n");

            w.write(
                "\n# Allow players to die normally but change totem behavior to this mod's behavior. (true / false)\n");
            w.write("ddTotemMode : " + ddTotemMode + "\n");
        } catch (IOException e) {
            LOGGER.error("Failed to make config file!");
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
                .append(penalty.right.right).append(". ");
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

    @Override
    public void reload() {
        loadConfig();
    }
}
