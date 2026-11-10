package com.forgotten.dimensions.dimension;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.progression.ReturnPoint;
import com.forgotten.dimensions.registry.ModAttachments;
import com.forgotten.dimensions.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Set;

/** Voyage entre la Surface et les sept dimensions (portail, commande, sauvetage en cas de chute). */
public final class DimensionTravel {
    private DimensionTravel() {}

    public static boolean isModDimension(World world) {
        return world.getRegistryKey().getValue().getNamespace().equals(ForgottenDimensions.MOD_ID);
    }

    /** Identifiant court (« celestial »...) de la dimension du mod, ou null. */
    public static String dimensionId(World world) {
        return isModDimension(world) ? world.getRegistryKey().getValue().getPath() : null;
    }

    /** @return false si la dimension n'existe pas sur ce serveur */
    public static boolean travelTo(ServerPlayerEntity player, String dimId) {
        MinecraftServer server = player.getEntityWorld().getServer();
        ServerWorld target = server.getWorld(FdDimensions.key(dimId));
        if (target == null) return false;
        if (!isModDimension(player.getEntityWorld())) {
            player.setAttached(ModAttachments.RETURN_POINT,
                    new ReturnPoint(player.getEntityWorld().getRegistryKey().getValue().toString(), player.getBlockPos()));
        }
        int x = player.getBlockX(), z = player.getBlockZ();
        if (dimId.equals("void")) {
            // Le Néant a la structure de l'End : on arrive sur l'île centrale, pas dans le vide qui l'entoure.
            x = Math.max(-64, Math.min(64, x));
            z = Math.max(-64, Math.min(64, z));
        }
        BlockPos arrival = findArrival(target, x, z, dimId);
        departFx(player);
        player.teleport(target, arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5, Set.of(), player.getYaw(), player.getPitch(), true);
        ensureReturnGate(target, arrival);
        arrive(player, dimId);
        return true;
    }

    /** Retour au point mémorisé (ou au point d'apparition si aucun). */
    public static void returnHome(ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        ReturnPoint rp = player.getAttached(ModAttachments.RETURN_POINT);
        departFx(player);
        ServerWorld world = rp == null ? null : server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(rp.dimension())));
        if (world == null || isModDimension(world)) {
            player.teleportTo(player.getRespawnTarget(false, TeleportTarget.NO_OP));
        } else {
            BlockPos p = rp.pos();
            player.teleport(world, p.getX() + 0.5, p.getY(), p.getZ() + 0.5, Set.of(), player.getYaw(), player.getPitch(), true);
        }
        player.fallDistance = 0;
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_PORTAL_TRAVEL,
                SoundCategory.PLAYERS, 0.4F, 1.4F);
    }

    /** Point d'arrivée sûr : sol solide, deux blocs libres, pas de fluide. Plateforme de secours sinon. */
    public static BlockPos findArrival(ServerWorld world, int x, int z, String dimId) {
        int[][] offsets = {{0, 0}, {16, 0}, {-16, 0}, {0, 16}, {0, -16}, {32, 32}, {-32, 32}, {32, -32}, {-32, -32},
                {48, 0}, {-48, 0}, {0, 48}, {0, -48}};
        for (int[] o : offsets) {
            BlockPos found = scanColumn(world, x + o[0], z + o[1]);
            if (found != null) return found;
        }
        return buildPlatform(world, x, z, dimId);
    }

    /**
     * Mondes à ciel ouvert : on ne retient que la première surface rencontrée en descendant (jamais une
     * grotte sous l'eau ou sous le sol). Mondes à plafond : première poche d'air avec un sol, sous le toit.
     */
    private static BlockPos scanColumn(ServerWorld world, int x, int z) {
        world.getChunk(x >> 4, z >> 4);
        boolean ceiling = world.getDimension().hasCeiling();
        int top = world.getBottomY() + world.getLogicalHeight() - 6;
        BlockPos.Mutable p = new BlockPos.Mutable(x, top, z);
        for (int y = top; y > world.getBottomY() + 2; y--) {
            p.setY(y);
            if (isFree(world, p)) continue;
            boolean ok = isFloor(world, p) && isFree(world, p.up()) && isFree(world, p.up(2));
            if (ok) return p.up().toImmutable();
            if (!ceiling) return null;
        }
        return null;
    }

    private static boolean isFree(ServerWorld world, BlockPos pos) {
        BlockState s = world.getBlockState(pos);
        return s.getCollisionShape(world, pos).isEmpty() && s.getFluidState().isEmpty();
    }

    private static boolean isFloor(ServerWorld world, BlockPos pos) {
        BlockState s = world.getBlockState(pos);
        return s.isSideSolidFullSquare(world, pos, net.minecraft.util.math.Direction.UP) && s.getFluidState().isEmpty()
                && !s.isOf(Blocks.MAGMA_BLOCK) && !s.isOf(Blocks.BEDROCK);
    }

    private static BlockPos buildPlatform(ServerWorld world, int x, int z, String dimId) {
        int y;
        if (!world.getDimension().hasCeiling()) {
            // Ciel ouvert : plateforme posée sur la surface (eau comprise) ; colonne vide → altitude fixe.
            int surface = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z);
            y = surface > world.getBottomY() + 1 ? surface - 1 : (dimId.equals("celestial") ? 110 : 70);
        } else {
            y = world.getBottomY() + Math.min(64, world.getLogicalHeight() / 2);
        }
        Block floor = platformBlock(dimId);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(new BlockPos(x + dx, y, z + dz), floor.getDefaultState());
                for (int dy = 1; dy <= 3; dy++) world.setBlockState(new BlockPos(x + dx, y + dy, z + dz), Blocks.AIR.getDefaultState());
            }
        }
        return new BlockPos(x, y + 1, z);
    }

    private static Block platformBlock(String dimId) {
        String stone = switch (dimId) {
            case "celestial" -> "cloudstone";
            case "corrupted" -> "blightstone";
            case "ocean" -> "abyssal_stone";
            case "chaos" -> "cinderstone";
            case "eternal" -> "frostrock";
            case "depths" -> "umbral_rock";
            default -> "voidstone";
        };
        Block b = ModBlocks.get("polished_" + stone);
        return b != null ? b : Blocks.STONE;
    }

    /** Place un portail de retour à côté du point d'arrivée s'il n'y en a pas déjà un à proximité. */
    public static void ensureReturnGate(ServerWorld world, BlockPos arrival) {
        Block gate = ModBlocks.get("dimensional_gate");
        for (BlockPos p : BlockPos.iterate(arrival.add(-6, -3, -6), arrival.add(6, 3, 6))) {
            if (world.getBlockState(p).isOf(gate)) return;
        }
        BlockPos g = arrival.east(2);
        if (!isFloor(world, g.down())) world.setBlockState(g.down(), Blocks.STONE_BRICKS.getDefaultState());
        world.setBlockState(g, gate.getDefaultState());
        world.setBlockState(g.up(), Blocks.AIR.getDefaultState());
    }

    private static void departFx(ServerPlayerEntity player) {
        ServerWorld w = player.getEntityWorld();
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
    }

    private static void arrive(ServerPlayerEntity player, String dimId) {
        ServerWorld w = player.getEntityWorld();
        w.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 60, 0.5, 1.0, 0.5, 0.4);
        w.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.5F, 1.2F);
        player.fallDistance = 0;
        player.networkHandler.sendPacket(new TitleS2CPacket(
                Text.translatable("dimension.forgotten_dimensions." + dimId).formatted(Formatting.GOLD)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable(dimId.equals("void")
                ? "chapter.forgotten_dimensions.7" : "chapter.forgotten_dimensions.3").formatted(Formatting.GRAY)));
        if (dimId.equals("ocean")) player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 1200, 0));
        if (dimId.equals("celestial") || dimId.equals("void")) player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0));
    }

    /** Appelé à chaque tick : sauve le joueur tombé sous le monde dans le Royaume Céleste ou le Néant. */
    public static void rescueIfFalling(ServerPlayerEntity player) {
        World w = player.getEntityWorld();
        if (!isModDimension(w) || player.isSpectator() || player.getY() > w.getBottomY() - 4) return;
        player.sendMessage(Text.translatable("message.forgotten_dimensions.fall_rescue").formatted(Formatting.AQUA), false);
        returnHome(player);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0));
    }
}
