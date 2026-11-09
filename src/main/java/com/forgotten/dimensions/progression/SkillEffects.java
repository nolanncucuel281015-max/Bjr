package com.forgotten.dimensions.progression;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/** Applique les bonus d'attributs des compétences et de la spécialisation (modificateurs persistants, idempotents). */
public final class SkillEffects {
    private SkillEffects() {}

    public static void apply(PlayerEntity player, PlayerProgress progress) {
        for (Skill skill : Skills.all().values()) {
            if (skill.attribute() == null) continue;
            Identifier id = ForgottenDimensions.id("skill/" + skill.id());
            int rank = progress.skillRank(skill.id());
            setModifier(player, skill.attribute(), id, rank * skill.perRank(), skill.operation(), rank > 0);
        }
        SkillTree spec = SkillTree.byId(progress.specialization());
        setModifier(player, EntityAttributes.ATTACK_DAMAGE, ForgottenDimensions.id("spec/warrior"), 0.10,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, spec == SkillTree.WARRIOR);
        setModifier(player, EntityAttributes.MOVEMENT_SPEED, ForgottenDimensions.id("spec/archer"), 0.05,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, spec == SkillTree.ARCHER);
        setModifier(player, EntityAttributes.MOVEMENT_SPEED, ForgottenDimensions.id("spec/explorer"), 0.10,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, spec == SkillTree.EXPLORER);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    static void setModifier(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, Identifier id, double value,
                            EntityAttributeModifier.Operation op, boolean active) {
        EntityAttributeInstance inst = player.getAttributeInstance(attribute);
        if (inst == null) return;
        if (active) {
            inst.overwritePersistentModifier(new EntityAttributeModifier(id, value, op));
        } else if (inst.hasModifier(id)) {
            inst.removeModifier(id);
        }
    }
}
