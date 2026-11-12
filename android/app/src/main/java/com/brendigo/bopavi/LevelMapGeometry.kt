package com.brendigo.bopavi

/**
 * Pixel-coordinate layout measured against the owner's 941x1672 map.
 * Small windows letterbox this composition without distorting the artwork.
 * Actual buttons retain at least a 48dp native touch target.
 */
internal object LevelMapGeometry {
    const val width=941f
    const val height=1672f
    const val starsPerWorld=60

    // Avoid crossing and obscuring actual 48dp native hit targets on small
    // screens. Keep the original reference camera only at safe scale.
    fun supports(pxWidth:Int,pxHeight:Int,portrait:Boolean,fontScale:Float):Boolean =
        portrait && pxWidth>=860 && pxHeight>=1480 && fontScale<1.25f

    /**
     * Node hit targets are 108x107 source pixels; ensure the actual
     * letterboxed node stays >=48dp in both directions before exposing the
     * fixed-position map. CompactMap has fully native large touch targets.
     */
    fun supportsNodeTouch(pxWidth:Int,pxHeight:Int,density:Float):Boolean {
        if(pxWidth<=0 || pxHeight<=0 || !density.isFinite() || density<=0f)
            return false
        val scale=minOf(pxWidth/width,pxHeight/height)
        return nodeWidth*scale>=48f*density &&
            nodeHeight*scale>=48f*density
    }

    data class Point(val x:Float,val y:Float)
    // Measured reference centers: three stepping stones on Sky Meadows,
    // two in Crystal Ruins and one on Volcano Peaks (not six on one island).
    // Nodes are actual selected-world stages, so disabled = truly locked.
    val nodes=listOf(
        Point(45f,969f),     // Meadow blue stage 1
        Point(129f,1117f),   // Meadow blue stage 2
        Point(299f,1107f),   // Meadow golden stage 3
        Point(421f,596f),    // Crystal stage 4
        Point(533f,681f),    // Crystal stage 5
        Point(508f,207f)     // Volcano locked stage 6
    )
    val plaques=listOf(
        Point(49f,739f),    // Sky Meadows wooden sign (lower-left)
        Point(642f,572f),   // Crystal Ruins stone sign (middle-right)
        Point(185f,133f)    // Volcano Peaks lava sign (upper-left)
    )
    const val nodeWidth=108f
    const val nodeHeight=107f
    fun starStrip(index:Int):Point {
        require(index in 0 until 3)
        val p=nodes[index]
        return Point(p.x-24f,p.y+nodeHeight-6f)
    }
    fun nodesInsideReference():Boolean=nodes.all{
        it.x>=0f && it.y>=0f &&
        it.x+nodeWidth<=width && it.y+nodeHeight<=height
    }
    fun starStripsInsideReference():Boolean=(0..2).all{
        val p=starStrip(it)
        p.x>=0f && p.y>=0f && p.x+156f<=width && p.y+42f<=height
    }
    fun firstThreeMarkersAndStarsDoNotOverlap():Boolean=(0..2).all{index->
        val strip=starStrip(index)
        val starRight=strip.x+156f
        val starBottom=strip.y+42f
        (0..2).filter{it!=index}.all{other->
            val marker=nodes[other]
            val separate=starRight<=marker.x ||
                strip.x>=marker.x+nodeWidth ||
                starBottom<=marker.y ||
                strip.y>=marker.y+nodeHeight
            separate
        }
    }


    /** Pure 941x1672 layout guard: markers must not collide with each other,
     * world plaques or the six native navigation/currency controls. */
    private fun intersects(a:FloatArray,b:FloatArray):Boolean =
        a[0]<b[2] && a[2]>b[0] && a[1]<b[3] && a[3]>b[1]

    fun controlAndNodeRectanglesSafe():Boolean {
        val markers=nodes.map{floatArrayOf(it.x,it.y,
            it.x+nodeWidth,it.y+nodeHeight)}
        val signs=plaques.mapIndexed{index,p->
            floatArrayOf(p.x,p.y,p.x+(if(index==0)342f else 286f),
                p.y+109f)
        }
        val controls=listOf(
            floatArrayOf(20f,16f,142f,111f), // HOME
            floatArrayOf(481f,23f,684f,96f), // coins
            floatArrayOf(706f,23f,917f,96f), // stars
            floatArrayOf(138f,1449f,804f,1571f), // PLAY
            floatArrayOf(19f,1353f,85f,1430f), // previous worlds
            floatArrayOf(855f,1353f,921f,1430f)) // more worlds
        val all=markers+signs+controls
        if(all.any{it[0]<0f || it[1]<0f || it[2]>width ||
            it[3]>height || it[0]>=it[2] || it[1]>=it[3]})return false
        return markers.indices.all{i->
            markers.indices.all{j->
                i==j || !intersects(markers[i],markers[j])
            } && (signs+controls).none{intersects(markers[i],it)}
        } && controls.indices.all{i->
            controls.indices.all{j->
                i==j || !intersects(controls[i],controls[j])
            }
        }
    }

    /**
     * Three native illustrated islands per page. The first page retains the
     * existing owner reference's precise meadow/crystal/volcano colors.
     * Other page palettes distinguish actual worlds without altering
     * hitboxes, unlock logic or previously saved progress.
     * Slots: 0 lower, 1 middle, 2 upper island.
     */
    fun islandPalette(world:Int,slot:Int):IntArray {
        require(slot in 0..2)
        return when(pageStart(world)) {
            0 -> when(slot) {
                0 -> intArrayOf(0xff3eae59.toInt(),0xff8b7269.toInt(),0xffa6ef75.toInt())
                1 -> intArrayOf(0xff288fe2.toInt(),0xff646b96.toInt(),0xff8de6ff.toInt())
                else -> intArrayOf(0xffe55b3d.toInt(),0xff664454.toInt(),0xfffa9b54.toInt())
            }
            3 -> when(slot) {
                0 -> intArrayOf(0xff6352aa.toInt(),0xff38345f.toInt(),0xffc2a5f6.toInt())
                1 -> intArrayOf(0xff24b9cc.toInt(),0xff3b7493.toInt(),0xffa0fff1.toInt())
                else -> intArrayOf(0xff84bce8.toInt(),0xff405888.toInt(),0xffe5f4ff.toInt())
            }
            else -> when(slot) {
                0 -> intArrayOf(0xff39a567.toInt(),0xff466e64.toInt(),0xffbcf7b6.toInt())
                1 -> intArrayOf(0xff745dc7.toInt(),0xff3d315e.toInt(),0xffc1b3ff.toInt())
                else -> intArrayOf(0xff3e5b9e.toInt(),0xff292c65.toInt(),0xff9eaeef.toInt())
            }
        }
    }

    fun pageStart(selected:Int):Int=(selected.coerceIn(0,7)/3)*3
    fun displayedWorlds(selected:Int):List<Int> {
        val start=pageStart(selected)
        return (start until minOf(start+3,LevelEngine.WORLD_COUNT)).toList()
    }
    fun nearestLevels(frontier:Int):List<Int> {
        val cap=LevelEngine.LEVELS_PER_WORLD
        val safe=frontier.coerceIn(1,cap)
        val first=(safe-2).coerceAtLeast(1).coerceAtMost(cap-5)
        return (first..first+5).toList()
    }
    fun playableFrontier(frontier:Int):Int=frontier.coerceIn(1,LevelEngine.LEVELS_PER_WORLD)
    fun isUnlocked(level:Int,frontier:Int):Boolean =
        level in 1..LevelEngine.LEVELS_PER_WORLD && level<=frontier
}
