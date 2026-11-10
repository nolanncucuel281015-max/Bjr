package com.forgotten.dimensions.entity;

import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellExecutor;
import com.forgotten.dimensions.magic.SpellFx;
import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.SpellTargeting;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.registry.ModEntities;
import com.forgotten.dimensions.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Projectile magique sans gravité, utilisé par les sorts de type BOLT et par les bâtons/baguettes. */
public class MagicBoltEntity extends ThrownItemEntity {
    private static final int MAX_AGE = 100;
    private static final net.minecraft.entity.data.TrackedData<Integer> SCHOOL = net.minecraft.entity.data.DataTracker.registerData(
            MagicBoltEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER);
    private static final net.minecraft.entity.data.TrackedData<Float> SIZE = net.minecraft.entity.data.DataTracker.registerData(
            MagicBoltEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.FLOAT);
    private String spellId = "fire_bolt";
    private float power = 5.0F;
    private float splash = 0.0F;

    public MagicBoltEntity(EntityType<? extends MagicBoltEntity> type, World world) {
        super(type, world);
    }

    public MagicBoltEntity(World world, LivingEntity owner, String spellId, float power, float splash) {
        super(ModEntities.MAGIC_BOLT, owner, world, new ItemStack(boltItem(Spells.get(spellId).school())));
        this.spellId = spellId;
        this.power = power;
        this.splash = splash;
        this.dataTracker.set(SCHOOL, Spells.get(spellId).school().ordinal());
        this.dataTracker.set(SIZE, switch (spellId) {
            case "fireball" -> 1.7F;
            case "ball_lightning" -> 2.3F;
            default -> splash > 0 ? 1.4F : 1.0F;
        });
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SCHOOL, 0).add(SIZE, 1.0F);
    }

    /** École synchronisée (rendu 3D côté client). */
    public SpellSchool school() {
        SpellSchool[] all = SpellSchool.values();
        return all[Math.max(0, Math.min(all.length - 1, dataTracker.get(SCHOOL)))];
    }

    public float size() {
        return dataTracker.get(SIZE);
    }

    public static Item boltItem(SpellSchool school) {
        return ModItems.get(school.id() + "_spell_bolt");
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.get("fire_spell_bolt");
    }

    @Override
    protected double getGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        World world = getEntityWorld();
        if (world.isClient()) {
            world.addParticleClient(SpellExecutor.particle(school()), getX(), getY(), getZ(), 0, 0, 0);
        } else if (age > MAX_AGE) {
            discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (!(getEntityWorld() instanceof ServerWorld) || !(hit.getEntity() instanceof LivingEntity target)) return;
        Spell spell = Spells.get(spellId);
        if (spell == null) return;
        if (getOwner() instanceof ServerPlayerEntity caster) {
            if (SpellTargeting.isEnemy(caster, target)) SpellExecutor.hit(caster, spell, target, power);
        } else {
            ServerWorld world = (ServerWorld) getEntityWorld();
            target.damage(world, world.getDamageSources().indirectMagic(this, getOwner()), power);
            SpellExecutor.schoolEffect(spell.school(), target, power);
        }
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && entity != getOwner();
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getEntityWorld() instanceof ServerWorld world) {
            Spell spell = Spells.get(spellId);
            if (spell != null) {
                com.forgotten.dimensions.magic.SpellVisuals.spawn(world, com.forgotten.dimensions.magic.SpellVisual.EXPLOSION,
                        spell.school(), hit.getPos(), splash > 0 ? splash : 0.9F * size(), splash > 0 ? 16 : 10);
                if (splash > 0 && getOwner() instanceof ServerPlayerEntity caster) {
                    SpellExecutor.areaDamage(caster, spell, hit.getPos(), splash, power * 0.6F);
                } else {
                    SpellFx.particles(world, SpellExecutor.particle(spell.school()), getX(), getY(), getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                }
            }
            discard();
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putString("Spell", spellId);
        view.putFloat("Power", power);
        view.putFloat("Splash", splash);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        spellId = view.getString("Spell", "fire_bolt");
        if (Spells.get(spellId) == null) spellId = "fire_bolt";
        power = view.getFloat("Power", 5.0F);
        splash = view.getFloat("Splash", 0.0F);
    }

    public String spellId() {
        return spellId;
    }
}
