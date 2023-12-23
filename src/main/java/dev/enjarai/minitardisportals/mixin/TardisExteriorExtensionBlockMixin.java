package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.block.TardisExteriorBlock;
import dev.enjarai.minitardis.block.TardisExteriorBlockEntity;
import dev.enjarai.minitardis.block.TardisExteriorExtensionBlock;
import dev.enjarai.minitardisportals.MiniTardisPortals;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TardisExteriorExtensionBlock.class)
public abstract class TardisExteriorExtensionBlockMixin {
    @Inject(
            method = "onUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void openDoorInstead(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        var facing = world.getBlockState(pos.down()).get(TardisExteriorBlock.FACING);
        if (!world.isClient()
                && hand == Hand.MAIN_HAND
                && hit.getSide() == facing
                && world.getBlockEntity(pos.down()) instanceof TardisExteriorBlockEntity blockEntity
                && blockEntity.getLinkedTardis() != null) {

            var tardis = blockEntity.getLinkedTardis();
            MiniTardisPortals.toggleDoorFromExterior(tardis, world, pos.down(), facing);
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
