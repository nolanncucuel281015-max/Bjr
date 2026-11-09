package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.TierDef;
import com.forgotten.dimensions.content.WeaponDef;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Arbalète dont les carreaux sont renforcés selon le palier. */
public class FdCrossbowItem extends CrossbowItem {
    private final WeaponDef def;
    private final TierDef tier;

    public FdCrossbowItem(WeaponDef def, TierDef tier, Settings settings) {
        super(settings);
        this.def = def;
        this.tier = tier;
    }

    public WeaponDef definition() {
        return def;
    }

    public double boltDamage() {
        return 2.5 + tier.damageBonus() * 0.4 + (def.isLegendary() ? 1.5 : 0.0);
    }

    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        ProjectileEntity projectile = super.createArrowEntity(world, shooter, weaponStack, projectileStack, critical);
        if (projectile instanceof PersistentProjectileEntity arrow) {
            arrow.setDamage(boltDamage());
            if (def.legendary().equals("stormcaller_crossbow")) arrow.addCommandTag(LegendaryEffects.STORM_ARROW);
        }
        return projectile;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return def.isLegendary() || super.hasGlint(stack);
    }
}
