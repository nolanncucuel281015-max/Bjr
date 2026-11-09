package com.forgotten.dimensions.event;

import com.forgotten.dimensions.item.ArmorSetBonuses;
import com.forgotten.dimensions.item.ArtifactEffects;
import com.forgotten.dimensions.magic.ManaManager;
import com.forgotten.dimensions.magic.SummonManager;
import com.forgotten.dimensions.magic.ZoneManager;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.Skills;
import com.forgotten.dimensions.progression.XpSource;
import com.forgotten.dimensions.util.PlayerCache;
import com.forgotten.dimensions.util.StatusEffectsHelper;
import com.forgotten.dimensions.util.config.FdConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Travail périodique par joueur, étalé dans le temps pour éviter les pics :
 * mana toutes les 10 ticks, artefacts/armures toutes les 20 ticks, exploration toutes les 40 ticks.
 */
public final class ServerTickHandler {
    private ServerTickHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(ZoneManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(ServerTickHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        int tick = server.getTicks();
        if (tick % 20 == 0) SummonManager.tick(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!player.isAlive()) continue;
            int phase = (tick + player.getId()) ;
            if (phase % 10 == 0) ManaManager.tick(player);
            if (phase % 20 == 0) perSecond(player, tick);
            if (phase % 40 == 0) checkBiome(player);
        }
    }

    private static void perSecond(ServerPlayerEntity player, long tick) {
        PlayerCache.Snapshot snap = PlayerCache.refresh(player);
        ArtifactEffects.applyPassives(player, snap, tick);
        ArmorSetBonuses.apply(player, snap.fullSet());
        ManaManager.updateMax(player);

        PlayerProgress p = ProgressionManager.get(player);
        if (Skills.rank(p, Skills.DARK_SIGHT) > 0 && player.getY() < 40 && !player.getEntityWorld().isSkyVisible(player.getBlockPos())) {
            StatusEffectsHelper.refresh(player, StatusEffects.NIGHT_VISION, 0);
        }
        int berserk = Skills.rank(p, Skills.BERSERK);
        if (berserk > 0 && player.getHealth() < player.getMaxHealth() * 0.3F) {
            StatusEffectsHelper.refresh(player, StatusEffects.STRENGTH, berserk - 1);
        }
        var held = player.getMainHandStack().getItem();
        if (held instanceof com.forgotten.dimensions.item.MeleeWeaponItem w && w.definition().isLegendary()) {
            com.forgotten.dimensions.item.LegendaryEffects.whileHeld(w.definition().legendary(), player);
        }
    }

    private static void checkBiome(ServerPlayerEntity player) {
        var entry = player.getEntityWorld().getBiome(player.getBlockPos());
        String biome = entry.getKey().map(k -> k.getValue().toString()).orElse(null);
        if (biome == null) return;
        PlayerProgress p = ProgressionManager.get(player);
        if (p.visitedBiomes().contains(biome)) return;
        ProgressionManager.set(player, p.withVisitedBiome(biome));
        player.sendMessage(Text.translatable("message.forgotten_dimensions.biome_discovered",
                Text.translatable("biome." + biome.replace(':', '.'))).formatted(Formatting.GREEN), true);
        ProgressionManager.addXp(player, FdConfig.get().biomeDiscoveryXp, XpSource.EXPLORATION);
    }
}
