package me.ev.deathsdoor.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.ev.deathsdoor.DeathsDoor.ddHealth;

@SuppressWarnings("ConstantValue")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    /**
     * Disable saturation healing when the player is at the threshold health
     */
    @Inject(at = @At("TAIL"), method = "canFoodHeal", cancellable = true)
    private void injectCanFoodHeal(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && ((PlayerEntity) (Object) this).getHealth() > ddHealth);
    }


}
