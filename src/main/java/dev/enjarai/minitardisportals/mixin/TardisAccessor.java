package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tardis.class)
public interface TardisAccessor {
    @Accessor
    void setInteriorDoorPosition(BlockPos pos);

    @Accessor
    BlockPos getInteriorDoorPosition();
}
