package me.ev.deathsdoor.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.ev.deathsdoor.DeathsDoor.ddHealth;

@SuppressWarnings("ConstantValue")
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    /**
     * Disable saturation healing when the player is at the threshold health
     */
    @Inject(at = @At("TAIL"), method = "isHurt", cancellable = true)
    private void injectCanFoodHeal(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && ((Player) (Object) this).getHealth() > ddHealth);
    }


}
