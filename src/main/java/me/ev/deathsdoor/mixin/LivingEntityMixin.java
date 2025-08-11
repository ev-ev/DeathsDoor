package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.ev.deathsdoor.DeathsDoor.DD;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Used in {@link ServerPlayerEntityMixin#injectIsDead}
     */
    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(at = @At("TAIL"), method = "isDead", cancellable = true)
    protected void injectIsDead(CallbackInfoReturnable<Boolean> cir) {
    }

    /**
     * Used in {@link ServerPlayerEntityMixin#injectBaseTick}
     */
    @Inject(at = @At("HEAD"), method = "baseTick")
    protected void injectBaseTick(CallbackInfo ci) {
    }

    /**
     * When saving player data, attempting to record the {@link DeathsDoor#DD} effect into NBT fails since it is not in
     * the registry. Thus, it is hot-removed from the effects before processing, then added back.
     */
    @Inject(at = @At("HEAD"), method = "writeCustomData", cancellable = true)
    public void injectWriteCustomData(WriteView view, CallbackInfo ci) {
        LivingEntity ts = (LivingEntity) (Object) this;
        if (ts.hasStatusEffect(DD)) {
            ci.cancel();
            StatusEffectInstance effect = ts.removeStatusEffectInternal(DD);

            writeCustomData(view);

            ts.addStatusEffect(effect);
        }
    }

    @Shadow
    protected abstract void writeCustomData(WriteView view);


    @Unique
    public boolean tryUseDeathProtectorAccessor(DamageSource source) {
        return tryUseDeathProtector(source);
    }

    @Shadow
    protected abstract boolean tryUseDeathProtector(DamageSource source);
}
