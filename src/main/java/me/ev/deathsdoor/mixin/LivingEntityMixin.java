package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoor;
import me.ev.deathsdoor.mixin.ServerPlayerMixin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.storage.ValueOutput;
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
     * Used in {@link ServerPlayerMixin#injectIsDead}
     */
    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(at = @At("TAIL"), method = "isDeadOrDying", cancellable = true)
    protected void injectIsDead(CallbackInfoReturnable<Boolean> cir) {
    }

    /**
     * Used in {@link ServerPlayerMixin#injectBaseTick}
     */
    @Inject(at = @At("HEAD"), method = "baseTick")
    protected void injectBaseTick(CallbackInfo ci) {
    }

    /**
     * When saving player data, attempting to record the {@link DeathsDoor#DD} effect into NBT fails since it is not in
     * the registry. Thus, it is hot-removed from the effects before processing, then added back.
     */
    @Inject(at = @At("HEAD"), method = "addAdditionalSaveData", cancellable = true)
    public void injectWriteCustomData(ValueOutput view, CallbackInfo ci) {
        LivingEntity ts = (LivingEntity) (Object) this;
        if (ts.hasEffect(DD)) {
            ci.cancel();
            MobEffectInstance effect = ts.removeEffectNoUpdate(DD);

            addAdditionalSaveData(view);

            ts.addEffect(effect);
        }
    }

    @Shadow
    protected abstract void addAdditionalSaveData(ValueOutput view);


    @Unique
    public boolean tryUseDeathProtectorAccessor(DamageSource source) {
        return checkTotemDeathProtection(source);
    }

    @Shadow
    protected abstract boolean checkTotemDeathProtection(DamageSource source);
}
