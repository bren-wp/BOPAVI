package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View

/**
 * A separate local illustration for the ORIGINAL aviator's Game Over pose.
 * Unlike the existing portrait resource, this character is seated, has
 * folded wings, lowered brows and resting feet on the stone podium.
 * Entirely decorative: Retry/Home remain separate accessible Android views.
 */
internal class GameOverSeatedBopi(context:Context):View(context) {
    private val brush=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val contour=Path()

    init { importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO }

    private fun oval(c:Canvas,l:Float,t:Float,r:Float,b:Float,
                     top:Int,bottom:Int,edge:Int=0xff153870.toInt(),
                     stroke:Float=4f) {
        brush.reset()
        brush.isAntiAlias=true
        brush.style=Paint.Style.FILL
        brush.shader=LinearGradient(l,t,l,b,top,bottom,Shader.TileMode.CLAMP)
        c.drawOval(l,t,r,b,brush)
        brush.shader=null
        if(stroke>0f){
            brush.style=Paint.Style.STROKE
            brush.strokeWidth=stroke
            brush.color=edge
            c.drawOval(l,t,r,b,brush)
            brush.style=Paint.Style.FILL
        }
    }

    private fun polygon(c:Canvas,fill:Int,edge:Int=0xff153870.toInt(),
                        stroke:Float=4f,build:Path.()->Unit) {
        contour.reset()
        contour.build()
        contour.close()
        brush.reset()
        brush.isAntiAlias=true
        brush.style=Paint.Style.FILL
        brush.color=fill
        c.drawPath(contour,brush)
        if(stroke>0f){
            brush.style=Paint.Style.STROKE
            brush.strokeWidth=stroke
            brush.strokeJoin=Paint.Join.ROUND
            brush.color=edge
            c.drawPath(contour,brush)
            brush.style=Paint.Style.FILL
        }
    }

    private fun line(c:Canvas,x1:Float,y1:Float,x2:Float,y2:Float,
                     color:Int,thickness:Float){
        brush.reset()
        brush.isAntiAlias=true
        brush.style=Paint.Style.STROKE
        brush.strokeCap=Paint.Cap.ROUND
        brush.strokeWidth=thickness
        brush.color=color
        c.drawLine(x1,y1,x2,y2,brush)
        brush.style=Paint.Style.FILL
    }

    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        if(width==0 || height==0)return
        val checkpoint=c.save()
        c.scale(width/512f,height/512f)

        // The feet touch the REAL grassy podium, with no fake overlay button.
        oval(c,111f,424f,403f,451f,0x55092339,0x11092339,
            stroke=0f)

        // A relaxed scarf tail trails down beside the seated character.
        polygon(c,0xffdb242d.toInt(),0xff7a1326.toInt(),5f){
            moveTo(188f,331f)
            cubicTo(113f,316f,95f,362f,42f,353f)
            cubicTo(82f,402f,119f,409f,176f,377f)
        }
        polygon(c,0xfff94e34.toInt(),0xffa41e28.toInt(),3f){
            moveTo(169f,328f)
            cubicTo(116f,323f,98f,350f,79f,362f)
            cubicTo(111f,375f,144f,367f,185f,354f)
        }

        // Wings are tucked downward rather than held up in a flying pose.
        polygon(c,0xff0878d8.toInt(),0xff064d9d.toInt(),6f){
            moveTo(164f,271f)
            cubicTo(103f,260f,65f,316f,86f,369f)
            cubicTo(100f,390f,126f,396f,150f,381f)
            cubicTo(157f,409f,186f,417f,205f,396f)
            cubicTo(193f,355f,195f,302f,164f,271f)
        }
        oval(c,101f,313f,157f,378f,0xff40b7ff.toInt(),0xff0679d6.toInt(),
            stroke=3f)
        oval(c,116f,344f,179f,398f,0xff2da0f8.toInt(),0xff086bc8.toInt(),
            stroke=3f)
        polygon(c,0xff0574d6.toInt(),0xff07458f.toInt(),6f){
            moveTo(353f,281f)
            cubicTo(412f,283f,451f,320f,434f,371f)
            cubicTo(420f,398f,388f,397f,370f,383f)
            cubicTo(369f,414f,334f,411f,313f,394f)
            cubicTo(330f,358f,326f,307f,353f,281f)
        }
        oval(c,375f,328f,427f,389f,0xff45b7ff.toInt(),0xff0575cb.toInt(),
            stroke=3f)

        // Round blue seated body and soft white belly, outlined in navy.
        oval(c,142f,253f,371f,433f,0xff2aa7ff.toInt(),0xff0756bb.toInt(),
            stroke=7f)
        oval(c,180f,297f,351f,421f,Color.WHITE,0xffe2edf8.toInt(),
            0xff2776bd.toInt(),3f)
        oval(c,208f,347f,322f,419f,0xffffffff.toInt(),0xffe9f1ff.toInt(),
            stroke=0f)

        // Large sitting toes, intentionally BELOW the belly.
        for(x in arrayOf(181f,287f)){
            oval(c,x,398f,x+74f,450f,0xffffdd3a.toInt(),0xfff49b00.toInt(),
                0xff9b4d03.toInt(),5f)
            oval(c,x+7f,405f,x+25f,431f,0xfffff1a2.toInt(),
                0xffffc61f.toInt(),stroke=0f)
            line(c,x+33f,423f,x+33f,444f,0xffb36804.toInt(),3f)
            line(c,x+51f,420f,x+52f,440f,0xffb36804.toInt(),3f)
        }

        // Distinct neck scarf and circular BOPAVI gold crown medallion.
        polygon(c,0xffed3133.toInt(),0xff9d1525.toInt(),6f){
            moveTo(166f,320f)
            cubicTo(221f,355f,310f,361f,360f,316f)
            lineTo(360f,354f)
            cubicTo(318f,395f,207f,393f,170f,356f)
        }
        oval(c,232f,352f,294f,414f,0xfffff2ad.toInt(),0xfff2a209.toInt(),
            0xff6a3206.toInt(),6f)
        polygon(c,0xffffd729.toInt(),0xff8c5108.toInt(),3f){
            moveTo(245f,379f)
            lineTo(252f,384f)
            lineTo(263f,369f)
            lineTo(272f,384f)
            lineTo(281f,378f)
            lineTo(279f,395f)
            lineTo(247f,395f)
        }

        // Dropped head, gentle crest and white cheeks make a SITTING pose.
        polygon(c,0xff078fea.toInt(),0xff035bad.toInt(),5f){
            moveTo(202f,122f)
            cubicTo(215f,87f,246f,74f,268f,65f)
            cubicTo(254f,93f,251f,102f,245f,123f)
            cubicTo(282f,87f,301f,93f,324f,101f)
            cubicTo(305f,113f,290f,133f,284f,149f)
        }
        oval(c,117f,110f,399f,363f,0xff58c6ff.toInt(),0xff0473cf.toInt(),
            0xff0b519d.toInt(),7f)
        oval(c,143f,184f,374f,344f,0xffffffff.toInt(),0xffedf5ff.toInt(),
            0xffb8def8.toInt(),3f)

        // Leather aviator strap and separate polished golden blue lenses.
        val strap=c.save()
        c.rotate(-10f,260f,155f)
        brush.reset()
        brush.isAntiAlias=true
        brush.color=0xff59301f.toInt()
        c.drawRoundRect(137f,139f,381f,179f,16f,16f,brush)
        brush.style=Paint.Style.STROKE
        brush.color=0xffb97d39.toInt()
        brush.strokeWidth=5f
        c.drawRoundRect(137f,139f,381f,179f,16f,16f,brush)
        c.restoreToCount(strap)
        for(center in arrayOf(207f,328f)){
            oval(c,center-55f,104f,center+55f,183f,
                0xfff9d15a.toInt(),0xffb76d10.toInt(),
                0xff6b3411.toInt(),6f)
            oval(c,center-42f,114f,center+42f,172f,
                0xffb4f8ff.toInt(),0xff0878bf.toInt(),
                0xff735020.toInt(),4f)
            line(c,center-18f,120f,center-31f,147f,0xccffffff.toInt(),8f)
        }

        // Sad lowered brows, large eyes and a tiny glistening tear.
        oval(c,174f,211f,241f,303f,Color.WHITE,0xffe7f7ff.toInt(),
            0xff68413e.toInt(),3f)
        oval(c,279f,219f,346f,314f,Color.WHITE,0xffeaf7ff.toInt(),
            0xff68413e.toInt(),3f)
        oval(c,195f,239f,236f,301f,0xff48251d.toInt(),0xff20111a.toInt(),
            stroke=2f)
        oval(c,285f,248f,325f,310f,0xff48251d.toInt(),0xff20111a.toInt(),
            stroke=2f)
        oval(c,208f,247f,220f,268f,Color.WHITE,Color.WHITE,stroke=0f)
        oval(c,297f,255f,309f,277f,Color.WHITE,Color.WHITE,stroke=0f)
        line(c,176f,207f,234f,227f,0xff3f2330.toInt(),10f)
        line(c,288f,231f,344f,211f,0xff3f2330.toInt(),10f)
        oval(c,159f,287f,194f,304f,0xffffb8bd.toInt(),0xffef9ba6.toInt(),
            stroke=0f)
        oval(c,337f,290f,371f,307f,0xffffb8bd.toInt(),0xffef9ba6.toInt(),
            stroke=0f)
        polygon(c,0xff80e7ff.toInt(),0xff2487cb.toInt(),2f){
            moveTo(337f,300f)
            cubicTo(349f,315f,344f,330f,335f,334f)
            cubicTo(327f,325f,330f,312f,337f,300f)
        }

        // Closed drooping orange beak replaces the portrait's open smile.
        polygon(c,0xffffc419.toInt(),0xffae5c05.toInt(),5f){
            moveTo(236f,292f)
            quadTo(270f,279f,290f,299f)
            quadTo(277f,323f,253f,319f)
        }
        line(c,248f,315f,279f,313f,0xffa55912.toInt(),3f)
        c.restoreToCount(checkpoint)
    }
}
