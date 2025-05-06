package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoorEffect;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

import static me.ev.deathsdoor.DeathsDoor.DD;
import static me.ev.deathsdoor.DeathsDoor.ddHealth;
import static net.minecraft.entity.effect.StatusEffects.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    @Unique
    private final List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> effects = List.of(ImmutablePair.of(DD, 0),
            ImmutablePair.of(DARKNESS, 0),
            ImmutablePair.of(SLOWNESS, 1),
            ImmutablePair.of(WEAKNESS, 0),
            ImmutablePair.of(MINING_FATIGUE, 1));
    @Unique
    private final List<ImmutablePair<RegistryEntry<StatusEffect>, Integer>> penalty_effects =
            List.of(ImmutablePair.of(HUNGER, 1), ImmutablePair.of(SLOWNESS, 0), ImmutablePair.of(MINING_FATIGUE, 0));
    @Unique
    boolean isOnDeathsDoor = false;
    @Unique
    private float lastHealth = 0.0f;

    /**
     * Record health before damage
     */
    @Inject(at = @At("HEAD"), method = "damage")
    private void injectHeadApplyDamage(ServerWorld world, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        lastHealth = player.getHealth();
    }

    /**
     * Based on health before damage and after damage, determine if player was dropped below (or to) death's door
     */
    @Inject(at = @At("TAIL"), method = "damage")
    private void injectTailApplyDamage(ServerWorld world, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (lastHealth > ddHealth && player.getHealth() <= ddHealth && !isOnDeathsDoor) {
            //Entered Death's Door
            onDeathsDoor(source);
        }
    }

    /**
     * Clamp player health to the threshold (so they don't die if the health was set to zero for example), apply status
     * effects (including {@link DeathsDoorEffect}) and play sounds to player (and attacker if any).
     */
    @Unique
    private void onDeathsDoor(DamageSource source) {
        isOnDeathsDoor = true;
        player.setHealth(ddHealth);
        applyStatuses();
        player.playSoundToPlayer(SoundEvent.of(Identifier.of("block.bell.use")), SoundCategory.PLAYERS, 1.0f, 0.8f);
        //new DDisplayEntity(player); //TODO add text particle?
        if (source != null && source.getAttacker() != null && !source.getAttacker().equals(player)) {
            if (source.getAttacker().isPlayer()) {
                PlayerEntity attacker = (PlayerEntity) source.getAttacker();
                attacker.playSoundToPlayer(SoundEvent.of(Identifier.of("block.glass.break")),
                        SoundCategory.PLAYERS,
                        1.0f,
                        0.8f);
            }
        }
    }

    /**
     * Apply status effects (and remove regeneration as that would break the balance of the mod)
     */
    @Unique
    private void applyStatuses() {
        player.removeStatusEffect(REGENERATION); //Hacky but regeneration is too strong
        for (ImmutablePair<RegistryEntry<StatusEffect>, Integer> entry : effects) {
            player.removeStatusEffect(entry.left);
            player.addStatusEffect(new StatusEffectInstance(entry.left, -1, entry.right, false, false, false), player);
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
            List<StatusEffectInstance> newList = effects.stream().map(t -> t.getEffectType() != DD ? t
                                                                                                   :
                                                                           new StatusEffectInstance(
                                                                                                           WITHER,
                                                                                                           -1,
                                                                                                           0,
                                                                                                           false,
                                                                                                           false,
                                                                                                           false))
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
        cir.setReturnValue(cir.getReturnValue() && isOnDeathsDoor);

    }

    /**
     * If the player ever has health above the threshold, leave death's door. If they ever happen to be on or below it
     * (e.g. reconnecting after disconnecting on death's door) put them on death's door.
     */
    @Override
    public void injectBaseTick(CallbackInfo c1) {
        float h = player.getHealth();
        if (h > ddHealth && isOnDeathsDoor) {
            leaveDeathsDoor();
        }
        if (h <= ddHealth && !isOnDeathsDoor) {
            onDeathsDoor(null);
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
    }

    /**
     * Remove the infinite length statuses
     */
    @Unique
    private void clearStatuses() {
        for (ImmutablePair<RegistryEntry<StatusEffect>, Integer> entry : effects) {
            player.removeStatusEffect(entry.left);
        }
    }

    /**
     * Add the temporary statuses
     */
    @Unique
    private void applyPenalty() {
        for (ImmutablePair<RegistryEntry<StatusEffect>, Integer> entry : penalty_effects) {
            player.removeStatusEffect(entry.left);
            player.addStatusEffect(new StatusEffectInstance(entry.left, 15 * 20, entry.right, false, false, true));
        }
    }
}
