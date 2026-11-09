package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.TierDef;
import com.forgotten.dimensions.content.WeaponDef;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Arc dont les flèches infligent davantage de dégâts selon le palier. */
public class FdBowItem extends BowItem {
    private final WeaponDef def;
    private final TierDef tier;

    public FdBowItem(WeaponDef def, TierDef tier, Settings settings) {
        super(settings);
        this.def = def;
        this.tier = tier;
    }

    public WeaponDef definition() {
        return def;
    }

    /** Dégâts de base de la flèche (vanilla : 2.0). */
    public double arrowDamage() {
        return 2.0 + tier.damageBonus() * 0.35 + (def.isLegendary() ? 1.0 : 0.0);
    }

    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        ProjectileEntity projectile = super.createArrowEntity(world, shooter, weaponStack, projectileStack, critical);
        if (projectile instanceof PersistentProjectileEntity arrow) {
            arrow.setDamage(arrowDamage());
            if (def.legendary().equals("void_bow")) {
                arrow.setNoGravity(true);
                arrow.addCommandTag(LegendaryEffects.VOID_ARROW);
            }
        }
        return projectile;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return def.isLegendary() || super.hasGlint(stack);
    }
}
