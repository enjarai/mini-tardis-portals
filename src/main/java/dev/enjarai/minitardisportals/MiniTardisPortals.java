package dev.enjarai.minitardisportals;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.InteriorDoorBlock;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardisportals.duck.TardisLinkedPortal;
import dev.enjarai.minitardisportals.mixin.DimensionIdRecordAccessor;
import dev.enjarai.minitardisportals.mixin.TardisAccessor;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.MiscNetworking;
import qouteall.q_misc_util.dimension.DimensionIdRecord;
import qouteall.q_misc_util.my_util.DQuaternion;
import xyz.nucleoid.fantasy.RuntimeWorld;

import java.util.Objects;

import static dev.enjarai.minitardis.component.Tardis.INTERIOR_CENTER;

public class MiniTardisPortals implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "mini_tardis_portals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static double PORTAL_OFFSET = 0.5 + 1.0 / 16.0 * 1.1;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerWorldEvents.LOAD.register((server, world) -> {
			if (world instanceof RuntimeWorld runtimeWorld) {
				var regKey = runtimeWorld.getRegistryKey();
				var id = regKey.getValue().hashCode();
				((DimensionIdRecordAccessor)DimensionIdRecord.serverRecord).getIdMap().forcePut(regKey, id);
				((DimensionIdRecordAccessor) DimensionIdRecord.serverRecord).getInverseMap().forcePut(id, regKey);
				var packet = MiscNetworking.createDimSyncPacket();
				server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.networkHandler.sendPacket(packet));
			}
		});

		PolymerResourcePackUtils.addModAssets(MOD_ID);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	private static PolymerModelData getModel(String modelPath) {
		return PolymerResourcePackUtils.requestModel(Items.BARRIER, id(modelPath));
	}

	public static void toggleDoorFromExterior(Tardis tardis, World sourceWorld, BlockPos exteriorPos, Direction sourceFacing) {
		if (tardis.isDoorOpen()) {
			if (tardis.setDoorOpen(false, false)) {
				sourceWorld.playSound(null, exteriorPos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundCategory.BLOCKS);
			}
		} else {
			if (tardis.setDoorOpen(true, false)) {
				var interiorWorld = tardis.getInteriorWorld();
				var interiorDoorPos = ((TardisAccessor) tardis).getInteriorDoorPosition();
				Direction interiorDoorFacing = Direction.NORTH;
				var interiorDoorState = interiorWorld.getBlockState(interiorDoorPos);

				do {
					if (interiorDoorState.isOf(ModBlocks.INTERIOR_DOOR) && interiorDoorState.get(InteriorDoorBlock.HALF) == DoubleBlockHalf.LOWER) {
						var facing = interiorDoorState.get(FacingBlock.FACING);
						((TardisAccessor) tardis).setInteriorDoorPosition(interiorDoorPos);

						interiorDoorFacing = facing;

						break;
					} else {
						interiorDoorPos = interiorWorld.getPointOfInterestStorage().getInSquare(
								poi -> poi.value().equals(ModBlocks.INTERIOR_DOOR_POI), INTERIOR_CENTER,
								64, PointOfInterestStorage.OccupationStatus.ANY
						).findAny().map(PointOfInterest::getPos).orElse(INTERIOR_CENTER);
						interiorDoorState = interiorWorld.getBlockState(interiorDoorPos);
					}
				} while (interiorDoorState.isOf(ModBlocks.INTERIOR_DOOR));

				openPortals(tardis, sourceWorld, exteriorPos, sourceFacing, interiorDoorPos, interiorDoorFacing);

				sourceWorld.playSound(null, exteriorPos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundCategory.BLOCKS);
				interiorWorld.playSound(null, interiorDoorPos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundCategory.BLOCKS);
			}
		}
	}

	public static void openDoorFromInterior(Tardis tardis, World interiorWorld, BlockPos interiorDoorPos, Direction interiorDoorFacing) {
		if (tardis.setDoorOpen(true, false)) {
			tardis.getCurrentLandedLocation().ifPresent(location -> {
				var exteriorWorld = location.getWorld(Objects.requireNonNull(interiorWorld.getServer()));
				var exteriorPos = location.pos();
				var exteriorFacing = location.facing();

				openPortals(tardis, exteriorWorld, exteriorPos, exteriorFacing, interiorDoorPos, interiorDoorFacing);

				exteriorWorld.playSound(null, exteriorPos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundCategory.BLOCKS);
				interiorWorld.playSound(null, interiorDoorPos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundCategory.BLOCKS);
			});
		}
	}

	public static void openPortals(Tardis tardis, World exteriorWorld, BlockPos exteriorPos, Direction exteriorFacing, BlockPos interiorPos, Direction interiorFacing) {
		var interiorWorld = tardis.getInteriorWorld();

		var sourcePos = Vec3d.ofBottomCenter(exteriorPos.up()).offset(exteriorFacing, PORTAL_OFFSET);
		var targetPos = Vec3d.ofBottomCenter(interiorPos.up()).offset(interiorFacing, PORTAL_OFFSET);

		var exteriorPortal = Portal.entityType.create(exteriorWorld);
		assert exteriorPortal != null;
		((TardisLinkedPortal) exteriorPortal).mini_tardis_portals$linkTardis(tardis, exteriorPos);
		exteriorPortal.setOriginPos(sourcePos);
		exteriorPortal.setDestinationDimension(interiorWorld.getRegistryKey());
		exteriorPortal.setDestination(targetPos);
		exteriorPortal.setOrientationAndSize(
				Vec3d.of(exteriorFacing.rotateClockwise(Direction.Axis.Y).getVector()),
				new Vec3d(0, -1, 0),
				0.99, 2
		);
		exteriorPortal.setOtherSideOrientation(DQuaternion.fromFacingVecs(
				Vec3d.of(interiorFacing.rotateClockwise(Direction.Axis.Y).getVector()),
				new Vec3d(0, -1, 0)
		));

		var interiorPortal = PortalManipulation.createReversePortal(exteriorPortal, Portal.entityType);
		((TardisLinkedPortal) interiorPortal).mini_tardis_portals$linkTardis(tardis, interiorPos);

		exteriorWorld.spawnEntity(exteriorPortal);
		interiorWorld.spawnEntity(interiorPortal);
	}
}
