package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.WeaponCategory;
import com.forgotten.dimensions.content.WeaponDef;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.SpellExecutor;
import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.SpellTargeting;
import com.forgotten.dimensions.magic.Spells;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Bâtons magiques et baguettes : clic droit = projectile de l'école du palier, contre du mana. */
public class MagicWeaponItem extends Item {
    private final WeaponDef def;
    private final SpellSchool school;

    public MagicWeaponItem(WeaponDef def, SpellSchool school, Settings settings) {
        super(settings);
        this.def = def;
        this.school = def.legendary().equals("frost_queen_scepter") ? SpellSchool.ICE : school;
    }

    public WeaponDef definition() {
        return def;
    }

    public SpellSchool school() {
        return school;
    }

    /** Puissance du projectile : bâton plus fort et plus lent, baguette rapide et économe. */
    public float boltPower() {
        float rarity = 1.0F + 0.25F * def.rarity().ordinal();
        return def.category() == WeaponCategory.STAFF ? 6.0F * rarity : 3.5F * rarity;
    }

    public float manaCost() {
        return def.category() == WeaponCategory.STAFF ? 10F : 4F;
    }

    public int cooldown() {
        return def.category() == WeaponCategory.STAFF ? 16 : 8;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack)) return ActionResult.FAIL;
        if (user instanceof ServerPlayerEntity player) {
            boolean ok;
            if (def.legendary().equals("frost_queen_scepter")) {
                ok = SpellCaster.castWeaponBolt(player, school, boltPower(), manaCost() * 1.5F);
                LivingEntity target = SpellTargeting.lookTarget(player, 24);
                if (ok && target != null) {
                    SpellExecutor.hit(player, Spells.get("frost_lance"), target, boltPower());
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 9));
                }
            } else {
                ok = SpellCaster.castWeaponBolt(player, school, boltPower(), manaCost());
            }
            if (!ok) return ActionResult.FAIL;
            user.getItemCooldownManager().set(stack, cooldown());
            stack.damage(1, user, hand == Hand.MAIN_HAND ? net.minecraft.entity.EquipmentSlot.MAINHAND : net.minecraft.entity.EquipmentSlot.OFFHAND);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return def.isLegendary() || super.hasGlint(stack);
    }
}
