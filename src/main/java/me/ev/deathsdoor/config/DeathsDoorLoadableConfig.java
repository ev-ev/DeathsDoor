package me.ev.deathsdoor.config;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.ev.deathsdoor.DeathsDoor.LOGGER;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DeathsDoorLoadableConfig implements DeathsDoorConfig {
    private static final Path configPath = Paths.get("config", "deaths-door-config.kv");

    private static final List<ImmutablePair<Potion, Integer>> ddEffects = new ArrayList<>();
    private static final List<ImmutablePair<Potion, ImmutablePair<Integer, Integer>>> ddPenaltyEffects =
        new ArrayList<>();
    private static SoundEvent ddSound;
    private static Boolean ddPlaySoundAround;
    private static Float ddSoundVolume;
    private static Float ddSoundAroundVolume;
    private static Float ddSoundPitch;
    private static SoundEvent ddAttackerSound;
    private static Float ddAttackerSoundVolume;
    private static Float ddAttackerSoundPitch;
    private static String ddTranslation;
    private static String ddTranslationAttacker;
    private static String ddTranslationResist;
    private static String ddTranslationColor;
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
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(":", 2);
                if (parts.length < 2) {
                    continue;
                }
                String key = parts[0].trim();
                String value = parts[1].trim();
                loadKeyValue(key, value);
            }
            //If a field is null, initialize it from defaults
            for (Field field : getClass().getDeclaredFields()) {
                try {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    Object value = field.get(null);
                    Field defaultField = DeathsDoorDefaultConfig.class.getDeclaredField(field.getName());
                    if (value instanceof List<?>) {
                        if (!((List<?>) value).isEmpty()) continue;
                        ((List<?>) value).addAll((List) defaultField.get(null));
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
                    if (value instanceof List<?>) {
                        ((List<?>) value).addAll((List) defaultField.get(null));
                    } else field.set(null, defaultField.get(null));
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
                    //boolean ddPresent = false;
                    String[] effects = value.split("\\.");
                    for (String effect : effects) {
                        String[] effect_data = effect.trim().split(",");
                        if (effect_data.length != 2) {
                            LOGGER.error("Error in config file, ddEffects malformed KV pair : {}",
                                Arrays.toString(effect_data));
                            continue;
                        }
                        Potion status = Potion.REGISTRY.getObject(new ResourceLocation(effect_data[0].trim()));
                        // if (effect_data[0].trim().equals("deathsdoor:dd")) {
                        //status = DeathsDoor.DD;
                        //ddPresent = true;
                        //} else {
                        if (status == null) {
                            LOGGER.error("Error in config file, ddEffects no such effect : {}", effect_data[0]);
                            continue;
                        }
                        int strength = Integer.parseInt(effect_data[1].trim());
                        ddEffects.add(ImmutablePair.of(status, strength));
                    }
                    //dd effect is not required in 1.12.2
                    //if (!ddPresent) {
                    //    LOGGER.info("DD effect missing from config! Is this a mistake? (deathsdoor:dd)");
                    //}
                    break;
                case "ddPenaltyEffects":
                    String[] penalties = value.split("\\.");
                    for (String penalty : penalties) {
                        String[] penalty_data = penalty.trim().split(",");
                        if (penalty_data.length != 3) {
                            LOGGER.error("Error in config file, ddPenaltyEffects malformed KV pair : {}",
                                Arrays.toString(penalty_data));
                            continue;
                        }
                        Potion status = Potion.REGISTRY.getObject(new ResourceLocation(penalty_data[0].trim()));
                        //if (penalty_data[0].trim().equals("deathsdoor:dd")) {
                        //    status = DeathsDoor.DD;
                        //} else {
                        if (status == null) {
                            LOGGER.error("Error in config file, ddPenaltyEffects no such effect : {}", penalty_data[0]);
                            continue;
                        }

                        int duration = Integer.parseInt(penalty_data[1].trim());
                        int strength = Integer.parseInt(penalty_data[2].trim());

                        ddPenaltyEffects.add(ImmutablePair.of(status, ImmutablePair.of(duration, strength)));
                    }
                    break;
                case "ddSound":
                    ddSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(value));
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
                    ddAttackerSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(value));
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
                    ddTranslationColor = value;
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

    private void saveConfig() {
        try (BufferedWriter w = Files.newBufferedWriter(configPath)) {
            w.write("# Effects to apply on death's door: EffectID,strength. \n");
            writeEffects(w);

            w.write("\n# Penalty effects after exiting death's door: EffectID,duration,strength.\n");
            writePenaltyEffects(w);

            w.write("\n# Sound to play for player hitting death's door\n");
            w.write("ddSound : " + ddSound.getRegistryName() + "\n");

            w.write("\n# Play sound to all players nearby (true) or only to involved player (false)\n");
            w.write("ddPlaySoundAround : " + ddPlaySoundAround + "\n");

            w.write("\n# Volume of sound to target player\n");
            w.write("ddSoundVolume : " + ddSoundVolume + "\n");

            w.write("\n# Volume of sound to players nearby target player\n");
            w.write("ddSoundAroundVolume : " + ddSoundAroundVolume + "\n");

            w.write("\n# Pitch of sound to be played\n");
            w.write("ddSoundPitch : " + ddSoundPitch + "\n");

            w.write("\n# Sound to play for attacker putting player on death's door\n");
            w.write("ddAttackerSound : " + ddAttackerSound.getRegistryName() + "\n");

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

            w.write("\n# Color for death's door broadcast message. Use color codes.\n");
            w.write("ddTranslationColor : " + ddTranslationColor + "\n");

            w.write("\n# Probability of resisting deaths door (value between 0.0 and 1.0, with 0 being no resistance)" +
                    ". Not applicable with ddTotemMode \n");
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
        for (ImmutablePair<Potion, Integer> effect : ddEffects) {
            sb.append(effect.left.getRegistryName()).append(", ").append(effect.right).append(". ");
        }
        w.write(sb + "\n");
    }

    private static void writePenaltyEffects(BufferedWriter w) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("ddPenaltyEffects : ");
        for (ImmutablePair<Potion, ImmutablePair<Integer, Integer>> penalty : ddPenaltyEffects) {
            sb.append(penalty.left.getRegistryName()).append(", ").append(penalty.right.left).append(", ")
                .append(penalty.right.right).append(". ");
        }
        w.write(sb + "\n");
    }

    @Override
    public List<ImmutablePair<Potion, Integer>> ddEffects() {
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

    @Override
    public void reload() {
        loadConfig();
    }
}