package com.forgotten.dimensions.block;

import com.forgotten.dimensions.dimension.DimensionTravel;
import com.forgotten.dimensions.dimension.FdDimensions;
import com.forgotten.dimensions.util.Advancements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Portail dimensionnel des Anciens Architectes. Clic droit avec une clé : voyage vers la dimension
 * de la clé (la clé n'est pas consommée). Clic droit à main vide dans une dimension du mod : retour.
 */
public class DimensionalGateBlock extends Block {
    public DimensionalGateBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
                                         Hand hand, BlockHitResult hit) {
        String dim = FdDimensions.dimensionOfKey(stack.getItem());
        if (dim == null) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player instanceof ServerPlayerEntity sp) {
            if (sp.getItemCooldownManager().isCoolingDown(stack)) return ActionResult.FAIL;
            if (dim.equals(DimensionTravel.dimensionId(world))) {
                sp.sendMessage(Text.translatable("message.forgotten_dimensions.gate.already_here").formatted(Formatting.GRAY), true);
                return ActionResult.FAIL;
            }
            sp.getItemCooldownManager().set(stack, 60);
            if (!DimensionTravel.travelTo(sp, dim)) {
                sp.sendMessage(Text.translatable("command.forgotten_dimensions.tp.missing", dim).formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
            Advancements.grant(sp, "first_journey");
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity sp) {
            if (DimensionTravel.isModDimension(world)) {
                DimensionTravel.returnHome(sp);
            } else {
                sp.sendMessage(Text.translatable("message.forgotten_dimensions.gate.need_key").formatted(Formatting.LIGHT_PURPLE), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (int i = 0; i < 2; i++) {
            world.addParticleClient(ParticleTypes.PORTAL, pos.getX() + random.nextDouble(), pos.getY() + 1.0 + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), (random.nextDouble() - 0.5) * 0.5, -0.2, (random.nextDouble() - 0.5) * 0.5);
        }
    }
}
