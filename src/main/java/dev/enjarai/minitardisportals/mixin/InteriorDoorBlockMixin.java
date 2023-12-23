package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.block.InteriorDoorBlock;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.block.TardisExteriorBlockEntity;
import dev.enjarai.minitardisportals.MiniTardisPortals;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InteriorDoorBlock.class)
public abstract class InteriorDoorBlockMixin implements TardisAware {
    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    @Inject(
            method = "onUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void openDoorInstead(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()) {
            getTardis(world).ifPresent(tardis ->
                    MiniTardisPortals.openDoorFromInterior(tardis, world,
                            state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down(), state.get(FacingBlock.FACING)));
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
