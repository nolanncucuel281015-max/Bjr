package com.forgotten.dimensions.content;

/**
 * Description d'un bloc « table-driven ».
 *
 * @param base     bloc de base (escaliers, dalles, murs), vide sinon
 * @param behavior comportement spécial (blocs techno/magiques), "none" sinon
 */
public record BlockDef(String id, BlockKind kind, float hardness, float resistance, String sound, int luminance,
                       boolean requiresTool, String base, int xpMin, int xpMax, String behavior) {
}
