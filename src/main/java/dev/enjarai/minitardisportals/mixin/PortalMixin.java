package dev.enjarai.minitardisportals.mixin;

import dev.enjarai.minitardis.ModCCAComponents;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardisportals.duck.TardisLinkedPortal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.Optional;
import java.util.UUID;

@Mixin(Portal.class)
public abstract class PortalMixin extends Entity implements TardisLinkedPortal {
    public PortalMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique @Nullable
    private UUID linkedTardis;
    @Unique @Nullable
    private BlockPos anchorPos;

    @Unique
    private Optional<Tardis> getTardis() {
        var server = getWorld().getServer();
        if (linkedTardis != null && server != null) {
            var holder = ModCCAComponents.TARDIS_HOLDER.get(server.getSaveProperties());
            return holder.getTardis(linkedTardis);
        }
        return Optional.empty();
    }

    @Override
    public void mini_tardis_portals$linkTardis(Tardis tardis, BlockPos anchorPos) {
        this.linkedTardis = tardis.uuid();
        this.anchorPos = anchorPos;
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("RETURN")
    )
    private void readTardisFromNbt(NbtCompound compoundTag, CallbackInfo ci) {
        if (compoundTag.contains("mini_tardis_portals:linked_tardis")) {
            linkedTardis = compoundTag.getUuid("mini_tardis_portals:linked_tardis");
        }
        if (compoundTag.contains("mini_tardis_portals:anchor_pos")) {
            anchorPos = NbtHelper.toBlockPos(compoundTag, "mini_tardis_portals:anchor_pos").orElse(null);
        }
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("RETURN")
    )
    private void writeTardisToNbt(NbtCompound compoundTag, CallbackInfo ci) {
        if (linkedTardis != null) {
            compoundTag.putUuid("mini_tardis_portals:linked_tardis", linkedTardis);
        }
        if (anchorPos != null) {
            compoundTag.put("mini_tardis_portals:anchor_pos", NbtHelper.fromBlockPos(anchorPos));
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void removeIfTardisClosedOrGone(CallbackInfo ci) {
        if (!getWorld().isClient() && anchorPos != null && linkedTardis != null) {
            var anchorState = getWorld().getBlockState(anchorPos);
            if (getTardis().map(tardis -> !tardis.isDoorOpen()).orElse(false) ||
                    !(anchorState.isOf(ModBlocks.TARDIS_EXTERIOR) || anchorState.isOf(ModBlocks.INTERIOR_DOOR))) {
                discard();
            }
        }
    }
}
