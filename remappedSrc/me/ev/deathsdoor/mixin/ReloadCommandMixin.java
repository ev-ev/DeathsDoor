package me.ev.deathsdoor.mixin;

import me.ev.deathsdoor.DeathsDoor;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ReloadCommand.class)
public abstract class ReloadCommandMixin {
    @Inject(at = @At("TAIL"), method = "tryReloadDataPacks")
    private static void injectTryReloadDataPacks(Collection<String> dataPacks, ServerCommandSource source,
                                                 CallbackInfo ci) {
        DeathsDoor.CONFIG.reload();
    }
}
