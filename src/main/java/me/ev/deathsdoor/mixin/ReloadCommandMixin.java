package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoor;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ReloadCommand.class)
public abstract class ReloadCommandMixin {
    @Inject(at = @At("TAIL"), method = "reloadPacks")
    private static void injectTryReloadDataPacks(Collection<String> dataPacks, CommandSourceStack source,
                                                 CallbackInfo ci) {
        DeathsDoor.CONFIG.reload();
    }
}
