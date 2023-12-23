package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.block.TardisExteriorBlock;
import dev.enjarai.minitardis.block.TardisExteriorBlockEntity;
import dev.enjarai.minitardisportals.MiniTardisPortals;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.DirectionProperty;
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

@Mixin(TardisExteriorBlock.class)
public abstract class TardisExteriorBlockMixin {
    @Shadow @Final public static DirectionProperty FACING;

    @Inject(
            method = "onUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void openDoorInstead(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()
                && hand == Hand.MAIN_HAND
                && hit.getSide() == state.get(FACING)
                && world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity
                && blockEntity.getLinkedTardis() != null) {

            var tardis = blockEntity.getLinkedTardis();
            MiniTardisPortals.toggleDoorFromExterior(tardis, world, pos, state.get(FACING));
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
