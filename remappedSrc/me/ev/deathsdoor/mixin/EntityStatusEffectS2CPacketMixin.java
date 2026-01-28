package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.ev.deathsdoor.DeathsDoor.DD;

@Mixin(EntityStatusEffectS2CPacket.class)
public abstract class EntityStatusEffectS2CPacketMixin {
    @Unique
    private final EntityStatusEffectS2CPacket ts = (EntityStatusEffectS2CPacket) (Object) this;
    @Shadow
    @Final
    private byte flags;

    /**
     * When the server sends the packets regarding status effects to the client, hotswap the {@link DeathsDoor#DD}
     * effect for the {@link StatusEffects#WITHER} effect.
     */
    @Inject(at = @At("HEAD"), method = "write", cancellable = true)
    private void injectWrite(RegistryByteBuf buf, CallbackInfo ci) {
        if (ts.getEffectId() == DD) {
            ci.cancel();
            buf.writeVarInt(ts.getEntityId());
            StatusEffect.ENTRY_PACKET_CODEC.encode(buf, StatusEffects.WITHER);
            buf.writeVarInt(ts.getAmplifier());
            buf.writeVarInt(ts.getDuration());
            buf.writeByte(flags);
        }
    }
}
