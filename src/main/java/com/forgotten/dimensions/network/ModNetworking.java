package com.forgotten.dimensions.network;

import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.network.packet.CastSpellPayload;
import com.forgotten.dimensions.network.packet.ChooseSpecializationPayload;
import com.forgotten.dimensions.network.packet.SelectSpellPayload;
import com.forgotten.dimensions.network.packet.UpgradeSkillPayload;
import com.forgotten.dimensions.progression.ProgressionManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Paquets client → serveur. Le client ne fait que demander : chaque action est revalidée ici
 * (le serveur fait autorité). La synchronisation serveur → client passe par les attachements Fabric.
 */
public final class ModNetworking {
    private ModNetworking() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(CastSpellPayload.ID, CastSpellPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectSpellPayload.ID, SelectSpellPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpgradeSkillPayload.ID, UpgradeSkillPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChooseSpecializationPayload.ID, ChooseSpecializationPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CastSpellPayload.ID, (payload, ctx) ->
                SpellCaster.tryCast(ctx.player(), payload.spellId()));

        ServerPlayNetworking.registerGlobalReceiver(SelectSpellPayload.ID, (payload, ctx) -> {
            if (Spells.get(payload.spellId()) == null) return;
            var player = ctx.player();
            ProgressionManager.set(player, ProgressionManager.get(player).withSelectedSpell(payload.spellId()));
        });

        ServerPlayNetworking.registerGlobalReceiver(UpgradeSkillPayload.ID, (payload, ctx) -> {
            String reason = ProgressionManager.upgradeSkill(ctx.player(), payload.skillId());
            if (reason != null) {
                ctx.player().sendMessage(Text.translatable("message.forgotten_dimensions.skill." + reason).formatted(Formatting.RED), true);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ChooseSpecializationPayload.ID, (payload, ctx) -> {
            String reason = ProgressionManager.chooseSpecialization(ctx.player(), payload.tree());
            if (reason != null) {
                ctx.player().sendMessage(Text.translatable("message.forgotten_dimensions.spec." + reason).formatted(Formatting.RED), true);
            }
        });
    }
}
