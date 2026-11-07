package com.brendigo.bopavi

/**
 * Physical hit radius is always GameSimulation.radius = 18.
 * Artwork size is intentionally independent: the owner references show
 * broad-feathered Sova, the bushy-tailed Lisac and the flying Bopi all
 * need the prominent silhouettes shown in the user's 864x1536 references.
 * These are visual sprite bounds, NOT collision bounds. The actual alpha
 * silhouette depends on the generated sprite; compare on-device captures
 * before claiming a pixel-perfect match.
 */
internal object PilotFlightVisual {
    fun diameter(skin:Int):Float=when(skin){
        9 -> 202f  // Sova: broad wings, goggles and full rounded flight pose
        10 -> 196f // Lisac: preserve the whole tail, ears and long red scarf
        else -> 180f // Bopi and cosmetic variants: full wings and scarf visible
    }
}
