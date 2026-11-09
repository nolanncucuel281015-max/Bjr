package com.forgotten.dimensions.client;

import com.forgotten.dimensions.magic.ManaState;
import com.forgotten.dimensions.magic.SpellCooldowns;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.registry.ModAttachments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Lecture côté client des données synchronisées par le serveur (lecture seule). */
public final class ClientData {
    private ClientData() {}

    public static ClientPlayerEntity player() {
        return MinecraftClient.getInstance().player;
    }

    public static PlayerProgress progress() {
        ClientPlayerEntity p = player();
        PlayerProgress v = p == null ? null : p.getAttached(ModAttachments.PROGRESS);
        return v != null ? v : PlayerProgress.DEFAULT;
    }

    public static ManaState mana() {
        ClientPlayerEntity p = player();
        ManaState v = p == null ? null : p.getAttached(ModAttachments.MANA);
        return v != null ? v : ManaState.DEFAULT;
    }

    public static long cooldownRemaining(String spell) {
        ClientPlayerEntity p = player();
        if (p == null) return 0;
        SpellCooldowns cds = p.getAttached(ModAttachments.SPELL_COOLDOWNS);
        return cds == null ? 0 : cds.remaining(spell, p.getEntityWorld().getTime());
    }
}
