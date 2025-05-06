# Death's Door
A serverside fabric mod that changes the way players dropping to "no health" is handled. Instead of the player dying immediately upon dropping to no health, they are instead put in a "psuedo no-health" state wherefrom they can then be killed, however, can be healed out of it.

### A story about the idea behind the packet manipulation

When designing the concept for this mod, initially the idea was to have players drop to 0.0 health and this worked fine on client side (i.e. the health bar would be empty and all would work) but as a server-side mod this did not function. This is because even if the packet sent to the player informing them about their death (DeathMessageS2CPacket) is not sent, the packet informing them about their health will cause the client to kill the player.

If instead a threshold of, let's say, 0.01f is used in order to prevent the client from ever being on 0.0 health, the client will have half a heart visible in their health bar. While this works, I did not like very much and was not what I envisioned. Thus, I started thinking about ways to "hack" around this.

At some point in the past I heard that the wither effect makes health black in order to make it harder to see it. As such, an idea arose to apply wither to the client when they are on 0.01f in order to hide the last heart. The only problem is that wither deals DOT, which by the mod's logic would immediately kill the player (since they would be on death's door).

In order to fix this problem, perhaps the client could be 'fooled' into having the wither effect but not take the DOT tick damage from the server. The way this was accomplished was through a 'fake' serverside effect (DeathsDoorEffect) that does nothing which is communicated to the client as the default minecraft wither effect, but since it's not the wither effect on the server, the player never takes damage from it. It is also not saved in the player nbt data since it is not recorded in the registry.