package me.ev.deathsdoor;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Custom effect used by the server only. The client is fooled into thinking that this is just a wither effect via
 * packet manipulation. The reason for this is in order to color the hearts of the client black without the damage over
 * time penalty of the wither effect. Since this custom effect does not apply its effect ever (see
 * {@link #shouldApplyEffectTickThisTick(int, int)}), it allows the client to have black hearts without then immediately
 * withering away
 */
public class DeathsDoorEffect extends MobEffect {
    public DeathsDoorEffect() {
        super(MobEffectCategory.NEUTRAL, 0x000000);
    }

    /**
     * Never do anything
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
