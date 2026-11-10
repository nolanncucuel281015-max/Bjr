package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.WeaponCategory;
import com.forgotten.dimensions.content.WeaponDef;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.progression.ProgressionManager;
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

/** Bâtons magiques et baguettes : clic droit = lance le sort actif (G pour changer), renforcé par l'arme. */
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

    /** Puissance de base (affichée dans l'infobulle) : sert de référence aux bonus de l'arme. */
    public float boltPower() {
        float rarity = 1.0F + 0.25F * def.rarity().ordinal();
        return def.category() == WeaponCategory.STAFF ? 6.0F * rarity : 3.5F * rarity;
    }

    public boolean isStaff() {
        return def.category() == WeaponCategory.STAFF;
    }

    /** Bonus de puissance de l'arme sur tous les sorts (bâton : fort ; baguette : modéré). */
    public float powerBonus() {
        int r = def.rarity().ordinal();
        return isStaff() ? 1.15F + 0.08F * r : 0.95F + 0.05F * r;
    }

    /** Bonus supplémentaire quand le sort appartient à l'école de l'arme. */
    public float affinityBonus() {
        return def.legendary().equals("frost_queen_scepter") ? 1.5F : 1.2F;
    }

    public float manaMultiplier() {
        return isStaff() ? 1.0F : 0.75F;
    }

    public float cooldownMultiplier() {
        return isStaff() ? 1.0F : 0.8F;
    }

    public SpellCaster.CastModifiers modifiersFor(Spell spell) {
        float power = powerBonus() * (spell.school() == school ? affinityBonus() : 1.0F);
        return new SpellCaster.CastModifiers(power, manaMultiplier(), cooldownMultiplier());
    }

    /** Clic droit : lance le sort actif (choisi avec G ou dans le livre de magie), renforcé par l'arme. */
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack)) return ActionResult.FAIL;
        if (user instanceof ServerPlayerEntity player) {
            Spell spell = Spells.get(ProgressionManager.get(player).selectedSpell());
            if (spell == null) spell = Spells.basicBolt(school);
            SpellCaster.Result result = SpellCaster.tryCast(player, spell.id(), modifiersFor(spell));
            if (result != SpellCaster.Result.OK) return ActionResult.FAIL;
            if (def.legendary().equals("frost_queen_scepter")) {
                LivingEntity target = SpellTargeting.lookTarget(player, 24);
                if (target != null) target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4));
            }
            user.getItemCooldownManager().set(stack, isStaff() ? 8 : 5);
            stack.damage(1, user, hand == Hand.MAIN_HAND ? net.minecraft.entity.EquipmentSlot.MAINHAND : net.minecraft.entity.EquipmentSlot.OFFHAND);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return def.isLegendary() || super.hasGlint(stack);
    }
}
