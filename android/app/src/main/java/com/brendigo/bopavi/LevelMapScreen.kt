package com.brendigo.bopavi

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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
        // Native button / currency chip retains its contentDescription and
        // enabled state while receiving the same layered bevel as Home.
        return Bopavi3DTheme.custom(top,bottom,stroke)
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
    private val gray=intArrayOf(0xff9babbe.toInt(),0xff425775.toInt(),0xffc6d2e1.toInt())
    private val volcano=intArrayOf(0xfffe8348.toInt(),0xff9a2b27.toInt(),0xffffcf89.toInt())
    private val wood=intArrayOf(0xffe0b576.toInt(),0xff82441e.toInt(),0xffffda9c.toInt())
    private val slate=intArrayOf(0xffa7d6ed.toInt(),0xff2b507a.toInt(),0xffd5f4ff.toInt())

    /**
     * Native scroll alternative for compact, landscape and large-text devices.
     * The 941x1672 reference camera would otherwise create overlapping
     * minimum-48dp level hit targets. Preserve all real world/level callbacks.
     */
    private fun compactMap(selected:Int,frontier:Int,
                           onWorld:(Int)->Unit,
                           onPlay:(Int,Long)->Unit,onBack:()->Unit):View {
        val root=FrameLayout(host).apply{setBackgroundColor(0xff168be1.toInt())}
        root.addView(ImageView(host).apply {
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val scroll=ScrollView(host).apply {
            isFillViewport=true
            clipToPadding=false
            setOnApplyWindowInsetsListener{_,insets->
                if(Build.VERSION.SDK_INT>=30){
                    val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    setPadding(safe.left,safe.top,safe.right,safe.bottom)
                }else{
                    @Suppress("DEPRECATION")
                    setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                }
                insets
            }
        }
        val column=LinearLayout(host).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(12),dp(12),dp(12),dp(24))
        }
        fun addControl(view:View,height:Int=64){
            column.addView(view,LinearLayout.LayoutParams(-1,dp(height)).apply{
                bottomMargin=dp(9)
            })
        }
        addControl(nativeButton("‹ HOME","Home",blue){onBack()},55)
        addControl(TextView(host).apply {
            text="LEVEL MAP"
            textSize=29f
            gravity=Gravity.CENTER
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(Color.WHITE)
            contentDescription="LEVEL MAP"
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
        },69)
        val coins=progress.coins()
        val stars=(0 until LevelEngine.WORLD_COUNT).sumOf{
            progress.worldStars(it)
        }
        addControl(TextView(host).apply {
            text="COINS  "+coins+"    ★  "+stars
            textSize=20f
            gravity=Gravity.CENTER
            setTextColor(Color.WHITE)
            contentDescription="Coins: "+coins+". Earned stars: "+stars
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
        },54)
        val labels=arrayOf("SKY MEADOWS","CRYSTAL RUINS","VOLCANO PEAKS",
            "NIGHT CASTLES","PEARL SEA","SKY TOWERS","MAGIC FOREST","STAR ABYSS")
        for(id in LevelMapGeometry.displayedWorlds(selected)){
            val earned=progress.worldStars(id)
            val colors=when(id%3){0->wood;1->slate;else->volcano}
            addControl(nativeButton(labels[id]+"  ★ "+earned+"/60",
                "Select world "+(id+1)+", "+labels[id]+", "+earned+" of 60 stars",
                colors){onWorld(id)})
        }
        val pages=LinearLayout(host).apply {
            orientation=LinearLayout.HORIZONTAL
        }
        val previous=LevelMapGeometry.pageStart(selected)>0
        val next=LevelMapGeometry.pageStart(selected)+3<LevelEngine.WORLD_COUNT
        pages.addView(nativeButton("‹ PREVIOUS","Previous worlds",blue,previous){
            onWorld((LevelMapGeometry.pageStart(selected)-3).coerceAtLeast(0))
        },LinearLayout.LayoutParams(0,dp(58),1f))
        pages.addView(nativeButton("MORE ›","More worlds",blue,next){
            onWorld(LevelMapGeometry.pageStart(selected)+3)
        },LinearLayout.LayoutParams(0,dp(58),1f))
        addControl(pages,58)
        for(level in LevelMapGeometry.nearestLevels(frontier)){
            val enabled=LevelMapGeometry.isUnlocked(level,frontier)
            val earned=progress.bestLevelStars(selected,level.toLong())
            val description=if(enabled) "Level "+level+", "+earned+" stars. Tap to play"
                else "Locked level "+level
            addControl(nativeButton(
                if(enabled)"LEVEL "+level+"  ★ "+earned else "LOCKED LEVEL "+level,
                description,
                if(!enabled)gray else if(level==frontier)gold else blue,
                enabled){
                if(enabled)onPlay(selected,level.toLong())
            })
        }
        val earned=progress.bestLevelStars(selected,frontier.toLong())
        addControl(nativeButton("▶ PLAY LEVEL "+frontier,
            "PLAY LEVEL "+frontier+", "+earned+" STARS",gold){
            onPlay(selected,frontier.toLong())
        },77)
        scroll.addView(column)
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        scroll.requestApplyInsets()
        return root
    }

    fun create(world:Int,onWorld:(Int)->Unit,
               onPlay:(Int,Long)->Unit,onBack:()->Unit):View {
        val selected=world.coerceIn(0,LevelEngine.WORLD_COUNT-1)
        val frontier=LevelMapGeometry.playableFrontier(progress.frontier(selected))
        if(!LevelMapGeometry.supports(
                host.resources.displayMetrics.widthPixels,
                host.resources.displayMetrics.heightPixels,
                host.resources.configuration.orientation==
                    Configuration.ORIENTATION_PORTRAIT,
                host.resources.configuration.fontScale)){
            return compactMap(selected,frontier,onWorld,onPlay,onBack)
        }
        val stage=LevelMapStage(host)
        val root=FrameLayout(host).apply {
            setBackgroundColor(0xff168be1.toInt())
            contentDescription="LEVEL MAP"
        }

        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        stage.place(LevelMapLandscape(host,selected),0f,0f,941f,1672f)

        // Owner's map has a single primary Back action. Keep page-changing
        // controls available, but move them away from the currency header and
        // volcano sign so the top 110px remains reference-aligned.
        stage.place(nativeButton("‹","Home",blue){onBack()}.apply {
            text=""
        },20f,16f,122f,95f,minTouch=true)
        // Decorative feather-edged arrow cannot consume native Back taps.
        stage.place(ReferenceArrowOverlayView(host),20f,16f,122f,95f)
        // The native root exposes the accessible page title; reference sky
        // stays clear of an extra painted heading between currency capsules.
        if(LevelMapGeometry.pageStart(selected)>0) {
            stage.place(nativeButton("‹","Previous worlds",blue){
                onWorld((LevelMapGeometry.pageStart(selected)-3).coerceAtLeast(0))
            },19f,1353f,66f,77f,minTouch=true)
        }
        if(LevelMapGeometry.pageStart(selected)+3<LevelEngine.WORLD_COUNT) {
            stage.place(nativeButton("›","More worlds",blue){
                onWorld(LevelMapGeometry.pageStart(selected)+3)
            },855f,1353f,66f,77f,minTouch=true)
        }
                // The reference includes currency capsules, not a row-wide status
        // banner. Only genuinely earned coins and level-rating stars exist.
        val totalStars=(0 until LevelEngine.WORLD_COUNT)
            .sumOf{progress.worldStars(it)}
        fun statChip(number:Int,kind:String):TextView=TextView(host).apply{
            text=java.text.NumberFormat.getIntegerInstance().format(number)
            setPadding(dp(20),0,dp(3),0)
            textSize=20f
            setTextColor(Color.WHITE)
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            gravity=Gravity.CENTER
            maxLines=1
            includeFontPadding=false
            setAutoSizeTextTypeUniformWithConfiguration(11,20,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            contentDescription=if(kind=="COINS")"Coins: $number" else "Earned stars: $number"
            background=face(0xff2879c4.toInt(),0xff071d4c.toInt(),
                0xff94ddff.toInt(),19)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        stage.place(statChip(progress.coins(),"COINS"),481f,23f,203f,73f)
        stage.place(ImageView(host).apply{
            setImageDrawable(HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.COIN,dp(32)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },477f,26f,57f,63f)
        stage.place(statChip(totalStars,"STARS"),706f,23f,211f,73f)
        stage.place(ImageView(host).apply{
            // Real earned STAR ratings must never be illustrated as
            // spendable pink gems; this game has no gem wallet.
            setImageDrawable(HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.STAR,dp(32)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },700f,25f,66f,64f)

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
            button.textSize=if(chosen)18f else 15f
            button.maxLines=3
            button.includeFontPadding=false
            button.setPadding(dp(3),0,dp(3),0)
            button.setAutoSizeTextTypeUniformWithConfiguration(11,18,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            button.isSelected=chosen
            // Chiseled wood, cyan crystal and glowing volcanic stone follow
            // the three owner reference sign materials. The real native
            // Button still owns the selected-world click and earned stars.
            button.background=RippleDrawable(
                ColorStateList.valueOf(0x44ffffff),
                LevelMapPlaqueDrawable(idx),null)
            button.setTextColor(if(idx==0)0xfffff2b5.toInt() else Color.WHITE)
            button.setShadowLayer(2f,0f,2f,0xff362014.toInt())
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
            // Three independent nested enamel layers reproduce the rounded
            // extrusion visible on the reference stage markers. All insets
            // are logical design pixels (not dp) on the 941×1672 camera.
            val rimColor=if(!unlocked)0xff43526c.toInt()
                else if(level==frontier)0xffb8640e.toInt()
                else 0xff073a8a.toInt()
            val sidewall=GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(if(!unlocked)0xff67788f.toInt() else rimColor,
                    0xff142752.toInt())).apply {
                shape=GradientDrawable.OVAL
                setStroke(3,0xff0a275a.toInt())
            }
            val bezel=GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(if(!unlocked)0xffe2e9f2.toInt()
                    else if(level==frontier)0xfffff3ac.toInt()
                    else 0xffd4fbff.toInt(),
                    if(!unlocked)0xff8394aa.toInt()
                    else if(level==frontier)0xffffb827.toInt()
                    else 0xff2589e8.toInt())).apply {
                shape=GradientDrawable.OVAL
                setStroke(2,rimColor)
            }
            val insetFace=GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                if(!unlocked)intArrayOf(0xffccd5e2.toInt(),0xff617993.toInt())
                else if(level==frontier)
                    intArrayOf(0xfffff89e.toInt(),0xffffbd23.toInt(),0xffdf7611.toInt())
                else intArrayOf(0xff9aecff.toInt(),0xff168fe7.toInt(),0xff0654b8.toInt())
            ).apply {
                shape=GradientDrawable.OVAL
                setStroke(2,if(!unlocked)0xfff6f8fb.toInt()
                    else if(level==frontier)0xfffff7c4.toInt()
                    else 0xffdffeff.toInt())
            }
            val plate=LayerDrawable(arrayOf(sidewall,bezel,insetFace)).apply {
                setLayerInset(1,3,1,3,8)
                setLayerInset(2,9,6,9,15)
            }
            node.background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                plate,null)
            node.textSize=if(unlocked)28f else 24f
            node.setAutoSizeTextTypeUniformWithConfiguration(24,34,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            stage.place(node,pos.x,pos.y,
                LevelMapGeometry.nodeWidth,LevelMapGeometry.nodeHeight,minTouch=true)
            if(!unlocked) {
                // A decorative, non-focusable vector icon; locked button has
                // isEnabled=false, and must not launch a hidden stage.
                stage.place(LevelMapLockGlyph(host),pos.x+34f,pos.y+30f,40f,46f)
            }
            if(index<3){
                // Draw three real-rating placeholders even for a locked
                // stage; zero earned stars remain outlined, never golden.
                // Filling them is tied only to saved bestLevelStars.
                val strip=LevelMapGeometry.starStrip(index)
                stage.place(LevelMapStarStrip(host,earned.coerceIn(0,3)),
                    strip.x,strip.y,156f,42f)
            }
        }

        val chosenStars=progress.bestLevelStars(selected,frontier.toLong())
        // Vector feather wings sit BEHIND the real, enabled gold CTA. They
        // cannot consume touch events or pretend to launch a locked level.
        stage.place(LevelMapPlayWings(host),98f,1440f,745f,141f)
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
    private data class Slot(val view:View,val bounds:RectF,val full:Boolean,
                            val minTouch:Boolean,val referenceAligned:Boolean,
                            val verticalAnchor:Float)
    private val slots=ArrayList<Slot>()
    // Opt-in for native portrait reference scenes (Shop, Skins, Settings):
    // art fills the original 941x1672 camera; actual tappable controls
    // still fall back to safe-area positioning around physical cutouts.
    private var shopFullBleed=false
    fun useShopFullBleedReferenceCamera() {
        shopFullBleed=true
        requestLayout()
    }
    private var safeLeft=0
    private var safeTop=0
    private var safeRight=0
    private var safeBottom=0

    /**
     * Decorative art is allowed to use the exact full owner reference grid.
     * A native touch target may do so only when its entire reference rectangle
     * fits within the real device's safe window insets. Otherwise it falls
     * back to the previously existing safe-area transform.
     */
    fun place(view:View,x:Float,y:Float,w:Float,h:Float,
              full:Boolean=false,minTouch:Boolean=false,
              referenceAligned:Boolean=false,verticalAnchor:Float=.5f){
        // Default center anchoring leaves every non-Home map unchanged.
        slots.add(Slot(view,RectF(x,y,x+w,y+h),full,minTouch,
            referenceAligned,verticalAnchor.coerceIn(0f,1f)))
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
    private fun referenceTransform(width:Int,height:Int):Triple<Float,Float,Float>{
        val scale=min(width/LevelMapGeometry.width,
            height/LevelMapGeometry.height)
        return Triple(scale,(width-LevelMapGeometry.width*scale)/2f,
            (height-LevelMapGeometry.height*scale)/2f)
    }
    private fun placement(slot:Slot,width:Int,height:Int,
                          safe:Triple<Float,Float,Float>,
                          reference:Triple<Float,Float,Float>
    ):Triple<Float,Float,Float>{
        // Keep the illustrated Shop kiosk, its Bopi art, the featured
        // panel and six product cards at the owner's original coordinates.
        // Only actionable native controls fall back into safe insets when
        // an actual display cutout would make the target unreachable.
        if(!slot.referenceAligned && !shopFullBleed)return safe
        val extraHeight=(height-LevelMapGeometry.height*reference.first)
            .coerceAtLeast(0f)
        val aligned=Triple(reference.first,reference.second,
            extraHeight*slot.verticalAnchor)
        if(!slot.minTouch)return aligned
        val scale=aligned.first
        val x=aligned.second
        val y=aligned.third
        val b=slot.bounds
        val inside=x+b.left*scale>=safeLeft &&
            x+b.right*scale<=width-safeRight &&
            y+b.top*scale>=safeTop &&
            y+b.bottom*scale<=height-safeBottom
        return if(inside)aligned else safe
    }
    override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int){
        val w=View.MeasureSpec.getSize(widthMeasureSpec)
        val h=View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w,h)
        val safe=transform(w,h)
        val reference=referenceTransform(w,h)
        val minimum=(48f*resources.displayMetrics.density).toInt()
        for(slot in slots){
            val (scale,_,_)=placement(slot,w,h,safe,reference)
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
        val safe=transform(w,h)
        val reference=referenceTransform(w,h)
        for(slot in slots){
            val child=slot.view
            val (scale,ox,oy)=placement(slot,w,h,safe,reference)
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
        // The owner map islands are irregular multi-plane cliffs, not
        // mathematically perfect triangular pyramids. All facets are passive
        // scenery and remain behind native level controls.
        path.moveTo(cx-rx,y+7f)
        path.lineTo(cx-rx*.88f,y+deep*.27f)
        path.lineTo(cx-rx*.65f,y+deep*.64f)
        path.lineTo(cx-rx*.34f,y+deep*.88f)
        path.lineTo(cx-rx*.06f,y+deep)
        path.lineTo(cx+rx*.27f,y+deep*.88f)
        path.lineTo(cx+rx*.64f,y+deep*.67f)
        path.lineTo(cx+rx*.87f,y+deep*.28f)
        path.lineTo(cx+rx,y+7f)
        path.close()
        color(rock);c.drawPath(path,p)
        color(0x66442823);p.style=Paint.Style.STROKE;p.strokeWidth=7f
        c.drawPath(path,p);p.style=Paint.Style.FILL
        // Raised bright turf lip with a darker, cut-rock overhang.
        color(0xff284e43.toInt())
        c.drawOval(cx-rx-4f,y-27f,cx+rx+4f,y+52f,p)
        color(top);c.drawOval(cx-rx,y-37f,cx+rx,y+43f,p)
        color(highlight);c.drawOval(cx-rx*.82f,y-34f,cx+rx*.82f,y+12f,p)
        color(0x99ffffff.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=4f
        c.drawArc(cx-rx*.88f,y-34f,cx+rx*.84f,y+21f,189f,157f,false,p)
        p.style=Paint.Style.FILL
        // Three stepped waterfall ribbons with matching island shadows.
        color(0xff80eaff.toInt())
        for(i in 0..2)c.drawRoundRect(cx+rx*.34f+i*11f,y+27f,
            cx+rx*.34f+i*11f+7f,y+deep*.74f,4f,4f,p)
        color(0xffd8fcff.toInt())
        for(i in 0..1)c.drawRoundRect(cx+rx*.34f+i*15f+3f,y+31f,
            cx+rx*.34f+i*15f+5f,y+deep*.70f,2f,2f,p)
        // Fine blue-white spray highlights overlap the naturally running
        // waterfall only. These are passive map decorations, not pickups.
        color(0x66dfffff)
        c.drawOval(cx+rx*.28f,y+deep*.62f,
            cx+rx*.61f,y+deep*.79f,p)
    }

    /** Deterministic layered rock facets give floating land a cut-stone edge.
     * All shapes remain below native controls and do not represent game items.
     */
    private fun cliffFacets(c:Canvas,cx:Float,y:Float,rx:Float,deep:Float){
        for(i in 0..8){
            val k=i/8f
            val x=cx-rx*.70f+k*rx*1.4f
            val bottomY=y+deep*(.65f+(.10f*(i%3)))
            path.reset()
            path.moveTo(x-19f,y+28f)
            path.lineTo(x+23f,y+30f)
            path.lineTo(x+17f,bottomY)
            path.lineTo(x-14f,bottomY-14f)
            path.close()
            color(if(i%2==0)0x55472f4b else 0x557da5a5)
            c.drawPath(path,p)
            color(0x668df8ff)
            p.strokeWidth=2.8f
            p.style=Paint.Style.STROKE
            c.drawLine(x-16f,y+35f,x-11f,bottomY-20f,p)
            p.style=Paint.Style.FILL
        }
    }

    /** Passive ivory towers and orange roofs behind the live Meadow nodes. */
    private fun meadowCastle(c:Canvas){
        for(i in 0..2){
            val x=302f+i*57f
            val high=if(i==1)153f else if(i==0)116f else 133f
            val y=1070f-high
            color(0xff795c57.toInt())
            c.drawRoundRect(x,y+5f,x+48f,1072f,5f,5f,p)
            color(0xfffff1d1.toInt())
            c.drawRoundRect(x+4f,y+4f,x+44f,1069f,3f,3f,p)
            color(0xffd6c3b4.toInt())
            c.drawRect(x+35f,y+7f,x+43f,1067f,p)
            for(w in 0..1){
                val wy=y+23f+w*37f
                color(0xff6b6281.toInt())
                c.drawRoundRect(x+17f,wy,x+31f,wy+24f,6f,6f,p)
                color(0xff84d5ec.toInt())
                c.drawRoundRect(x+19f,wy+3f,x+29f,wy+19f,4f,4f,p)
            }
            path.reset()
            path.moveTo(x-8f,y+7f)
            path.lineTo(x+23f,y-43f)
            path.lineTo(x+55f,y+7f)
            path.close()
            color(0xff842c36.toInt());c.drawPath(path,p)
            path.reset()
            path.moveTo(x-3f,y+4f)
            path.lineTo(x+23f,y-39f)
            path.lineTo(x+48f,y+4f)
            path.close()
            color(0xffff8b34.toInt());c.drawPath(path,p)
            color(0xffffd477.toInt())
            c.drawRect(x+20f,y-50f,x+23f,y-36f,p)
            path.reset()
            path.moveTo(x+23f,y-50f)
            path.lineTo(x+44f,y-46f)
            path.lineTo(x+23f,y-40f)
            path.close()
            color(0xffff6949.toInt());c.drawPath(path,p)
        }
    }

    /** Local vector foliage on the actual Sky Meadows world. */
    private fun meadowCanopy(c:Canvas){
        for(i in 0..11){
            val x=115f+(i*73%484)
            val y=1060f+(i%4)*21f
            color(0xff765034.toInt())
            c.drawRoundRect(x-5f,y-5f,x+5f,y+35f,3f,3f,p)
            color(0xff126d42.toInt())
            c.drawOval(x-27f,y-28f,x+27f,y+12f,p)
            color(0xff51c963.toInt())
            c.drawOval(x-22f,y-35f,x+17f,y+1f,p)
            color(0xffbaff8a.toInt())
            c.drawOval(x-10f,y-34f,x+8f,y-21f,p)
        }
    }

    /** Faceted crystals echo the original floating ruins rather than
     * placing collectible reward sprites outside the real level runtime.
     */
    private fun crystalCluster(c:Canvas){
        for(i in 0..8){
            val x=435f+i*48f
            val y=589f+(i%3)*24f
            val height=74f+(i*31%83)
            path.reset()
            path.moveTo(x-19f,y+34f)
            path.lineTo(x,y-height)
            path.lineTo(x+21f,y+34f)
            path.close()
            color(if(i%2==0)0xff7e37d4.toInt() else 0xff1874cf.toInt())
            c.drawPath(path,p)
            path.reset()
            path.moveTo(x,y-height)
            path.lineTo(x+21f,y+34f)
            path.lineTo(x,y+21f)
            path.close()
            color(if(i%2==0)0xffe29aff.toInt() else 0xff83f5ff.toInt())
            c.drawPath(path,p)
            color(0xbaffffff.toInt())
            p.style=Paint.Style.STROKE
            p.strokeWidth=2f
            c.drawLine(x,y-height+8f,x-10f,y+16f,p)
            p.style=Paint.Style.FILL
        }
    }

    /** Active volcanic land is an environmental backdrop, never a false
     * unlocked level marker. The red-orange fissures use fixed local geometry.
     */
    private fun volcanoCrown(c:Canvas){
        path.reset()
        path.moveTo(600f,236f)
        path.lineTo(654f,143f)
        path.lineTo(682f,192f)
        path.lineTo(722f,107f)
        path.lineTo(763f,180f)
        path.lineTo(808f,141f)
        path.lineTo(862f,235f)
        path.close()
        color(0xff61404d.toInt())
        c.drawPath(path,p)
        path.reset()
        path.moveTo(662f,190f)
        path.lineTo(713f,133f)
        path.lineTo(758f,196f)
        path.lineTo(735f,184f)
        path.lineTo(710f,166f)
        path.close()
        color(0xff2c273f.toInt())
        c.drawPath(path,p)
        color(0xffffe76c.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=12f
        c.drawArc(691f,126f,760f,201f,23f,133f,false,p)
        color(0xffff7824.toInt())
        p.strokeWidth=6f
        c.drawArc(691f,126f,760f,201f,23f,133f,false,p)
        p.style=Paint.Style.FILL
        for(i in 0..6){
            val x=580f+i*43f
            val y=228f+(i%3)*27f
            color(0xffff922c.toInt())
            c.drawCircle(x,y,4f+(i%3)*2f,p)
            color(0xffffe070.toInt())
            c.drawCircle(x-1f,y-1f,2f,p)
        }
    }
    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)
        val checkpoint=canvas.save()
        canvas.scale(width/LevelMapGeometry.width,height/LevelMapGeometry.height)
        // The older giant white fog oval washed out the three vivid island
        // palettes on the 941px owner reference. Keep atmospheric depth as
        // three local, softly colored glows, not a flat desaturating veil.
        for((x,y,r,tint) in arrayOf(
            floatArrayOf(706f,235f,287f,0f),
            floatArrayOf(613f,685f,303f,1f),
            floatArrayOf(344f,1137f,393f,2f))) {
            val glow=when(tint.toInt()){
                0 -> if(selected%3==2)0x55ffad65 else 0x33ffad65
                1 -> if(selected%3==1)0x5579e6ff else 0x3379e6ff
                else -> if(selected%3==0)0x559ffb72 else 0x339ffb72
            }
            color(glow)
            canvas.drawOval(x-r,y-r*.58f,x+r,y+r*.58f,p)
        }
        island(canvas,709f,239f,231f,222f,
            0xffe55b3d.toInt(),0xff664454.toInt(),0xfffa9b54.toInt())
        island(canvas,621f,690f,264f,252f,
            0xff288fe2.toInt(),0xff646b96.toInt(),0xff8de6ff.toInt())
        island(canvas,347f,1140f,352f,290f,
            0xff3eae59.toInt(),0xff8b7269.toInt(),0xffa6ef75.toInt())
        cliffFacets(canvas,709f,239f,231f,222f)
        cliffFacets(canvas,621f,690f,264f,252f)
        cliffFacets(canvas,347f,1140f,352f,290f)
        volcanoCrown(canvas)
        crystalCluster(canvas)
        meadowCastle(canvas)
        meadowCanopy(canvas)
        // The reference contains no large floating selection halo across
        // the scenery. World selection is already visible through the real
        // pressed/selected sign and enabled stage plates.
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
        // Footpaths must stay on their *own* island. The former one-piece
        // path joined Meadow -> Crystal -> Volcano through open sky, creating
        // two enormous wooden columns absent from the owner's world map.
        // A stage remains operable through its real native level marker.
        val pts=LevelMapGeometry.nodes
        for(group in arrayOf(0..2,3..4)) {
            path.reset()
            path.moveTo(pts[group.first].x+54f,pts[group.first].y+52f)
            for(i in group.first+1..group.last) {
                val from=pts[i-1];val to=pts[i]
                path.quadTo((from.x+to.x)*.5f+54f,
                    (from.y+to.y)*.5f+40f,
                    to.x+54f,to.y+52f)
            }
            // color() resets Paint to FILL; explicitly enable STROKE for
            // all three rope/stone passes to avoid filling the landscape.
            color(0xff68472d.toInt());p.style=Paint.Style.STROKE
            p.strokeWidth=25f;p.strokeCap=Paint.Cap.ROUND
            canvas.drawPath(path,p)
            color(0xffffdeb2.toInt());p.style=Paint.Style.STROKE
            p.strokeWidth=16f;p.strokeCap=Paint.Cap.ROUND
            canvas.drawPath(path,p)
            color(0xfffff5d8.toInt());p.style=Paint.Style.STROKE
            p.strokeWidth=3f;p.strokeCap=Paint.Cap.ROUND
            canvas.drawPath(path,p)
        }
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
