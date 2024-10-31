package dev.enjarai.minitardisportals.net;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Set;

public record WorldKeysPacket(Set<RegistryKey<World>> keys) {
    public static final StructEndec<WorldKeysPacket> ENDEC = StructEndecBuilder.of(
            CodecUtils.toEndec(RegistryKey.createCodec(RegistryKeys.WORLD)).setOf().fieldOf("keys", WorldKeysPacket::keys),
            WorldKeysPacket::new
    );
}
