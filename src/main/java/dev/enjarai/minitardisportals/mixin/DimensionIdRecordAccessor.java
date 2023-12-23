package dev.enjarai.minitardisportals.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import qouteall.q_misc_util.dimension.DimensionIdRecord;

@Mixin(DimensionIdRecord.class)
public interface DimensionIdRecordAccessor {
    @Accessor(value = "idMap", remap = false)
    BiMap<RegistryKey<World>, Integer> getIdMap();

    @Accessor(value = "inverseMap", remap = false)
    BiMap<Integer, RegistryKey<World>> getInverseMap();
}
