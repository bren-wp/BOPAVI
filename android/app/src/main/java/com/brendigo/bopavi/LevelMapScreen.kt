package com.brendigo.bopavi

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlin.math.min

/**
 * One genuine offline Android Level Map. The original owner's board is an art
 * target; no screenshot, fake badges or saved prices are baked into a bitmap.
 * Every node and world plaque remains an accessible native button.
 */
internal class LevelMapScreen(
    private val host:Activity,
    private val progress:ProgressStore
) {
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()

    private fun face(top:Int,bottom:Int,stroke:Int,round:Int=27):RippleDrawable {
        val plate=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top,bottom)).apply{
            cornerRadius=dp(round).toFloat()
            setStroke(dp(2),stroke)
        }
        return RippleDrawable(ColorStateList.valueOf(0x44ffffff),plate,null)
    }

    private fun nativeButton(label:String,description:String,
                             color:IntArray,enabled:Boolean=true,
                             onClick:()->Unit):Button = Button(host).apply{
        text=label
        contentDescription=description
        isAllCaps=false
        textSize=17f
        typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
        setTextColor(Color.WHITE)
        gravity=Gravity.CENTER
        setAutoSizeTextTypeUniformWithConfiguration(11,17,1,
            android.util.TypedValue.COMPLEX_UNIT_SP)
        background=face(color[0],color[1],color[2],25)
        minimumHeight=dp(48)
        elevation=dp(4).toFloat()
        isEnabled=enabled
        setOnClickListener{ if(isEnabled) onClick() }
    }

    private val blue=intArrayOf(0xff2bc7ff.toInt(),0xff0958c9.toInt(),0xffd3f6ff.toInt())
    private val gold=intArrayOf(0xffffe662.toInt(),0xffff8804.toInt(),0xfffffff1.toInt())
    private val green=intArrayOf(0xff7feb45.toInt(),0xff087a2a.toInt(),0xffbbff8f.toInt())
    private val gray=intArrayOf(0xff9babbe.toInt(),0xff425775.toInt(),0xffc6d2e1.toInt())
    private val volcano=intArrayOf(0xfffe8348.toInt(),0xff9a2b27.toInt(),0xffffcf89.toInt())
    private val wood=intArrayOf(0xffe0b576.toInt(),0xff82441e.toInt(),0xffffda9c.toInt())
    private val slate=intArrayOf(0xffa7d6ed.toInt(),0xff2b507a.toInt(),0xffd5f4ff.toInt())

    fun create(world:Int,onWorld:(Int)->Unit,
               onPlay:(Int,Long)->Unit,onBack:()->Unit):View {
        val selected=world.coerceIn(0,LevelEngine.WORLD_COUNT-1)
        val frontier=LevelMapGeometry.playableFrontier(progress.frontier(selected))
        val stage=LevelMapStage(host)
        val root=FrameLayout(host).apply {setBackgroundColor(0xff168be1.toInt())}

        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        stage.place(LevelMapLandscape(host,selected),0f,0f,941f,1672f)

        val title=TextView(host).apply{
            text="LEVEL MAP"
            textSize=17f
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity=Gravity.CENTER
            contentDescription="LEVEL MAP"
            background=GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xaa104d9b.toInt(),0xbb071d4d.toInt())).apply{
                cornerRadius=dp(18).toFloat()
            }
        }
        stage.place(title,342f,20f,125f,77f)
        stage.place(nativeButton("‹","Home",blue){onBack()},
            20f,16f,131f,95f,minTouch=true)
        stage.place(nativeButton("‹","Previous worlds",blue,
            LevelMapGeometry.pageStart(selected)>0){
            onWorld((LevelMapGeometry.pageStart(selected)-3).coerceAtLeast(0))
        },167f,27f,63f,65f,minTouch=true)
        stage.place(nativeButton("›","More worlds",blue,
            LevelMapGeometry.pageStart(selected)+3<LevelEngine.WORLD_COUNT){
            onWorld(LevelMapGeometry.pageStart(selected)+3)
        },248f,27f,63f,65f,minTouch=true)

        // The reference includes currency capsules, not a row-wide status
        // banner. Only genuinely earned coins and level-rating stars exist.
        val totalStars=(0 until LevelEngine.WORLD_COUNT)
            .sumOf{progress.worldStars(it)}
        fun statChip(number:Int,kind:String):TextView=TextView(host).apply{
            text="$kind  "+java.text.NumberFormat.getIntegerInstance().format(number)
            textSize=20f
            setTextColor(Color.WHITE)
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            gravity=Gravity.CENTER
            contentDescription=if(kind=="COINS")"Coins: $number" else "Earned stars: $number"
            background=face(0xff2879c4.toInt(),0xff071d4c.toInt(),
                0xff94ddff.toInt(),19)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        stage.place(statChip(progress.coins(),"COINS"),481f,23f,203f,73f)
        stage.place(statChip(totalStars,"STARS"),706f,23f,211f,73f)

        val captions=arrayOf("SKY MEADOWS","CRYSTAL RUINS","VOLCANO PEAKS",
            "NIGHT CASTLES","PEARL SEA","SKY TOWERS","MAGIC FOREST","STAR ABYSS")
        for((idx,id) in LevelMapGeometry.displayedWorlds(selected).withIndex()) {
            val pos=LevelMapGeometry.plaques[idx]
            val earned=progress.worldStars(id)
            val chosen=id==selected
            val colors=when(idx) {
                0->wood
                1->slate
                else->volcano
            }
            val button=nativeButton(
                captions[id]+"\n★ $earned/${LevelMapGeometry.starsPerWorld}",
                "Select world ${id+1}, ${captions[id]}, $earned of 60 stars",
                colors){onWorld(id)}
            button.textSize=if(chosen)20f else 17f
            button.isSelected=chosen
            stage.place(button,pos.x,pos.y,if(idx==0)342f else 286f,109f,
                minTouch=true)
        }
        // The mascot on the first reference map is decorative; unlike map
        // nodes, it neither grants stars nor pretends to be a clickable control.
        if(selected==0)stage.place(ImageView(host).apply{
            setImageResource(R.drawable.bopiportrait0)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },223f,937f,222f,209f)

        val visible=LevelMapGeometry.nearestLevels(frontier)
        for(index in visible.indices){
            val level=visible[index]
            val pos=LevelMapGeometry.nodes[index]
            val unlocked=LevelMapGeometry.isUnlocked(level,frontier)
            val earned=progress.bestLevelStars(selected,level.toLong())
            val node=nativeButton(
                if(unlocked)"$level" else "",
                if(unlocked)"Level $level, $earned stars. Tap to play"
                else "Locked level $level",
                if(!unlocked)gray else if(level==frontier)gold else blue,
                unlocked){if(unlocked)onPlay(selected,level.toLong())}
            // Actual embossed circular stage plates instead of rectangular
            // Android buttons; content descriptions retain true save state.
            val plate=GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                if(!unlocked)intArrayOf(0xffb5c0cc.toInt(),0xff4f637e.toInt())
                else if(level==frontier)
                    intArrayOf(0xfffff488.toInt(),0xffffa600.toInt())
                else intArrayOf(0xff66e0ff.toInt(),0xff0068d7.toInt())
            ).apply {
                shape=GradientDrawable.OVAL
                setStroke(dp(4),if(!unlocked)0xff42516c.toInt()
                    else if(level==frontier)0xffefac15.toInt()
                    else 0xffbdfbff.toInt())
            }
            node.background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                plate,null)
            node.textSize=if(unlocked)28f else 24f
            stage.place(node,pos.x,pos.y,
                LevelMapGeometry.nodeWidth,LevelMapGeometry.nodeHeight,minTouch=true)
            if(!unlocked) {
                // A decorative, non-focusable vector icon; locked button has
                // isEnabled=false, and must not launch a hidden stage.
                stage.place(LevelMapLockGlyph(host),pos.x+34f,pos.y+30f,40f,46f)
            } else if(index<3){
                // Three outlined stars are visible even on a new install.
                // Filling them is tied only to saved bestLevelStars.
                val strip=LevelMapGeometry.starStrip(index)
                stage.place(LevelMapStarStrip(host,earned.coerceIn(0,3)),
                    strip.x,strip.y,156f,42f)
            }
        }

        val chosenStars=progress.bestLevelStars(selected,frontier.toLong())
        stage.place(nativeButton("▶  PLAY LEVEL",
            "PLAY LEVEL $frontier, $chosenStars STARS",
            gold){onPlay(selected,frontier.toLong())}.apply{
                textSize=27f
                setAutoSizeTextTypeUniformWithConfiguration(18,27,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                setTextColor(0xff5a2800.toInt())
            },138f,1449f,666f,122f,minTouch=true)

        // Bars are handled on the native viewport only. The illustrated
        // islands continue drawing edge-to-edge behind notches/navigation.
        stage.setOnApplyWindowInsetsListener { _,insets ->
            if(Build.VERSION.SDK_INT>=30){
                val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                stage.setSafe(safe.left,safe.top,safe.right,safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                stage.setSafe(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
            }
            insets
        }
        stage.requestApplyInsets()
        return root
    }
}

/** Uniform reference camera for native taps and the painted landscape. */
internal class LevelMapStage(context:Context):ViewGroup(context){
    private data class Slot(val view:View,val bounds:RectF,val full:Boolean,val minTouch:Boolean)
    private val slots=ArrayList<Slot>()
    private var safeLeft=0
    private var safeTop=0
    private var safeRight=0
    private var safeBottom=0

    fun place(view:View,x:Float,y:Float,w:Float,h:Float,
              full:Boolean=false,minTouch:Boolean=false){
        slots.add(Slot(view,RectF(x,y,x+w,y+h),full,minTouch))
        addView(view,ViewGroup.LayoutParams(-2,-2))
    }
    fun setSafe(left:Int,top:Int,right:Int,bottom:Int) {
        if(left!=safeLeft || top!=safeTop || right!=safeRight || bottom!=safeBottom){
            safeLeft=left;safeTop=top;safeRight=right;safeBottom=bottom
            requestLayout()
        }
    }
    private fun transform(width:Int,height:Int):Triple<Float,Float,Float>{
        val availableW=(width-safeLeft-safeRight).coerceAtLeast(1).toFloat()
        val availableH=(height-safeTop-safeBottom).coerceAtLeast(1).toFloat()
        val scale=min(availableW/LevelMapGeometry.width,
            availableH/LevelMapGeometry.height)
        return Triple(scale,safeLeft+(availableW-LevelMapGeometry.width*scale)/2f,
            safeTop+(availableH-LevelMapGeometry.height*scale)/2f)
    }
    override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int){
        val w=View.MeasureSpec.getSize(widthMeasureSpec)
        val h=View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w,h)
        val (scale,_,_)=transform(w,h)
        val minimum=(48f*resources.displayMetrics.density).toInt()
        for(slot in slots){
            val cw=if(slot.full)w else maxOf((slot.bounds.width()*scale).toInt(),
                if(slot.minTouch)minimum else 1)
            val ch=if(slot.full)h else maxOf((slot.bounds.height()*scale).toInt(),
                if(slot.minTouch)minimum else 1)
            slot.view.measure(View.MeasureSpec.makeMeasureSpec(cw,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(ch,View.MeasureSpec.EXACTLY))
        }
    }
    override fun onLayout(changed:Boolean,left:Int,top:Int,right:Int,bottom:Int){
        val w=right-left
        val h=bottom-top
        val (scale,ox,oy)=transform(w,h)
        for(slot in slots){
            val child=slot.view
            val x=if(slot.full)0 else (ox+slot.bounds.left*scale).toInt()
            val y=if(slot.full)0 else (oy+slot.bounds.top*scale).toInt()
            child.layout(x,y,x+child.measuredWidth,y+child.measuredHeight)
        }
    }
}

/**
 * Three locally painted islands and a continuous navigable bridge; background
 * uses the same preinstalled fantasy landscape as Home. No network or bitmaps
 * are generated during a frame. All hit targets are sibling Android Views.
 */
internal class LevelMapLandscape(context:Context,private val selected:Int):View(context){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    private fun color(value:Int){p.reset();p.isAntiAlias=true;p.color=value}
    private fun island(c:Canvas,cx:Float,y:Float,rx:Float,deep:Float,
                       top:Int,rock:Int,highlight:Int){
        color(0x55032048)
        c.drawOval(cx-rx-9f,y+deep-12f,cx+rx+9f,y+deep+21f,p)
        path.reset()
        path.moveTo(cx-rx,y+7f)
        path.lineTo(cx+rx,y+7f)
        path.lineTo(cx+rx*.61f,y+deep*.69f)
        path.lineTo(cx,y+deep)
        path.lineTo(cx-rx*.63f,y+deep*.70f)
        path.close()
        color(rock);c.drawPath(path,p)
        color(0x66442823);p.style=Paint.Style.STROKE;p.strokeWidth=7f
        c.drawPath(path,p);p.style=Paint.Style.FILL
        color(top);c.drawOval(cx-rx,y-37f,cx+rx,y+43f,p)
        color(highlight);c.drawOval(cx-rx*.82f,y-34f,cx+rx*.82f,y+12f,p)
        // Three stepped waterfall ribbons with matching island shadows.
        color(0xff80eaff.toInt())
        for(i in 0..2)c.drawRoundRect(cx+rx*.34f+i*11f,y+27f,
            cx+rx*.34f+i*11f+7f,y+deep*.74f,4f,4f,p)
        color(0xffd8fcff.toInt())
        for(i in 0..1)c.drawRoundRect(cx+rx*.34f+i*15f+3f,y+31f,
            cx+rx*.34f+i*15f+5f,y+deep*.70f,2f,2f,p)
    }
    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)
        val checkpoint=canvas.save()
        canvas.scale(width/LevelMapGeometry.width,height/LevelMapGeometry.height)
        // Atmospheric distance: no rectangular screenshot overlay.
        color(0x55ffffff)
        canvas.drawOval(35f,220f,950f,1110f,p)
        island(canvas,709f,239f,231f,222f,
            0xffe55b3d.toInt(),0xff664454.toInt(),0xfffa9b54.toInt())
        island(canvas,621f,690f,264f,252f,
            0xff288fe2.toInt(),0xff646b96.toInt(),0xff8de6ff.toInt())
        island(canvas,347f,1140f,352f,290f,
            0xff3eae59.toInt(),0xff8b7269.toInt(),0xffa6ef75.toInt())
        // A ring on the actual selected world, not a false unlock indicator.
        val focus=selected%3
        val focusX=floatArrayOf(347f,621f,709f)[focus]
        val focusY=floatArrayOf(1140f,690f,239f)[focus]
        color(0xaaeaffff.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=6f
        canvas.drawOval(focusX-110f,focusY-50f,focusX+110f,focusY+34f,p)
        p.style=Paint.Style.FILL
        // Crystal spires are local vector shapes, not reward hitboxes.
        for(i in 0..6){
            val x=460f+i*63f
            val y=540f+(i%3)*17f
            path.reset()
            path.moveTo(x-13f,y+58f)
            path.lineTo(x,y-32f-(i%2)*25f)
            path.lineTo(x+17f,y+58f)
            path.close()
            color(if(i%2==0)0xff9b66ff.toInt() else 0xff30e4ff.toInt())
            canvas.drawPath(path,p)
        }
        // Lava fissures stay on the distant upper world.
        color(0xffffad31.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=11f
        path.reset();path.moveTo(552f,230f)
        path.cubicTo(624f,273f,640f,223f,710f,270f)
        path.cubicTo(750f,289f,776f,247f,824f,263f)
        canvas.drawPath(path,p);p.style=Paint.Style.FILL
        // A gently curving rope path through the six real level markers.
        val pts=LevelMapGeometry.nodes
        color(0xff694630.toInt());p.style=Paint.Style.STROKE
        p.strokeWidth=35f;p.strokeCap=Paint.Cap.ROUND
        path.reset()
        path.moveTo(pts[0].x+52f,pts[0].y+53f)
        for(i in 1 until pts.size){
            val from=pts[i-1];val to=pts[i]
            path.quadTo((from.x+to.x)/2f+54f,(from.y+to.y)/2f,
                to.x+54f,to.y+52f)
        }
        canvas.drawPath(path,p)
        color(0xffffdeb2.toInt());p.strokeWidth=21f;canvas.drawPath(path,p)
        p.style=Paint.Style.FILL;p.strokeCap=Paint.Cap.BUTT
        // Tiny floral highlights around Sky Meadows, excluding controls.
        for(i in 0..21){
            val x=80f+(i*109%665)
            val y=916f+(i*61%469)
            color(0xfffcfcff.toInt())
            canvas.drawCircle(x-5f,y,5f,p)
            canvas.drawCircle(x+5f,y,5f,p)
            canvas.drawCircle(x,y-5f,5f,p)
            canvas.drawCircle(x,y+5f,5f,p)
            color(0xffffc83e.toInt());canvas.drawCircle(x,y,3f,p)
        }
        canvas.restoreToCount(checkpoint)
    }
}
