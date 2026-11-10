package com.forgotten.dimensions.entity.creature;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * Sons des créatures. Phase 5 : sons de Minecraft choisis par type de créature ; la phase 9
 * les remplacera par des sons originaux.
 */
public final class CreatureSounds {
    public record Set(SoundEvent ambient, SoundEvent hurt, SoundEvent death) {}

    private CreatureSounds() {}

    public static Set of(String name) {
        return switch (name) {
            case "phantom" -> new Set(SoundEvents.ENTITY_PHANTOM_AMBIENT, SoundEvents.ENTITY_PHANTOM_HURT, SoundEvents.ENTITY_PHANTOM_DEATH);
            case "golem" -> new Set(SoundEvents.ENTITY_IRON_GOLEM_STEP, SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundEvents.ENTITY_IRON_GOLEM_DEATH);
            case "allay" -> new Set(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundEvents.ENTITY_ALLAY_HURT, SoundEvents.ENTITY_ALLAY_DEATH);
            case "sheep" -> new Set(SoundEvents.ENTITY_SHEEP_AMBIENT, SoundEvents.ENTITY_SHEEP_HURT, SoundEvents.ENTITY_SHEEP_DEATH);
            case "evoker" -> new Set(SoundEvents.ENTITY_EVOKER_AMBIENT, SoundEvents.ENTITY_EVOKER_HURT, SoundEvents.ENTITY_EVOKER_DEATH);
            case "wolf" -> new Set(SoundEvents.ENTITY_RAVAGER_AMBIENT, SoundEvents.ENTITY_RAVAGER_HURT, SoundEvents.ENTITY_RAVAGER_DEATH);
            case "spider" -> new Set(SoundEvents.ENTITY_SPIDER_AMBIENT, SoundEvents.ENTITY_SPIDER_HURT, SoundEvents.ENTITY_SPIDER_DEATH);
            case "plant" -> new Set(SoundEvents.BLOCK_AZALEA_LEAVES_PLACE, SoundEvents.BLOCK_GRASS_BREAK, SoundEvents.BLOCK_AZALEA_LEAVES_BREAK);
            case "silverfish" -> new Set(SoundEvents.ENTITY_SILVERFISH_AMBIENT, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_SILVERFISH_DEATH);
            case "hoglin" -> new Set(SoundEvents.ENTITY_HOGLIN_AMBIENT, SoundEvents.ENTITY_HOGLIN_HURT, SoundEvents.ENTITY_HOGLIN_DEATH);
            case "vex" -> new Set(SoundEvents.ENTITY_VEX_AMBIENT, SoundEvents.ENTITY_VEX_HURT, SoundEvents.ENTITY_VEX_DEATH);
            case "wither_skeleton" -> new Set(SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT, SoundEvents.ENTITY_WITHER_SKELETON_HURT, SoundEvents.ENTITY_WITHER_SKELETON_DEATH);
            case "guardian" -> new Set(SoundEvents.ENTITY_GUARDIAN_AMBIENT, SoundEvents.ENTITY_GUARDIAN_HURT, SoundEvents.ENTITY_GUARDIAN_DEATH);
            case "squid" -> new Set(SoundEvents.ENTITY_GLOW_SQUID_AMBIENT, SoundEvents.ENTITY_GLOW_SQUID_HURT, SoundEvents.ENTITY_GLOW_SQUID_DEATH);
            case "turtle" -> new Set(SoundEvents.ENTITY_TURTLE_AMBIENT_LAND, SoundEvents.ENTITY_TURTLE_HURT, SoundEvents.ENTITY_TURTLE_DEATH);
            case "drowned" -> new Set(SoundEvents.ENTITY_DROWNED_AMBIENT, SoundEvents.ENTITY_DROWNED_HURT, SoundEvents.ENTITY_DROWNED_DEATH);
            case "strider" -> new Set(SoundEvents.ENTITY_STRIDER_AMBIENT, SoundEvents.ENTITY_STRIDER_HURT, SoundEvents.ENTITY_STRIDER_DEATH);
            case "blaze" -> new Set(SoundEvents.ENTITY_BLAZE_AMBIENT, SoundEvents.ENTITY_BLAZE_HURT, SoundEvents.ENTITY_BLAZE_DEATH);
            case "polar_bear" -> new Set(SoundEvents.ENTITY_POLAR_BEAR_AMBIENT, SoundEvents.ENTITY_POLAR_BEAR_HURT, SoundEvents.ENTITY_POLAR_BEAR_DEATH);
            case "stray" -> new Set(SoundEvents.ENTITY_STRAY_AMBIENT, SoundEvents.ENTITY_STRAY_HURT, SoundEvents.ENTITY_STRAY_DEATH);
            case "rabbit" -> new Set(SoundEvents.ENTITY_RABBIT_AMBIENT, SoundEvents.ENTITY_RABBIT_HURT, SoundEvents.ENTITY_RABBIT_DEATH);
            case "warden" -> new Set(SoundEvents.ENTITY_WARDEN_AMBIENT, SoundEvents.ENTITY_WARDEN_HURT, SoundEvents.ENTITY_WARDEN_DEATH);
            case "slime" -> new Set(SoundEvents.ENTITY_SLIME_SQUISH, SoundEvents.ENTITY_SLIME_HURT, SoundEvents.ENTITY_SLIME_DEATH);
            case "enderman" -> new Set(SoundEvents.ENTITY_ENDERMAN_AMBIENT, SoundEvents.ENTITY_ENDERMAN_HURT, SoundEvents.ENTITY_ENDERMAN_DEATH);
            case "bat" -> new Set(SoundEvents.ENTITY_BAT_AMBIENT, SoundEvents.ENTITY_BAT_HURT, SoundEvents.ENTITY_BAT_DEATH);
            case "amethyst" -> new Set(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK);
            default -> new Set(SoundEvents.ENTITY_ZOMBIE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_HURT, SoundEvents.ENTITY_ZOMBIE_DEATH);
        };
    }
}
