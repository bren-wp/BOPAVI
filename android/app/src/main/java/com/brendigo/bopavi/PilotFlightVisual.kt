package com.brendigo.bopavi

/**
 * Physical hit radius is always GameSimulation.radius = 18.
 * Artwork size is intentionally independent: the owner references show
 * broad-feathered Sova and the full bushy-tailed Lisac considerably larger
 * on screen than the compact, imported Bopi pilot.
 */
internal object PilotFlightVisual {
    fun diameter(skin:Int):Float=when(skin){
        9 -> 184f  // Sova: spread wings and full head occupy ~38% of camera width
        10 -> 182f // Lisac: tail, ears and red scarf retain the reference silhouette
        else -> 142f // Owner-approved Bopi source; do not re-stretch cropped layers
    }
}
