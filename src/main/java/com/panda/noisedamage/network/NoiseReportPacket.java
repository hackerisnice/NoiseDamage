package com.panda.noisedamage.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NoiseReportPacket(float noise) implements CustomPayload {
    public static final Id<NoiseReportPacket> ID = new Id<>(Identifier.of("noisedamage", "noise_report"));
    public static final PacketCodec<PacketByteBuf, NoiseReportPacket> CODEC =
            PacketCodec.of((value, buf) -> buf.writeFloat(value.noise),
                           buf -> new NoiseReportPacket(buf.readFloat()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
