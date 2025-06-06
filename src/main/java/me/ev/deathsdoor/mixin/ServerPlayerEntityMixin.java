package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoorEffect;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static me.ev.deathsdoor.DeathsDoor.*;
import static net.minecraft.entity.effect.StatusEffects.REGENERATION;
import static net.minecraft.entity.effect.StatusEffects.WITHER;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    @Unique
    boolean isOnDeathsDoor = false;
    @Unique
    boolean die = false;
    @Unique
    private float lastHealth = 0.0f;
    @Unique
    private boolean init = false;

    /**
     * Record health before damage
     */
    @Inject(at = @At("HEAD"), method = "damage")
    private void injectHeadApplyDamage(ServerWorld world, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        lastHealth = player.getHealth();
    }

    /**
     * Based on health before damage and after damage, determine if player was dropped below (or to) death's door. If
     * is on death's door, if dealt damage, attempt to resist
     */
    @Inject(at = @At("TAIL"), method = "damage")
    private void injectTailApplyDamage(ServerWorld world, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (isOnDeathsDoor) {
            if (cir.getReturnValue()) {
                if (CONFIG.ddTotemMode()) {
                    if (tryUseDeathProtectorAccessor(source)) {
                        resistDeathsDoor(source);
                    } else {
                        die = true;
                        player.onDeath(source);
                    }
                } else {
                    if (CONFIG.ddResist() != 0 && (CONFIG.ddResist() > RAND.nextFloat())) resistDeathsDoor(source);
                    else if (!tryUseDeathProtectorAccessor(source)) {
                        die = true;
                        player.onDeath(source);
                    }
                }
            }
        } else {
            if (!CONFIG.ddTotemMode()) {
                if (lastHealth > ddHealth && player.getHealth() <= ddHealth) enterDeathsDoor(source);
            } else {
                if (player.getHealth() == 0.0f) {
                    if (tryUseDeathProtectorAccessor(source)) {
                        enterDeathsDoor(source);
                    } else {
                        die = true;
                        player.onDeath(source);
                    }
                }
            }
        }

    }

    /**
     * Same as enterDeathsDoor but call a different chat message
     */
    @Unique
    private void resistDeathsDoor(DamageSource source) {
        isOnDeathsDoor = true;
        player.setHealth(ddHealth);
        applyStatuses();
        onDeathsDoorFX(source);
        resistDeathsDoorChat(source);

    }

    /**
     * Clamp player health to the threshold (so they don't die if the health was set to zero for example), apply status
     * effects (including {@link DeathsDoorEffect}) and play sounds to player (and attacker if any).
     */
    @Unique
    private void enterDeathsDoor(DamageSource source) {
        isOnDeathsDoor = true;
        player.setHealth(ddHealth);
        applyStatuses();
        if (init) {
            onDeathsDoorFX(source);
            onDeathsDoorChat(source);
        }
    }

    /**
     * Apply status effects (and remove regeneration as that would break the balance of the mod)
     */
    @Unique
    private void applyStatuses() {
        player.removeStatusEffect(REGENERATION); //Hacky but regeneration is too strong
        for (ImmutablePair<RegistryEntry<StatusEffect>, Integer> entry : CONFIG.ddEffects()) {
            player.removeStatusEffect(entry.left);
            player.addStatusEffect(new StatusEffectInstance(entry.left, -1, entry.right, false, false, false), player);
        }
    }

    @Unique
    private void onDeathsDoorFX(DamageSource source) {
        if (CONFIG.ddPlaySoundAround()) {
            player.playSoundToPlayer(SoundEvent.of(CONFIG.ddSound()), SoundCategory.PLAYERS, CONFIG.ddSoundVolume(), CONFIG.ddSoundPitch());
            player.getServerWorld().playSoundFromEntity(player, player, SoundEvent.of(CONFIG.ddSound()), SoundCategory.PLAYERS, CONFIG.ddSoundAroundVolume(), CONFIG.ddSoundPitch());
        } else {
            player.playSoundToPlayer(SoundEvent.of(CONFIG.ddSound()), SoundCategory.PLAYERS, CONFIG.ddSoundVolume(), CONFIG.ddSoundPitch());
        }

        if (source != null &&
            source.getAttacker() != null &&
            !source.getAttacker().equals(player) &&
            source.getAttacker().isPlayer()) {
            PlayerEntity attacker = (PlayerEntity) source.getAttacker();
            attacker.playSoundToPlayer(SoundEvent.of(CONFIG.ddAttackerSound()), SoundCategory.PLAYERS, CONFIG.ddAttackerSoundVolume(), CONFIG.ddAttackerSoundPitch());
        }

        player.getServerWorld().spawnParticles(ParticleTypes.RAID_OMEN,
            player.getX(),
            player.getY(),
            player.getZ(),
            10,
            0.0,
            1.0,
            0.0,
            2.0);
    }

    @Unique
    private void resistDeathsDoorChat(DamageSource source) {
        broadcast(CONFIG.ddMessageResist(player.getName()), source);
        if (CONFIG.ddGlobalBroadcastMessage()) {
            Objects.requireNonNull(player.getServer()).getPlayerManager()
                .broadcast(CONFIG.ddMessageResistNS(player.getName()), false);
        }
    }

    @Unique
    private void onDeathsDoorChat(DamageSource source) {
        if (source != null && source.getAttacker() != null && !source.getAttacker().equals(player)) {
            broadcast(CONFIG.ddMessage(player.getName(), source.getAttacker().getName()), source);
            if (CONFIG.ddGlobalBroadcastMessage()) {
                Objects.requireNonNull(player.getServer()).getPlayerManager()
                    .broadcast(CONFIG.ddMessageNS(player.getName(), source.getAttacker().getName()), false);
            }
        } else if (!CONFIG.ddTranslation().isEmpty()) {
            broadcast(CONFIG.ddMessage(player.getName()), source);
            if (CONFIG.ddGlobalBroadcastMessage()) {
                Objects.requireNonNull(player.getServer()).getPlayerManager()
                    .broadcast(CONFIG.ddMessageNS(player.getName()), false);
            }
        }
    }

    @Unique
    private void broadcast(Text message, @Nullable DamageSource source) {
        ServerPlayerEntity src;
        if (source != null && source.getAttacker() != null && source.getAttacker().isPlayer()) {
            src = (ServerPlayerEntity) source.getAttacker();
        } else {
            src = null;
        }

        if (CONFIG.ddMaxBroadcastDistance() == 0.0f) {
            player.sendMessage(message, true);
            if (src != null) src.sendMessage(message, true);
        } else if (CONFIG.ddMaxBroadcastDistance() == -1.0f) {
            Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message, true);
        } else {
            player.getServerWorld().getPlayers(t -> t == src || t.distanceTo(player) <= CONFIG.ddMaxBroadcastDistance())
                .forEach(t -> t.sendMessage(message, true));
        }
    }

    /**
     * When the player has status effects removed, the server sends a
     * {@link net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket}, which will crash the client if
     * allowed to send the {@link DeathsDoorEffect}. Thus, before that happens, check the effects to be removed and if
     * they contain the effect, cancel the function, replace the effect with {@link StatusEffects#WITHER} and re-call
     * the function.
     */
    @Inject(at = @At("HEAD"), method = "onStatusEffectsRemoved", cancellable = true)
    private void injectionStatusEffectsRemoved(Collection<StatusEffectInstance> effects, CallbackInfo ci) {
        if (effects.stream().anyMatch(t -> t.getEffectType() == DD)) {
            ci.cancel();
            List<StatusEffectInstance> newList = effects.stream()
                .map(t -> t.getEffectType() != DD ? t : new StatusEffectInstance(WITHER, -1, 0, false, false, false))
                .toList();
            onStatusEffectsRemoved(newList);
        }
    }

    @Shadow
    protected void onStatusEffectsRemoved(Collection<StatusEffectInstance> effects) {
    }

    /**
     * Prevent the player from dying unless they are on death's door
     */
    @Override
    public void injectIsDead(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && die);
    }

    /**
     * If the player ever has health above the threshold, leave death's door. If they ever happen to be on or below it
     * (e.g. reconnecting after disconnecting on death's door) put them on death's door.
     */
    @Override
    public void injectBaseTick(CallbackInfo c1) {
        float h = player.getHealth();

        if (die) {
            player.setHealth(0);
        }

        if (h == 0.0f) { //Bug fix for disconnect revival glitch
            die = true;
        } else if (h <= ddHealth && !isOnDeathsDoor) {
            enterDeathsDoor(null);
        } else if (h > ddHealth && isOnDeathsDoor) {
            leaveDeathsDoor();
        }

        init = true;

        if (isOnDeathsDoor) {
            player.getServerWorld().spawnParticles(ParticleTypes.RAID_OMEN,
                player.getX(),
                player.getY(),
                player.getZ(),
                1,
                0.0,
                1.0,
                0.0,
                1.0);
        }
    }

    /**
     * Clear bad (infinite) statuses from death's door and apply penalty statuses
     */
    @Unique
    private void leaveDeathsDoor() {
        isOnDeathsDoor = false;
        clearStatuses();
        applyPenalty();
        player.getServerWorld().spawnParticles(ParticleTypes.TRIAL_OMEN,
            player.getX(),
            player.getY(),
            player.getZ(),
            10,
            0.0,
            1.0,
            0.0,
            2.0);
    }

    /**
     * Remove the infinite length statuses
     */
    @Unique
    private void clearStatuses() {
        for (ImmutablePair<RegistryEntry<StatusEffect>, Integer> entry : CONFIG.ddEffects()) {
            player.removeStatusEffect(entry.left);
        }
    }

    /**
     * Add the temporary statuses
     */
    @Unique
    private void applyPenalty() {
        for (ImmutablePair<RegistryEntry<StatusEffect>, ImmutablePair<Integer, Integer>> entry :
            CONFIG.ddPenaltyEffects()) {
            player.removeStatusEffect(entry.left);
            player.addStatusEffect(new StatusEffectInstance(entry.left,
                entry.right.left,
                entry.right.right,
                false,
                false,
                true));
        }
    }
}
