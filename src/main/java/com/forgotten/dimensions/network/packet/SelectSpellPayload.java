package com.forgotten.dimensions.network.packet;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** C2S : choisir le sort actif. */
public record SelectSpellPayload(String spellId) implements CustomPayload {
    public static final Id<SelectSpellPayload> ID = new Id<>(ForgottenDimensions.id("select_spell"));
    public static final PacketCodec<RegistryByteBuf, SelectSpellPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.string(64), SelectSpellPayload::spellId, SelectSpellPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
