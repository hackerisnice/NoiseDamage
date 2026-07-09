package com.panda.noisedamage.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NoiseSyncPacket(float noise) implements CustomPayload {
    public static final Id<NoiseSyncPacket> ID = new Id<>(Identifier.of("noisedamage", "noise_sync"));
    public static final PacketCodec<PacketByteBuf, NoiseSyncPacket> CODEC =
            PacketCodec.of((value, buf) -> buf.writeFloat(value.noise),
                           buf -> new NoiseSyncPacket(buf.readFloat()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
