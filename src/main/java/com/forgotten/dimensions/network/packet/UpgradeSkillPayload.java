package com.forgotten.dimensions.network.packet;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** C2S : dépenser un point de compétence. */
public record UpgradeSkillPayload(String skillId) implements CustomPayload {
    public static final Id<UpgradeSkillPayload> ID = new Id<>(ForgottenDimensions.id("upgrade_skill"));
    public static final PacketCodec<RegistryByteBuf, UpgradeSkillPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.string(64), UpgradeSkillPayload::skillId, UpgradeSkillPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
