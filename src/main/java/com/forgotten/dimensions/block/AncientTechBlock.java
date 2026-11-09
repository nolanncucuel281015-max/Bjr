package com.forgotten.dimensions.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Blocs technologiques des Anciens Architectes.
 * <ul>
 *   <li>redstone_power : source de redstone permanente (niveau 15) ;</li>
 *   <li>lore_terminal : affiche un fragment d'histoire au clic droit ;</li>
 *   <li>glow_low / glow_high : étincelles d'énergie (la lumière vient des propriétés du bloc).</li>
 * </ul>
 */
public class AncientTechBlock extends Block {
    public static final int LORE_ENTRIES = 7;
    private final String behavior;

    public AncientTechBlock(String behavior, Settings settings) {
        super(settings);
        this.behavior = behavior;
    }

    public String behavior() {
        return behavior;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return behavior.equals("redstone_power");
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return behavior.equals("redstone_power") ? 15 : 0;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!behavior.equals("lore_terminal")) return ActionResult.PASS;
        if (!world.isClient()) {
            // Fragment choisi de façon déterministe selon la position : chaque terminal raconte toujours la même chose.
            int index = Math.floorMod(pos.getX() * 31 + pos.getZ() * 17 + pos.getY(), LORE_ENTRIES) + 1;
            player.sendMessage(Text.translatable("lore.forgotten_dimensions.terminal." + index).formatted(Formatting.AQUA), false);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if ((behavior.startsWith("glow") || behavior.equals("redstone_power")) && random.nextInt(4) == 0) {
            world.addParticleClient(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + 1.05,
                    pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }
}
