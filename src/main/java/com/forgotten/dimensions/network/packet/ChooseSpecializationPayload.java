package com.forgotten.dimensions.network.packet;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** C2S : choisir une spécialisation (une seule fois, à partir du niveau 20). */
public record ChooseSpecializationPayload(String tree) implements CustomPayload {
    public static final Id<ChooseSpecializationPayload> ID = new Id<>(ForgottenDimensions.id("choose_specialization"));
    public static final PacketCodec<RegistryByteBuf, ChooseSpecializationPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.string(32), ChooseSpecializationPayload::tree, ChooseSpecializationPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
