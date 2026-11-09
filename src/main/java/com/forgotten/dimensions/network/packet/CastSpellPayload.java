package com.forgotten.dimensions.network.packet;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** C2S : lancer un sort (le serveur vérifie niveau, compétence, mana et recharge). */
public record CastSpellPayload(String spellId) implements CustomPayload {
    public static final Id<CastSpellPayload> ID = new Id<>(ForgottenDimensions.id("cast_spell"));
    public static final PacketCodec<RegistryByteBuf, CastSpellPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.string(64), CastSpellPayload::spellId, CastSpellPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
