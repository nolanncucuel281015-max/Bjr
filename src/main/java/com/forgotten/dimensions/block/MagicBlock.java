package com.forgotten.dimensions.block;

import com.forgotten.dimensions.magic.ManaManager;
import com.forgotten.dimensions.util.StatusEffectsHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Blocs magiques. Comportements :
 * mana_restore (clic droit : +30 mana, une fois toutes les 30 s par joueur via recharge d'objet),
 * rune_* (marcher dessus donne un effet lié à l'école), heal (régénération en marchant dessus),
 * ward (repousse les monstres proches quand on marche dessus), mana_regen_zone (lu par ManaManager),
 * altar (affiche la maîtrise de magie), particles (décoratif).
 */
public class MagicBlock extends Block {
    private final String behavior;

    public MagicBlock(String behavior, Settings settings) {
        super(settings);
        this.behavior = behavior;
    }

    public String behavior() {
        return behavior;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);
        if (world.isClient() || !(entity instanceof PlayerEntity player) || world.getTime() % 20 != 0) return;
        switch (behavior) {
            case "rune_fire" -> StatusEffectsHelper.apply(player, StatusEffects.FIRE_RESISTANCE, 200, 0);
            case "rune_ice" -> player.setFrozenTicks(0);
            case "rune_lightning" -> StatusEffectsHelper.apply(player, StatusEffects.SPEED, 100, 1);
            case "rune_nature" -> StatusEffectsHelper.apply(player, StatusEffects.SATURATION, 1, 0);
            case "rune_void" -> StatusEffectsHelper.apply(player, StatusEffects.SLOW_FALLING, 100, 0);
            case "heal" -> StatusEffectsHelper.apply(player, StatusEffects.REGENERATION, 60, 0);
            case "ward" -> {
                for (HostileEntity mob : world.getEntitiesByClass(HostileEntity.class, new Box(pos).expand(6), LivingEntity::isAlive)) {
                    double dx = mob.getX() - (pos.getX() + 0.5), dz = mob.getZ() - (pos.getZ() + 0.5);
                    double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                    mob.addVelocity(dx / len, 0.3, dz / len);
                    mob.velocityDirty = true;
                }
            }
            default -> { }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (behavior.equals("mana_restore")) {
            if (player instanceof ServerPlayerEntity sp) {
                if (sp.getItemCooldownManager().isCoolingDown(asItem().getDefaultStack())) {
                    sp.sendMessage(Text.translatable("message.forgotten_dimensions.mana_crystal.cooldown").formatted(Formatting.GRAY), true);
                    return ActionResult.FAIL;
                }
                ManaManager.restore(sp, 30F);
                sp.getItemCooldownManager().set(asItem().getDefaultStack(), 600);
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1.0F, 1.4F);
            }
            return ActionResult.SUCCESS;
        }
        if (behavior.equals("altar")) {
            if (player instanceof ServerPlayerEntity sp) {
                var p = com.forgotten.dimensions.progression.ProgressionManager.get(sp);
                for (var school : com.forgotten.dimensions.magic.SpellSchool.values()) {
                    sp.sendMessage(Text.translatable("message.forgotten_dimensions.altar.mastery",
                            Text.translatable(school.translationKey()), p.schoolMastery(school.id()),
                            p.schoolCasts().getOrDefault(school.id(), 0)).formatted(school.formatting), false);
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) != 0) return;
        ParticleEffect p = switch (behavior) {
            case "rune_fire" -> ParticleTypes.FLAME;
            case "rune_ice" -> ParticleTypes.SNOWFLAKE;
            case "rune_lightning" -> ParticleTypes.ELECTRIC_SPARK;
            case "rune_nature", "heal" -> ParticleTypes.HAPPY_VILLAGER;
            case "rune_void" -> ParticleTypes.PORTAL;
            case "mana_restore", "mana_regen_zone" -> ParticleTypes.GLOW;
            default -> ParticleTypes.ENCHANT;
        };
        world.addParticleClient(p, pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0, 0.03, 0);
    }
}
