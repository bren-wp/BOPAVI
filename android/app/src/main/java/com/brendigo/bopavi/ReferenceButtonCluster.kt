package com.brendigo.bopavi

/** Match the reference's compact, centered 3D icon+text buttons in View pixels. */
internal object ReferenceButtonCluster {
    data class Sizes(val iconWidth:Int,val labelWidth:Int,val gap:Int) {
        val totalWidth:Int get()=iconWidth+labelWidth+gap
    }

    fun measure(width:Int,height:Int,textAdvance:Float,
                iconHeightFraction:Float,iconWidthFraction:Float):Sizes {
        require(width>0 && height>0)
        require(iconHeightFraction>0f && iconWidthFraction>0f)
        val gap=(width*.016f).toInt().coerceIn(4,12)
        val icon=minOf((height*iconHeightFraction).toInt(),
            (width*iconWidthFraction).toInt(),width-gap-1).coerceAtLeast(1)
        val remaining=(width-icon-gap).coerceAtLeast(1)
        val label=(textAdvance.coerceAtLeast(0f)*1.12f).toInt()
            .coerceIn(1,remaining)
        return Sizes(icon,label,gap)
    }
}
