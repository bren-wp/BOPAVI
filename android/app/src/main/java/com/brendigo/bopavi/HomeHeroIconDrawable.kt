package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Embossed Home pictograms. Each icon has its own layered geometry instead
 * of platform emoji or a flat tinted glyph. Original offline Canvas artwork.
 */
internal class HomeHeroIconDrawable(
    private val kind:BopaviUiIconDrawable.Kind,
    private val size:Int
):Drawable(){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var alpha=255
    private var filter:ColorFilter?=null

    private fun setup(color:Int,style:Paint.Style=Paint.Style.FILL,width:Float=2f){
        paint.reset()
        paint.isAntiAlias=true
        paint.style=style
        paint.strokeJoin=Paint.Join.ROUND
        paint.strokeCap=Paint.Cap.ROUND
        paint.strokeWidth=width
        paint.color=Color.argb(Color.alpha(color)*alpha/255,
            Color.red(color),Color.green(color),Color.blue(color))
        paint.colorFilter=filter
    }

    private fun enamel(canvas:Canvas,path:Path,
                      light:Int,dark:Int,outline:Int,stroke:Float=5f){
        val behind=canvas.save()
        canvas.translate(0f,4f)
        setup(0x88051a57.toInt(),Paint.Style.STROKE,stroke+4f)
        canvas.drawPath(path,paint)
        canvas.restoreToCount(behind)
        setup(outline,Paint.Style.STROKE,stroke)
        canvas.drawPath(path,paint)
        setup(light)
        paint.shader=LinearGradient(0f,6f,0f,93f,light,dark,
            Shader.TileMode.CLAMP)
        canvas.drawPath(path,paint)
        paint.shader=null
    }

    private fun polygon(vararg xy:Float):Path=Path().apply{
        moveTo(xy[0],xy[1])
        var i=2
        while(i<xy.size){lineTo(xy[i],xy[i+1]);i+=2}
        close()
    }


    /**
     * A native faceted purple crystal for earned stars. This changes
     * artwork only: the value remains ProgressStore.worldStars and
     * GameSimulation's real star pickups, never a fictitious gem wallet.
     */
    private fun gem(canvas:Canvas){
        val outer=polygon(50f,5f,79f,15f,96f,40f,50f,97f,4f,40f,21f,15f)
        val depth=canvas.save()
        canvas.translate(0f,4f)
        setup(0x88081852.toInt())
        canvas.drawPath(outer,paint)
        canvas.restoreToCount(depth)
        enamel(canvas,outer,0xffff83ff.toInt(),0xffa808d5.toInt(),
            0xff300a72.toInt(),4f)
        val facetLeft=polygon(21f,17f,49f,6f,39f,42f,9f,41f)
        setup(0xfffaaaff.toInt())
        canvas.drawPath(facetLeft,paint)
        val facetTop=polygon(21f,17f,79f,17f,63f,41f,39f,41f)
        setup(0xffffb5ff.toInt())
        canvas.drawPath(facetTop,paint)
        val facetCenter=polygon(39f,41f,63f,41f,50f,88f)
        setup(0xffe636fa.toInt())
        canvas.drawPath(facetCenter,paint)
        val facetRight=polygon(63f,41f,92f,40f,50f,88f)
        setup(0xff8a16c7.toInt())
        canvas.drawPath(facetRight,paint)
        setup(0xddffffff.toInt(),Paint.Style.STROKE,3f)
        canvas.drawLine(23f,19f,39f,15f,paint)
        canvas.drawLine(39f,15f,54f,15f,paint)
    }

    private fun counterStar(canvas:Canvas){
        val star=Path()
        for(i in 0 until 10){
            val angle=-PI/2.0+i*PI/5.0
            val radius=if(i%2==0)45f else 21f
            val x=50f+cos(angle).toFloat()*radius
            val y=49f+sin(angle).toFloat()*radius
            if(i==0)star.moveTo(x,y) else star.lineTo(x,y)
        }
        star.close()
        enamel(canvas,star,0xfffff8ae.toInt(),0xffffaa00.toInt(),
            0xffb85a0b.toInt(),5.5f)
        setup(0xaaffffff.toInt(),Paint.Style.STROKE,2.5f)
        canvas.drawLine(32f,33f,48f,15f,paint)
    }


    /** Shared 3D golden rim for wallet and recorded-result coins. */
    private fun coinRim(canvas:Canvas){
        setup(0x88082459.toInt())
        canvas.drawCircle(51f,54f,44f,paint)
        setup(0xffb9550b.toInt())
        canvas.drawCircle(50f,50f,44f,paint)
        setup(0xffffdb49.toInt())
        canvas.drawCircle(50f,46f,40f,paint)
        setup(0xffffe988.toInt())
        canvas.drawCircle(50f,44f,34f,paint)
        setup(0xffc76d13.toInt(),Paint.Style.STROKE,4f)
        canvas.drawCircle(50f,44f,31f,paint)
    }

    /** The owner Home wallet coin has a raised dollar symbol. */
    private fun counterCoin(canvas:Canvas){
        coinRim(canvas)
        setup(0xff92510e.toInt(),Paint.Style.STROKE,7f)
        paint.typeface=android.graphics.Typeface.create(
            "sans-serif-black",android.graphics.Typeface.BOLD)
        paint.textAlign=Paint.Align.CENTER
        paint.textSize=55f
        canvas.drawText("$",50f,65f,paint)
        setup(0xfffff9b2.toInt())
        paint.typeface=android.graphics.Typeface.create(
            "sans-serif-black",android.graphics.Typeface.BOLD)
        paint.textAlign=Paint.Align.CENTER
        paint.textSize=55f
        canvas.drawText("$",50f,64f,paint)
    }

    /** Actual collected coins have the distinct gold crown on Game Over. */
    private fun crownCoin(canvas:Canvas){
        coinRim(canvas)
        val crown=polygon(27f,36f,36f,48f,50f,28f,64f,48f,
            73f,36f,68f,65f,32f,65f)
        enamel(canvas,crown,0xfffff9ae.toInt(),0xffffb91e.toInt(),
            0xff96520d.toInt(),2.8f)
        setup(0xcaffffff.toInt(),Paint.Style.STROKE,2.5f)
        canvas.drawLine(39f,47f,50f,34f,paint)
    }

    private fun play(canvas:Canvas){
        val triangle=Path().apply{
            moveTo(21f,10f)
            quadTo(17f,6f,17f,18f)
            lineTo(17f,81f)
            quadTo(18f,93f,29f,89f)
            lineTo(83f,56f)
            quadTo(94f,49f,82f,42f)
            close()
        }
        enamel(canvas,triangle,Color.WHITE,0xffe6efff.toInt(),
            0xff9b4514.toInt(),7f)
        setup(0x88ffffff.toInt(),Paint.Style.STROKE,4f)
        canvas.drawLine(27f,24f,27f,73f,paint)
    }

    private fun shirt(canvas:Canvas){
        setup(0xfff7faff.toInt(),Paint.Style.STROKE,6f)
        canvas.drawArc(RectF(39f,3f,61f,29f),200f,220f,false,paint)
        val jersey=polygon(30f,19f,43f,28f,57f,28f,70f,19f,
            94f,33f,81f,52f,72f,48f,72f,88f,28f,88f,
            28f,48f,19f,52f,6f,33f)
        enamel(canvas,jersey,0xff48cbff.toInt(),0xff0458d2.toInt(),
            0xffe2f5ff.toInt(),4f)
        setup(0xffdffbff.toInt(),Paint.Style.STROKE,2.5f)
        canvas.drawLine(32f,79f,68f,79f,paint)
        val feather=Path().apply{
            moveTo(30f,65f)
            cubicTo(32f,47f,52f,37f,69f,39f)
            cubicTo(65f,57f,48f,66f,30f,65f)
            close()
        }
        enamel(canvas,feather,Color.WHITE,0xffc6eaff.toInt(),
            0xff166dbd.toInt(),1f)
        setup(0xff3288c8.toInt(),Paint.Style.STROKE,2f)
        canvas.drawLine(28f,70f,65f,43f,paint)
    }

    private fun shop(canvas:Canvas){
        val shopBody=Path().apply{
            addRoundRect(RectF(15f,32f,85f,91f),7f,7f,Path.Direction.CW)
        }
        enamel(canvas,shopBody,0xffffe3ab.toInt(),0xffe89028.toInt(),
            0xffad4e16.toInt(),4f)
        val window=Path().apply{
            addRoundRect(RectF(26f,45f,60f,75f),3f,3f,Path.Direction.CW)
        }
        enamel(canvas,window,0xff62f7ff.toInt(),0xff1387e6.toInt(),
            0xff00518f.toInt(),3f)
        setup(0x99ffffff.toInt(),Paint.Style.STROKE,3f)
        canvas.drawLine(32f,70f,52f,49f,paint)
        val awning=polygon(19f,14f,78f,14f,87f,37f,12f,37f)
        enamel(canvas,awning,0xfffff6f4.toInt(),0xfff6caca.toInt(),
            0xffa22b32.toInt(),4f)
        for(i in 0..3){
            val x=20f+i*16f
            setup(0xffee3543.toInt())
            canvas.drawRect(x,18f,x+9f,34f,paint)
        }
        setup(0xffffd222.toInt())
        canvas.drawCircle(78f,71f,16f,paint)
        setup(0xffba6510.toInt(),Paint.Style.STROKE,4f)
        canvas.drawCircle(78f,71f,14f,paint)
        setup(0xfffff2b0.toInt(),Paint.Style.STROKE,2f)
        canvas.drawCircle(78f,71f,10f,paint)
    }

    private fun gear(canvas:Canvas){
        val teeth=Path()
        for(i in 0 until 32){
            val theta=-PI/2.0+i*PI/16.0
            val radius=if(i%4<2)45f else 35f
            val x=50f+cos(theta).toFloat()*radius
            val y=50f+sin(theta).toFloat()*radius
            if(i==0)teeth.moveTo(x,y) else teeth.lineTo(x,y)
        }
        teeth.close()
        enamel(canvas,teeth,0xffffffff.toInt(),0xffb2c6ee.toInt(),
            0xff1a458b.toInt(),4f)
        setup(0xff003d90.toInt())
        canvas.drawCircle(50f,50f,20f,paint)
        setup(0xff83adf3.toInt(),Paint.Style.STROKE,6f)
        canvas.drawCircle(50f,50f,17f,paint)
        setup(0xffe2f2ff.toInt(),Paint.Style.STROKE,3f)
        canvas.drawArc(RectF(19f,19f,81f,81f),198f,114f,false,paint)
    }

    private fun trophy(canvas:Canvas){
        val cup=Path().apply{
            moveTo(26f,17f);lineTo(74f,17f);lineTo(70f,48f)
            cubicTo(67f,64f,56f,70f,50f,70f)
            cubicTo(44f,70f,33f,64f,30f,48f)
            close()
        }
        enamel(canvas,cup,0xfffff69d.toInt(),0xffe98904.toInt(),
            0xffa65608.toInt(),4f)
        setup(0xffe4a523.toInt(),Paint.Style.STROKE,8f)
        canvas.drawArc(RectF(12f,18f,41f,57f),77f,195f,false,paint)
        canvas.drawArc(RectF(59f,18f,88f,57f),-92f,195f,false,paint)
        setup(0xffa7570b.toInt(),Paint.Style.STROKE,9f)
        canvas.drawLine(50f,68f,50f,84f,paint)
        setup(0xffffdc39.toInt())
        canvas.drawRoundRect(RectF(29f,81f,71f,91f),4f,4f,paint)
        val star=Path()
        for(i in 0 until 10){
            val theta=-PI/2.0+i*PI/5.0
            val rad=if(i%2==0)15f else 7f
            val x=50f+cos(theta).toFloat()*rad
            val y=42f+sin(theta).toFloat()*rad
            if(i==0)star.moveTo(x,y) else star.lineTo(x,y)
        }
        star.close()
        setup(0xfffff4a0.toInt())
        canvas.drawPath(star,paint)
    }

    /**
     * Reference Home has a blue/gold three-step podium, not a generic dot.
     * Keep every podium number inside the raised local vector icon so it
     * scales with the native leaderboard shortcut at every display density.
     */
    private fun podium(canvas:Canvas){
        fun step(l:Float,t:Float,r:Float,b:Float,light:Int,dark:Int){
            val block=Path().apply {
                addRoundRect(RectF(l,t,r,b),4f,4f,Path.Direction.CW)
            }
            enamel(canvas,block,light,dark,0xff0b2f71.toInt(),3f)
        }
        step(9f,44f,36f,89f,0xffa1e9ff.toInt(),0xff1762b9.toInt())
        step(36f,20f,65f,89f,0xfffff29c.toInt(),0xffe39409.toInt())
        step(65f,52f,91f,89f,0xffffbf9b.toInt(),0xffae5525.toInt())
        paint.reset()
        paint.isAntiAlias=true
        paint.typeface=android.graphics.Typeface.create(
            "sans-serif-black",android.graphics.Typeface.BOLD)
        paint.textAlign=Paint.Align.CENTER
        paint.textSize=22f
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=4f
        paint.strokeJoin=Paint.Join.ROUND
        paint.color=0xff092a61.toInt()
        for((number,x,y) in listOf(Triple("2",22.5f,72f),
            Triple("1",50.5f,53f),Triple("3",78f,76f))){
            canvas.drawText(number,x,y,paint)
        }
        paint.style=Paint.Style.FILL
        paint.color=Color.WHITE
        for((number,x,y) in listOf(Triple("2",22.5f,72f),
            Triple("1",50.5f,53f),Triple("3",78f,76f))){
            canvas.drawText(number,x,y,paint)
        }
    }

    /** Raised blue sapphire shield, with an inset polished golden star. */
    private fun shield(canvas:Canvas){
        val outline=Path().apply {
            moveTo(50f,7f)
            cubicTo(65f,19f,78f,19f,89f,24f)
            lineTo(84f,64f)
            cubicTo(77f,82f,63f,92f,50f,98f)
            cubicTo(37f,92f,23f,82f,16f,64f)
            lineTo(11f,24f)
            cubicTo(22f,19f,35f,19f,50f,7f)
            close()
        }
        enamel(canvas,outline,0xff8aefff.toInt(),0xff084bba.toInt(),
            0xff062f74.toInt(),5f)
        val inner=Path().apply {
            moveTo(50f,19f);lineTo(77f,30f)
            lineTo(73f,62f);quadTo(68f,77f,50f,86f)
            quadTo(32f,77f,27f,62f);lineTo(23f,30f);close()
        }
        enamel(canvas,inner,0xffa8f4ff.toInt(),0xff126ac8.toInt(),
            0xff0b4798.toInt(),2f)
        val star=Path()
        for(i in 0 until 10){
            val angle=-PI/2.0+i*PI/5.0
            val radius=if(i%2==0)20f else 9f
            val x=50f+cos(angle).toFloat()*radius
            val y=49f+sin(angle).toFloat()*radius
            if(i==0)star.moveTo(x,y) else star.lineTo(x,y)
        }
        star.close()
        enamel(canvas,star,0xfffff8a4.toInt(),0xffee9a0e.toInt(),
            0xff99520e.toInt(),3f)
    }

    /** 3D horseshoe magnet with genuinely separate pale metallic poles. */
    private fun magnet(canvas:Canvas){
        val arc=Path().apply {
            moveTo(17f,17f);lineTo(37f,17f)
            lineTo(37f,56f)
            cubicTo(37f,73f,63f,73f,63f,56f)
            lineTo(63f,17f);lineTo(83f,17f)
            lineTo(83f,57f)
            cubicTo(83f,105f,17f,105f,17f,57f)
            close()
        }
        enamel(canvas,arc,0xffff8298.toInt(),0xffd22256.toInt(),
            0xff3c235f.toInt(),5f)
        setup(0xfff7fdff.toInt())
        canvas.drawRoundRect(RectF(17f,17f,37f,34f),3f,3f,paint)
        canvas.drawRoundRect(RectF(63f,17f,83f,34f),3f,3f,paint)
        setup(0xff97dfff.toInt(),Paint.Style.STROKE,2f)
        canvas.drawLine(21f,20f,32f,20f,paint)
        canvas.drawLine(67f,20f,78f,20f,paint)
        setup(0x88ffffff.toInt(),Paint.Style.STROKE,3f)
        canvas.drawArc(RectF(25f,43f,75f,88f),18f,146f,false,paint)
    }

    /** Raised red location pin for genuine recorded flight distance. */
    private fun distancePin(canvas:Canvas){
        val pin=Path().apply{
            moveTo(50f,96f)
            cubicTo(40f,78f,17f,52f,17f,39f)
            cubicTo(17f,19f,32f,7f,50f,7f)
            cubicTo(68f,7f,83f,19f,83f,39f)
            cubicTo(83f,52f,60f,78f,50f,96f)
            close()
        }
        enamel(canvas,pin,0xffff8b66.toInt(),0xffcf2331.toInt(),
            0xff7b1431.toInt(),5f)
        setup(0xff802343.toInt())
        canvas.drawCircle(50f,39f,14f,paint)
        setup(0xfffffaf4.toInt())
        canvas.drawCircle(50f,36f,11f,paint)
        setup(0xbaffffff.toInt(),Paint.Style.STROKE,3.5f)
        canvas.drawArc(RectF(25f,15f,72f,60f),208f,95f,false,paint)
    }

    /** Real endless-run berry, separate from coins and stars. */
    private fun berry(canvas:Canvas){
        val fruit=Path().apply{
            moveTo(50f,26f)
            cubicTo(75f,11f,89f,39f,82f,61f)
            cubicTo(72f,86f,56f,95f,50f,96f)
            cubicTo(44f,95f,28f,86f,18f,61f)
            cubicTo(11f,39f,25f,11f,50f,26f)
            close()
        }
        enamel(canvas,fruit,0xffff9a8d.toInt(),0xffdb1f4c.toInt(),
            0xff761449.toInt(),4f)
        val leaf=polygon(50f,27f,35f,15f,16f,22f,35f,31f,
            50f,25f,65f,31f,84f,22f,65f,15f)
        enamel(canvas,leaf,0xffbaff8e.toInt(),0xff2a9e53.toInt(),
            0xff18694c.toInt(),2.5f)
        setup(0xaaffffff.toInt())
        canvas.drawOval(RectF(28f,40f,36f,52f),paint)
        setup(0xffffefac.toInt())
        for(i in 0..3){
            canvas.drawCircle(39f+(i%2)*18f,58f+(i/2)*13f,2f,paint)
        }
    }


    /** Original raised white retry arrow with a cinnamon enamel outline. */
    private fun retry(canvas:Canvas){
        val arc=Path().apply {
            addArc(RectF(16f,24f,84f,89f),-50f,-275f)
        }
        val shadow=canvas.save()
        canvas.translate(0f,4f)
        setup(0x99092254.toInt(),Paint.Style.STROKE,24f)
        canvas.drawPath(arc,paint)
        canvas.restoreToCount(shadow)
        setup(0xff783411.toInt(),Paint.Style.STROKE,22f)
        canvas.drawPath(arc,paint)
        setup(Color.WHITE,Paint.Style.STROKE,15f)
        canvas.drawPath(arc,paint)
        val arrow=polygon(59f,5f,91f,23f,62f,46f,61f,33f,
            44f,32f,44f,18f)
        enamel(canvas,arrow,Color.WHITE,0xffd5e6f7.toInt(),
            0xff763511.toInt(),4f)
        setup(0xbbffffff.toInt(),Paint.Style.STROKE,3f)
        canvas.drawArc(RectF(22f,29f,78f,80f),203f,97f,false,paint)
    }

    /** Rounded white 3D house icon for the actual blue HOME result action. */
    private fun home(canvas:Canvas){
        val walls=Path().apply{
            moveTo(25f,52f);lineTo(50f,28f);lineTo(75f,52f)
            lineTo(75f,87f);quadTo(75f,91f,71f,91f)
            lineTo(29f,91f);quadTo(25f,91f,25f,87f);close()
        }
        enamel(canvas,walls,Color.WHITE,0xffc5d9fa.toInt(),
            0xff0b3172.toInt(),4.5f)
        val roof=polygon(10f,50f,47f,13f,52f,13f,90f,50f,
            81f,62f,50f,34f,20f,62f)
        enamel(canvas,roof,Color.WHITE,0xffd2e6ff.toInt(),
            0xff082b72.toInt(),5f)
        val door=Path().apply {
            addRoundRect(RectF(43f,68f,57f,92f),2f,2f,Path.Direction.CW)
        }
        enamel(canvas,door,0xff73caff.toInt(),0xff1262b9.toInt(),
            0xff0b3172.toInt(),1.8f)
        setup(0x99ffffff.toInt(),Paint.Style.STROKE,2f)
        canvas.drawLine(28f,53f,47f,36f,paint)
    }

    private fun sound(canvas:Canvas){
        val speaker=polygon(13f,40f,29f,40f,51f,22f,51f,78f,
            29f,60f,13f,60f)
        enamel(canvas,speaker,0xfff9feff.toInt(),0xffcbdcf9.toInt(),
            0xff244f9e.toInt(),5f)
        setup(0xffeaf6ff.toInt(),Paint.Style.STROKE,6f)
        canvas.drawArc(RectF(38f,27f,75f,73f),-55f,110f,false,paint)
        canvas.drawArc(RectF(40f,13f,95f,87f),-55f,110f,false,paint)
        setup(0xff294c9b.toInt(),Paint.Style.STROKE,2f)
        canvas.drawArc(RectF(43f,16f,95f,84f),-55f,110f,false,paint)
    }

    override fun draw(canvas:Canvas){
        if(bounds.isEmpty)return
        val save=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        canvas.scale(bounds.width()/100f,bounds.height()/100f)
        when(kind){
            BopaviUiIconDrawable.Kind.PLAY->play(canvas)
            BopaviUiIconDrawable.Kind.STAR->counterStar(canvas)
            BopaviUiIconDrawable.Kind.GEM->gem(canvas)
            BopaviUiIconDrawable.Kind.COIN->counterCoin(canvas)
            BopaviUiIconDrawable.Kind.CROWN_COIN->crownCoin(canvas)
            BopaviUiIconDrawable.Kind.SHIRT->shirt(canvas)
            BopaviUiIconDrawable.Kind.SHOP->shop(canvas)
            BopaviUiIconDrawable.Kind.SETTINGS->gear(canvas)
            BopaviUiIconDrawable.Kind.RECORDS->trophy(canvas)
            BopaviUiIconDrawable.Kind.LEADERBOARD->podium(canvas)
            BopaviUiIconDrawable.Kind.RESTART->retry(canvas)
            BopaviUiIconDrawable.Kind.HOME->home(canvas)
            BopaviUiIconDrawable.Kind.GATE->distancePin(canvas)
            BopaviUiIconDrawable.Kind.BERRY->berry(canvas)
            BopaviUiIconDrawable.Kind.SHIELD->shield(canvas)
            BopaviUiIconDrawable.Kind.MAGNET->magnet(canvas)
            BopaviUiIconDrawable.Kind.SOUND->sound(canvas)
            else->{
                setup(Color.WHITE)
                canvas.drawCircle(50f,50f,20f,paint)
            }
        }
        canvas.restoreToCount(save)
    }
    override fun getIntrinsicWidth():Int=size
    override fun getIntrinsicHeight():Int=size
    override fun setAlpha(value:Int){alpha=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
