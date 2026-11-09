package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.ArtifactDef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Artefact : effet passif tant qu'il est dans l'inventaire, effet actif éventuel au clic droit. */
public class ArtifactItem extends Item {
    private final ArtifactDef def;

    public ArtifactItem(ArtifactDef def, Settings settings) {
        super(settings);
        this.def = def;
    }

    public ArtifactDef definition() {
        return def;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!ArtifactEffects.ACTIVE.contains(def.id())) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack)) return ActionResult.FAIL;
        if (user instanceof ServerPlayerEntity player) {
            int cooldown = ArtifactEffects.use(player, def.id());
            if (cooldown <= 0) return ActionResult.FAIL;
            user.getItemCooldownManager().set(stack, cooldown);
        }
        return ActionResult.SUCCESS;
    }
}
