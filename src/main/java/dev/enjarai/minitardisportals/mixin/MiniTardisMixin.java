package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.MiniTardis;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MiniTardis.class)
public abstract class MiniTardisMixin {
    /**
     * @author me
     * @reason immersiveportals doesnt like polymer much
     */
    @Overwrite
    public static boolean playerIsRealGamer(ServerPlayNetworkHandler player) {
        return true;
    }
}
