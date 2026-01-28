package me.ev.deathsdoor;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Custom effect used by the server only. The client is fooled into thinking that this is just a wither effect via
 * packet manipulation. The reason for this is in order to color the hearts of the client black without the damage over
 * time penalty of the wither effect. Since this custom effect does not apply its effect ever (see
 * {@link #canApplyUpdateEffect(int, int)}), it allows the client to have black hearts without then immediately
 * withering away
 */
public class DeathsDoorEffect extends StatusEffect {
    public DeathsDoorEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x000000);
    }

    /**
     * Never do anything
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
