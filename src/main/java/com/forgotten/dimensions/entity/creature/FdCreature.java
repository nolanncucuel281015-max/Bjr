package com.forgotten.dimensions.entity.creature;

import com.forgotten.dimensions.content.CreatureDef;

/** Interface commune aux créatures du mod (hostiles et pacifiques). */
public interface FdCreature {
    CreatureDef definition();

    CreatureState creatureState();
}
