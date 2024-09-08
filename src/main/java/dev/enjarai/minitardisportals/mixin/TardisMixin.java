package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardisportals.MiniTardisPortals;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

@Mixin(Tardis.class)
public abstract class TardisMixin {
    @Shadow public abstract MinecraftServer getServer();

    @Shadow
    @Nullable
    RuntimeWorldHandle interiorWorld;

    @Inject(
            method = "initializeInteriorWorld",
            at = @At("TAIL"),
            remap = false
    )
    private void initializeInteriorWorld(CallbackInfo ci) {
        MiniTardisPortals.initWorld(getServer(), interiorWorld.asWorld());
    }
}
