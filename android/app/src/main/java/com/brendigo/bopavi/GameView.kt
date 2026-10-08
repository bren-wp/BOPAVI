package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Native GPU-backed Android Canvas. No HTML, Chromium, WebView or network activity. */
class GameView(context: Context, val game: GameSimulation, private val reducedMotion: Boolean, private val skinIndex: Int, private val onFinished: (GameSimulation) -> Unit, private val onLevelCompleted:(Long)->Unit = {}, private val onFlap:()->Unit = {}, private val onCollect:()->Unit = {}) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val headerTypeface=Typeface.create("sans-serif-black",Typeface.BOLD)
    private val pickupHues=intArrayOf(0xffffc83b.toInt(),0xffffba83.toInt(),0xffa5efff.toInt(),0xffff9836.toInt(),0xfffff1ad.toInt(),0xffc5adff.toInt(),0xff89f7ef.toInt(),0xffc3a6ff.toInt())
    private val feather = intArrayOf(0xff39b5fc.toInt(),0xffffc73e.toInt(),0xffff6883.toInt(),0xff9e86f6.toInt(),0xff45daad.toInt(),0xff6676a8.toInt())
    private val skyA = intArrayOf(0xff159df7.toInt(),0xff18b5e7.toInt(),0xff418ddc.toInt(),0xff6e287e.toInt(),0xff45aaf6.toInt(),0xff131a4b.toInt(),0xff123969.toInt(),0xff0b123f.toInt())
    private val skyB = intArrayOf(0xffd0f8ff.toInt(),0xffffe3b2.toInt(),0xffedfbff.toInt(),0xffffa36d.toInt(),0xffffe9b6.toInt(),0xff7461bc.toInt(),0xff60f6d5.toInt(),0xff5955a9.toInt())
    private val pillars = intArrayOf(0xff20b96c.toInt(),0xfff5a65b.toInt(),0xff8ad8f5.toInt(),0xffe65b35.toInt(),0xffe9d9b5.toInt(),0xff57459a.toInt(),0xff5fdddc.toInt(),0xff7973f3.toInt())
    private val pillarDark = intArrayOf(0xff096c46.toInt(),0xffbd7153.toInt(),0xff4282ad.toInt(),0xff912f35.toInt(),0xff9d8d80.toInt(),0xff241b60.toInt(),0xff247b9b.toInt(),0xff373192.toInt())
    private val sky = LinearGradient(0f,0f,0f,800f,skyA[game.level.world],skyB[game.level.world],Shader.TileMode.CLAMP)
    private var lastFrame = 0L
    private var fpsTimestamp = 0L
    private var sent = false
    private var completedSeen=0
    private var pickupSeen=0
    var paused = false
        set(v) { field = v; lastFrame = 0L; if(!v) postInvalidateOnAnimation() }
    init { isClickable = true; importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES; contentDescription = "Dodirni za let Bopija" }
    private fun fill(color: Int) { p.shader=null; p.color=color; p.alpha=255; p.style=Paint.Style.FILL; p.strokeWidth=1f }
    private fun rect(c: Canvas,l:Float,t:Float,r:Float,b:Float,color:Int,round:Float=0f){fill(color);c.drawRoundRect(l,t,r,b,round,round,p)}
    private fun oval(c:Canvas,l:Float,t:Float,r:Float,b:Float,color:Int){fill(color);c.drawOval(l,t,r,b,p)}
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,center:Boolean=false){fill(color);p.textSize=size;p.typeface=headerTypeface;p.textAlign=if(center) Paint.Align.CENTER else Paint.Align.LEFT;c.drawText(s,x,y,p)}
    override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas)
        val now=System.nanoTime()
        val fps=if(reducedMotion) 30L else 60L
        if(fpsTimestamp != 0L && now-fpsTimestamp < 1_000_000_000L/fps-1_500_000L) { postInvalidateDelayed(5); return }
        fpsTimestamp=now
        if(!paused && !game.finished) {
            if(lastFrame!=0L) game.step(((now-lastFrame)/1_000_000_000.0).toFloat())
            lastFrame=now
            if(game.completionCount>completedSeen){completedSeen=game.completionCount;onLevelCompleted(game.completedOrdinal)}
            if(game.coins+game.stars>pickupSeen){pickupSeen=game.coins+game.stars;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);onCollect()}
        } else lastFrame=0L
        canvas.drawColor(skyA[game.level.world]); val scale=min(width/480f,height/800f)
        canvas.save(); canvas.translate((width-480f*scale)/2,(height-800f*scale)/2);canvas.scale(scale,scale)
        fill(Color.WHITE);p.shader=sky;canvas.drawRect(0f,0f,480f,800f,p);p.shader=null
        drawBackground(canvas)
        for(i in game.level.gates.indices) {
            val g=game.level.gates[i];val x=g.x-game.distance
            if(x < -100f || x>550f)continue
            drawGate(canvas,g,x,i)
        }
        drawBird(canvas)
        drawHud(canvas)
        if(!game.active && !game.finished) {
            rect(canvas,71f,565f,409f,638f,0xcc102654.toInt(),27f)
            text(canvas,"DODIRNI ZA LET",240f,613f,30f,Color.WHITE,true)
        }
        if(paused){rect(canvas,40f,340f,440f,458f,0xe91b2b55.toInt(),24f);text(canvas,"PAUZA",240f,409f,36f,Color.WHITE,true)}
        canvas.restore()
        if(game.finished && !sent){sent=true;post{if(isAttachedToWindow)onFinished(game)}}
        if(!paused && !game.finished && isAttachedToWindow) postInvalidateOnAnimation()
    }
    private fun drawBackground(c:Canvas){
        val w=game.level.world;val t=if(reducedMotion)0f else game.time
        val night=w==5||w==7
        // Parallax: three very cheap shape layers; no bitmaps allocated while drawing.
        for(layer in 0..2){
            val par=if(reducedMotion)0f else game.distance*(.08f+layer*.12f)
            for(i in -1..(if(reducedMotion)2 else 4)){
                val x=(i*154f+layer*67f-par%154f)
                val y=70f+layer*108f+(i and 1)*27f
                val a=if(night)0x667d9fcf else 0xccebfaff.toInt()
                oval(c,x,y,x+91f,y+28f,a);oval(c,x+19f,y-17f,x+67f,y+29f,a)
            }
        }
        val ridge=if(night)0xff22265e.toInt() else if(w==3)0xffa35455.toInt() else 0xff83bfbb.toInt()
        for(i in -1..5){
            val x=i*127f-game.distance*.055f%127f
            path.reset();path.moveTo(x,753f);path.lineTo(x+55f,619f);path.lineTo(x+108f,753f);path.close();fill(ridge);c.drawPath(path,p)
            if(w==2) { path.reset();path.moveTo(x+39f,650f);path.lineTo(x+55f,619f);path.lineTo(x+73f,650f);path.close();fill(Color.WHITE);c.drawPath(path,p) }
        }
        drawWorldDetails(c,w)
        rect(c,0f,751f,480f,800f,if(night)0xff171f53.toInt() else 0xff64c881.toInt())
        rect(c,0f,752f,480f,760f,if(night)0xff5d70ca.toInt() else 0xffa8e889.toInt())
        if(w==7)for(i in 0..19){val x=((i*83+17)%470).toFloat();val y=((i*113+31)%510).toFloat();oval(c,x,y,x+2f,y+2f,Color.WHITE)}
    }
    /** Separate low-cost silhouettes for all eight worlds; no bitmap allocations per frame. */
    private fun drawWorldDetails(c:Canvas,w:Int){
        val offset=if(reducedMotion)0f else game.distance*.035f
        val shift=offset%180f
        when(w){
            0 -> for(i in 0..3){val x=i*166f-shift;oval(c,x,500f,x+104f,535f,0xff739c89.toInt());rect(c,x+8f,493f,x+96f,505f,0xff6fe579.toInt(),12f)}
            1 -> {
                oval(c,365f,100f,432f,167f,0xffffec98.toInt())
                for(i in 0..4){val x=i*138f-shift;oval(c,x,675f,x+108f,689f,0x885ae7fa.toInt());oval(c,x+28f,697f,x+115f,708f,0x66ffffff)}
                rect(c,370f,550f,382f,721f,0xffac8555.toInt(),5f)
                for(j in 0..4){val yy=568f+j*3f;oval(c,331f+j*8f,yy-58f,397f+j*7f,yy+17f,0xff56be81.toInt())}
            }
            2 -> for(i in 0..8){val x=(i*79f+33f-shift*.6f+480f)%480f;val y=135f+i%4*111f;path.reset();path.moveTo(x-10f,y);path.lineTo(x+10f,y);path.moveTo(x,y-10f);path.lineTo(x,y+10f);fill(0xb5ffffff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=3f;c.drawPath(path,p);p.style=Paint.Style.FILL}
            3 -> for(i in 0..4){val x=i*137f-shift;path.reset();path.moveTo(x,690f);path.lineTo(x+51f,575f);path.lineTo(x+90f,690f);path.close();fill(0xff633047.toInt());c.drawPath(path,p);oval(c,x+38f,593f,x+63f,609f,0xffffb24b.toInt());rect(c,x+43f,620f,x+49f,699f,0xfff58135.toInt(),4f)}
            4 -> for(i in 0..3){val x=i*165f-shift;rect(c,x+38f,532f,x+62f,734f,0xffe2d5a3.toInt(),6f);rect(c,x+23f,522f,x+77f,542f,0xffffe3a7.toInt(),3f);rect(c,x+23f,713f,x+77f,733f,0xffffe3a7.toInt(),3f)}
            5 -> {oval(c,358f,103f,425f,170f,0xffffe1b2.toInt());oval(c,374f,90f,442f,159f,0xff131a4b.toInt());for(i in 0..12){val x=(i*119+37)%470f;val y=(i*77+128)%590f;oval(c,x,y,x+4f,y+4f,0xffffd89a.toInt())}}
            6 -> for(i in 0..5){val x=i*104f-shift*.8f;path.reset();path.moveTo(x+15f,733f);path.lineTo(x+40f,594f-i%2*25f);path.lineTo(x+69f,733f);path.close();fill(if(i%2==0)0xff5be4d7.toInt() else 0xffb68bee.toInt());c.drawPath(path,p);path.reset();path.moveTo(x+40f,594f-i%2*25f);path.lineTo(x+40f,720f);fill(0xaaffffff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=2f;c.drawPath(path,p);p.style=Paint.Style.FILL}
            7 -> {oval(c,324f,110f,453f,239f,0xffba79d9.toInt());oval(c,347f,122f,422f,168f,0x886cdfeb.toInt());fill(0x99f8e3ff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=7f;c.drawOval(303f,142f,473f,206f,p);p.style=Paint.Style.FILL;for(i in 0..11){val x=(i*103+31)%460f;val y=(i*173+61)%500f;oval(c,x,y,x+3f,y+3f,0xffffffff.toInt())}}
        }
    }
    private fun drawGate(c:Canvas,g:LevelEngine.Gate,x:Float,index:Int){
        val a=LevelEngine.opening(g,game.time)
        val w=g.width;val color=pillars[g.kind];val shade=pillarDark[g.kind]
        val top=a.top;val bottom=a.bottom
        rect(c,x+7f,0f,x+w-5f,top,shade,7f)
        rect(c,x,0f,x+w-13f,top,color,8f)
        rect(c,x-7f,top-28f,x+w+5f,top,color,7f)
        rect(c,x+7f,bottom,x+w-5f,755f,shade,7f)
        rect(c,x,bottom,x+w-13f,755f,color,7f)
        rect(c,x-7f,bottom,x+w+5f,bottom+27f,color,7f)
        val highlight=if(g.kind==3)0xffffcf7b.toInt() else 0x99ffffff.toInt()
        rect(c,x+6f,0f,x+13f,top-30f,highlight,3f)
        rect(c,x+6f,bottom+28f,x+13f,751f,highlight,3f)
        when(g.kind){
            0->{
                for(j in 0..2){val xx=x+j*17f;oval(c,xx,top-24f,xx+16f,top-14f,0xff4ee882.toInt())}
                for(j in 0..2){val xx=x+j*19f;oval(c,xx,bottom+6f,xx+12f,bottom+12f,0xff2b934d.toInt())}
            }
            2,6->{ for(j in 0..2){val yy=top-24+j*7f;oval(c,x+j*16f,yy,x+j*16f+13f,yy+7f,shade)} }
            1->{for(j in 0..2)oval(c,x+j*18f,bottom+6f,x+j*18f+12f,bottom+12f,0xffffe6b4.toInt())}
            3->{
                rect(c,x,bottom+7f,x+w,bottom+14f,0xffffd56d.toInt(),3f)
                for(j in 0..2){val xx=x+12f+j*18f;oval(c,xx,bottom+16f+j%2*5f,xx+6f,bottom+24f+j%2*5f,0xffffa047.toInt())}
            }
            4->{for(j in 0..2)rect(c,x+j*16f,top-19f,x+j*16f+7f,top-5f,0xfffffff0.toInt(),2f)}
            5->{
                rect(c,x+11f,top-16f,x+19f,top-5f,0xff9e83ef.toInt())
                for(j in 0..2){val yy=bottom+8f+j*11f;oval(c,x+14f,yy,x+22f,yy+8f,0xffb1a0ff.toInt())}
            }
            7->{
                for(j in 0..2)oval(c,x+j*17f,bottom+3f,x+j*17f+9f,bottom+12f,0xffc1a4ff.toInt())
                rect(c,x+14f,top-21f,x+25f,top-14f,0xffb2ecff.toInt(),3f)
            }
        }
        val middle=x+w*.5f;val center=(a.top+a.bottom)*.5f
        if(g.coin && game.coinVisible(index)) {
            val pulse=if(reducedMotion)0f else sin(game.time*5f+g.phase)*2f
            oval(c,middle-16f-pulse,center-16f-pulse,middle+16f+pulse,center+16f+pulse,pickupHues[g.kind])
            oval(c,middle-11f,center-11f,middle+11f,center+11f,0x66ffffff)
            text(c,LevelEngine.collectibleIcons[g.kind],middle,center+7f,18f,Color.WHITE,true)
        }
        if(g.star && game.starVisible(index)){text(c,LevelEngine.collectibleIcons[g.kind],middle+35f,center-25f,26f,0xffffe25d.toInt(),true)}
        if(g.power!=0 && game.powerVisible(index)){oval(c,middle+26f,center+26f,middle+52f,center+52f,0xff1a3c8b.toInt());text(c,if(g.power==1)"◆" else "↗",middle+39f,center+46f,19f,Color.WHITE,true)}
    }
    private fun drawBird(c:Canvas){
        if(game.collectPulse>0f){
            val portion=game.collectPulse/.36f
            fill(0xffffe69c.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=if(reducedMotion)2f else 3f
            p.alpha=(190f*portion).toInt().coerceIn(0,190)
            c.drawCircle(126f,game.y,28f+(1f-portion)*38f,p)
            p.style=Paint.Style.FILL;p.alpha=255
        }
        if(game.impactPulse>0f){
            val portion=game.impactPulse/.65f
            fill(0xffb8efff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=5f
            p.alpha=(210f*portion).toInt().coerceIn(0,210)
            c.drawCircle(126f,game.y,35f+(1f-portion)*28f,p)
            p.style=Paint.Style.FILL;p.alpha=255
        }
        if(!reducedMotion){
            for(i in 1..3){
                val ox=126f-i*19f-16f
                val oy=game.y+8f+sin(game.time*9f-i)*4f
                oval(c,ox,oy,ox+18f-i*3f,oy+8f-i*1.5f,0x5595eaff)
            }
        }
        if(game.magnetTime>0f)oval(c,92f,game.y-34f,160f,game.y+34f,0x444fdfff)
        if(game.shield>0){
            fill(0xff9fefff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=3f
            c.drawCircle(126f,game.y,34f+sin(game.time*5f)*2f,p)
            p.style=Paint.Style.FILL
        }
        c.save();c.translate(126f,game.y)
        c.rotate((game.velocity*.06f).coerceIn(-24f,48f))
        val phase=if(reducedMotion)0f else sin(game.time*19f)
        val squash=if(reducedMotion)1f else 1f+phase*.035f
        c.scale(1f,squash)
        // animated scarf tail and feathers
        path.reset();path.moveTo(-12f,11f);path.lineTo(-35f-3f*phase,22f);path.lineTo(-29f,5f);path.close();fill(0xffef385b.toInt());c.drawPath(path,p)
        oval(c,-30f,0f,-8f,16f,0xff1678d8.toInt())
        c.save();c.rotate(phase*26f,-13f,0f);oval(c,-28f,-4f,8f,16f,0xff0c78dc.toInt());oval(c,-25f,-6f,4f,5f,0xff4ac3ff.toInt());c.restore()
        oval(c,-23f,-24f,26f,25f,0xff095cc8.toInt())
        oval(c,-20f,-25f,23f,23f,feather[skinIndex.coerceIn(0,5)])
        oval(c,-13f,5f,19f,25f,Color.WHITE)
        oval(c,-14f,-17f,-2f,-10f,0x80ffffff.toInt())
        oval(c,18f,0f,25f,7f,0xffffa9ad.toInt())
        oval(c,-7f,-16f,7f,3f,Color.WHITE);oval(c,6f,-15f,20f,4f,Color.WHITE)
        val blink=if(!reducedMotion && (game.time%4.7f)>4.57f) 3f else 13f
        oval(c,-2f,-blink,5f,4f,0xff10224f.toInt());oval(c,10f,-blink,17f,4f,0xff10224f.toInt())
        path.reset();path.moveTo(11f,5f);path.lineTo(30f,9f);path.lineTo(13f,17f);path.close();fill(0xffffb321.toInt());c.drawPath(path,p)
        rect(c,-18f,7f,21f,14f,0xffff5b61.toInt(),3f)
        // bronze flying goggles with glints
        rect(c,-23f,-24f,22f,-17f,0xff7d451f.toInt(),3f)
        oval(c,-16f,-37f,5f,-16f,0xffa65d2b.toInt());oval(c,-12f,-34f,2f,-20f,0xff8cdeff.toInt())
        oval(c,3f,-36f,25f,-16f,0xffa65d2b.toInt());oval(c,7f,-32f,20f,-20f,0xff8cdeff.toInt())
        c.restore()
    }
    private fun drawHud(c:Canvas){
        rect(c,14f,22f,197f,71f,0xcc15285c.toInt(),20f)
        text(c,"LEVEL ${game.displayLevel}",26f,55f,20f,Color.WHITE)
        rect(c,210f,22f,350f,71f,0xcc15285c.toInt(),20f)
        text(c,"${LevelEngine.collectibleIcons[game.level.world]} ${game.stars+game.coins}",225f,54f,20f,0xffffe39c.toInt())
        if(game.shield>0)text(c,"ŠTIT ×${game.shield}",22f,104f,17f,Color.WHITE)
        if(game.magnetTime>0)text(c,"MAGNET",22f,127f,17f,Color.WHITE)
        text(c,"${LevelEngine.names[game.level.world]} · ${game.displayLevel}",240f,735f,18f,Color.WHITE,true)
    }
    override fun onTouchEvent(event:MotionEvent):Boolean {
        if(event.actionMasked==MotionEvent.ACTION_DOWN){if(!paused && !game.finished){game.flap();onFlap();performClick();invalidate()};return true}
        return true
    }
    override fun performClick():Boolean {super.performClick();return true}
    override fun onDetachedFromWindow(){paused=true;super.onDetachedFromWindow()}
}
