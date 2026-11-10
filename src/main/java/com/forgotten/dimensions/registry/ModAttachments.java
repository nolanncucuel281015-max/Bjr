package com.forgotten.dimensions.registry;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.magic.ManaState;
import com.forgotten.dimensions.magic.SpellCooldowns;
import com.forgotten.dimensions.progression.PlayerProgress;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Données persistantes des joueurs (Fabric Data Attachment API). Elles sont sauvegardées avec le
 * joueur, conservées à la mort et synchronisées uniquement vers le joueur concerné.
 */
public final class ModAttachments {
    public static final AttachmentType<PlayerProgress> PROGRESS = AttachmentRegistry.create(
            ForgottenDimensions.id("progress"), b -> b
                    .initializer(() -> PlayerProgress.DEFAULT)
                    .persistent(PlayerProgress.CODEC)
                    .copyOnDeath()
                    .syncWith(PacketCodecs.codec(PlayerProgress.CODEC), AttachmentSyncPredicate.targetOnly()));

    public static final AttachmentType<ManaState> MANA = AttachmentRegistry.create(
            ForgottenDimensions.id("mana"), b -> b
                    .initializer(() -> ManaState.DEFAULT)
                    .persistent(ManaState.CODEC)
                    .copyOnDeath()
                    .syncWith(ManaState.PACKET_CODEC, AttachmentSyncPredicate.targetOnly()));

    public static final AttachmentType<SpellCooldowns> SPELL_COOLDOWNS = AttachmentRegistry.create(
            ForgottenDimensions.id("spell_cooldowns"), b -> b
                    .initializer(() -> SpellCooldowns.EMPTY)
                    .persistent(SpellCooldowns.CODEC)
                    .copyOnDeath()
                    .syncWith(PacketCodecs.codec(SpellCooldowns.CODEC), AttachmentSyncPredicate.targetOnly()));

    /** Point de retour des portails (persistant, non synchronisé : seul le serveur en a besoin). */
    public static final AttachmentType<com.forgotten.dimensions.progression.ReturnPoint> RETURN_POINT = AttachmentRegistry.create(
            ForgottenDimensions.id("return_point"), b -> b
                    .persistent(com.forgotten.dimensions.progression.ReturnPoint.CODEC)
                    .copyOnDeath());

    private ModAttachments() {}

    public static void register() {
        // Le chargement de la classe enregistre les attachements.
    }
}
