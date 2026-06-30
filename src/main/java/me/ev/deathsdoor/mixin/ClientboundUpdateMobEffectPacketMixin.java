package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.ev.deathsdoor.DeathsDoor.DD;

@Mixin(ClientboundUpdateMobEffectPacket.class)
public abstract class ClientboundUpdateMobEffectPacketMixin {
    @Unique
    private final ClientboundUpdateMobEffectPacket ts = (ClientboundUpdateMobEffectPacket) (Object) this;
    @Shadow
    @Final
    private byte flags;

    /**
     * When the server sends the packets regarding status effects to the client, hotswap the {@link DeathsDoor#DD}
     * effect for the {@link MobEffects#WITHER} effect.
     */
    @Inject(at = @At("HEAD"), method = "write", cancellable = true)
    private void injectWrite(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        if (ts.getEffect() == DD) {
            ci.cancel();
            buf.writeVarInt(ts.getEntityId());
            MobEffect.STREAM_CODEC.encode(buf, MobEffects.WITHER);
            buf.writeVarInt(ts.getEffectAmplifier());
            buf.writeVarInt(ts.getEffectDurationTicks());
            buf.writeByte(flags);
        }
    }
}
