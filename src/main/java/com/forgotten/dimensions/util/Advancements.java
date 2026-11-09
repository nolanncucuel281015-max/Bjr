package com.forgotten.dimensions.util;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/** Accorde les succès « déclenchés par le code » (critère minecraft:impossible nommé "granted"). */
public final class Advancements {
    private Advancements() {}

    public static void grant(ServerPlayerEntity player, String path) {
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;
        AdvancementEntry entry = server.getAdvancementLoader().get(ForgottenDimensions.id(path));
        if (entry == null) {
            ForgottenDimensions.LOGGER.warn("Succès introuvable : {}", path);
            return;
        }
        player.getAdvancementTracker().grantCriterion(entry, "granted");
    }
}
