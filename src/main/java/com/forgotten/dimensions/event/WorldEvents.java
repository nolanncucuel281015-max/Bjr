package com.forgotten.dimensions.event;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.magic.ManaManager;
import com.forgotten.dimensions.magic.SummonManager;
import com.forgotten.dimensions.magic.ZoneManager;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.SkillEffects;
import com.forgotten.dimensions.progression.XpSource;
import com.forgotten.dimensions.util.PlayerCache;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;

/** Connexion des joueurs, minage, nettoyage des invocations, cycle de vie du serveur. */
public final class WorldEvents {
    public static final TagKey<Block> FD_ORES = TagKey.of(RegistryKeys.BLOCK, ForgottenDimensions.id("ores"));

    private WorldEvents() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onJoin(handler.getPlayer());
            unlockModRecipes(handler.getPlayer());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerCache.remove(handler.getPlayer().getUuid()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> onJoin(newPlayer));

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity sp)) return;
            if (state.isIn(FD_ORES)) ProgressionManager.addXp(sp, 8, XpSource.MINING);
            else if (state.isIn(ConventionalBlockTags.ORES)) ProgressionManager.addXp(sp, 3, XpSource.MINING);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (SummonManager.isSummon(entity) && !SummonManager.isTracked(entity)) entity.discard();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ZoneManager.clear();
            SummonManager.clear();
            PlayerCache.clear();
        });
    }

    /**
     * Débloque toutes les recettes du mod dans le livre de recettes (établi, four...) : elles y apparaissent
     * dès la connexion, avec les ingrédients surlignés quand on les possède.
     */
    public static int unlockModRecipes(ServerPlayerEntity player) {
        var recipes = player.getEntityWorld().getServer().getRecipeManager().values().stream()
                .filter(r -> r.id().getValue().getNamespace().equals(com.forgotten.dimensions.ForgottenDimensions.MOD_ID))
                .toList();
        return player.unlockRecipes(recipes);
    }

    private static void onJoin(ServerPlayerEntity player) {
        PlayerCache.refresh(player);
        SkillEffects.apply(player, ProgressionManager.get(player));
        ManaManager.updateMax(player);
    }
}
