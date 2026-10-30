package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Reference-inspired, fully native, offline menus. The attached illustrations
 * are visual references, not interactive bitmap screenshots: every price, star,
 * pilot choice and unlock is read from real ProgressStore state.
 */
internal class ReferenceMenus(private val host:Activity, private val progress:ProgressStore) {
    private fun d(value:Int)=(value*host.resources.displayMetrics.density+.5f).toInt()
    private val navy=0xff082858.toInt()
    private val cream=0xfffff2dd.toInt()

    private fun plate(top:Int,bottom:Int,stroke:Int,radius:Int=22):GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(top,bottom)).apply {
            cornerRadius=d(radius).toFloat()
            setStroke(d(2),stroke)
        }
    private fun text(label:String,size:Int=18,color:Int=navy):TextView =
        TextView(host).apply {
            this.text=label
            textSize=size.toFloat()
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            setPadding(d(5),d(5),d(5),d(5))
        }
    private fun button(label:String,primary:Boolean=true,onClick:()->Unit):Button =
        Button(host).apply {
            text=label
            isAllCaps=false
            textSize=if(primary)20f else 15f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(primary)0xff69310a.toInt() else Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                if(primary)plate(0xffffe961.toInt(),0xffff8c00.toInt(),0xfffff3c6.toInt(),28)
                else plate(0xff31c7ff.toInt(),0xff0859ca.toInt(),0xffadf6ff.toInt(),24),null)
            elevation=d(5).toFloat()
            minHeight=d(52)
            contentDescription=label
            setOnClickListener { onClick() }
        }
    private fun row():LinearLayout=LinearLayout(host).apply {
        orientation=LinearLayout.HORIZONTAL
        gravity=Gravity.CENTER
        clipChildren=false
        clipToPadding=false
    }
    private fun shell(title:String,world:Int=0):Pair<FrameLayout,LinearLayout> {
        val root=FrameLayout(host).apply {setBackgroundColor(0xff2189e3.toInt())}
        val backdrop=ImageView(host).apply {
            setImageResource(if(world==0)R.drawable.homeback else when(world) {
                1->R.drawable.world1
                2->R.drawable.world2
                3->R.drawable.world3
                4->R.drawable.world4
                5->R.drawable.world5
                6->R.drawable.world6
                else->R.drawable.world7
            })
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        root.addView(backdrop,FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply {
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x28032b76,0x117ed5fa,0xaa041b42.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val body=LinearLayout(host).apply {
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(16),d(32),d(16),d(25))
        }
        val scroll=ScrollView(host).apply {
            isFillViewport=true
            clipToPadding=false
            addView(body,FrameLayout.LayoutParams(-1,-2))
            setOnApplyWindowInsetsListener{v,insets->
                if(Build.VERSION.SDK_INT>=30) {
                    val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    v.setPadding(safe.left,safe.top,safe.right,safe.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    v.setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                }
                insets
            }
        }
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        val heading=text(title,30,Color.WHITE).apply {
            letterSpacing=.035f
            background=plate(0xffffdf39.toInt(),0xffff8700.toInt(),
                0xfffff4c4.toInt(),24)
            setTextColor(0xff713708.toInt())
            elevation=d(6).toFloat()
        }
        body.addView(heading,LinearLayout.LayoutParams(-1,d(80)).apply {
            topMargin=d(15);bottomMargin=d(15)
        })
        return root to body
    }
    private fun back(body:LinearLayout,onBack:()->Unit) {
        body.addView(button("⌂  HOME",false,onBack),
            LinearLayout.LayoutParams(-1,d(54)).apply{topMargin=d(8)})
    }
    fun skins(choice:Int,onChoose:(Int)->Unit,onBack:()->Unit):View {
        val (root,body)=shell("SKINS")
        body.addView(text("CHOOSE YOUR PILOT",21,Color.WHITE),
            LinearLayout.LayoutParams(-1,d(50)))
        val choices=arrayOf("CLASSIC AVIATOR" to "BOPI","WISE OWL" to "SOVA",
            "SKY FOX" to "LISAC")
        val imgs=intArrayOf(R.drawable.bopiportrait0,R.drawable.bopiportrait9,R.drawable.bopiportrait10)
        val descriptions=arrayOf("Fast classic wingbeat","Smooth, calm flight","Protective first impact")
        for(i in choices.indices){
            val card=LinearLayout(host).apply {
                orientation=LinearLayout.HORIZONTAL
                gravity=Gravity.CENTER_VERTICAL
                setPadding(d(12),d(12),d(12),d(12))
                background=plate(if(choice==i)0xfffff9e3.toInt() else 0xffe4f2ff.toInt(),
                    if(choice==i)0xffffdb7b.toInt() else 0xff87c9f5.toInt(),
                    if(choice==i)0xffffa113.toInt() else 0xff2b8af2.toInt(),22)
                elevation=d(4).toFloat()
                isClickable=true
                isFocusable=true
                contentDescription=(if(choice==i)"Selected " else "Choose ")+choices[i].first
                setOnClickListener{onChoose(i)}
            }
            card.addView(ImageView(host).apply {
                setImageResource(imgs[i])
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(d(104),d(120)))
            val details=LinearLayout(host).apply{orientation=LinearLayout.VERTICAL}
            details.addView(text(choices[i].first,17),LinearLayout.LayoutParams(-1,-2))
            details.addView(text(descriptions[i],12,0xff315073.toInt()),
                LinearLayout.LayoutParams(-1,-2))
            details.addView(text(if(choice==i)"✓ EQUIPPED" else "EQUIP",16,
                if(choice==i)0xff067c30.toInt() else 0xff035bb8.toInt()),
                LinearLayout.LayoutParams(-1,d(34)))
            card.addView(details,LinearLayout.LayoutParams(0,-2,1f))
            body.addView(card,LinearLayout.LayoutParams(-1,-2).apply {
                topMargin=d(8);bottomMargin=d(8)
            })
        }
        back(body,onBack)
        return root
    }

    fun shop(onBuy:(Int)->Unit,onClaim:()->Unit,onBack:()->Unit):View {
        val (root,body)=shell("SHOP")
        body.addView(text("COINS  •  ${progress.coins()}",24,cream).apply{
            background=plate(0xff164b8e.toInt(),0xff09234c.toInt(),
                0xff7be9ff.toInt(),20)
            contentDescription="Earned coins: ${progress.coins()}"
        },LinearLayout.LayoutParams(-1,d(62)).apply{bottomMargin=d(10)})
        val bonus=progress.bonusCoinsAvailable()
        if(bonus>0)body.addView(button("CLAIM +$bonus EARNED COINS"){onClaim()},
            LinearLayout.LayoutParams(-1,d(56)).apply{bottomMargin=d(8)})
        val icons=arrayOf("🛡","🧲","🛡","🧲","🛡","🧲")
        for(start in 0..4 step 2){
            val pair=row()
            for(index in start..start+1) {
                val card=LinearLayout(host).apply {
                    orientation=LinearLayout.VERTICAL
                    gravity=Gravity.CENTER
                    setPadding(d(7),d(13),d(7),d(13))
                    background=plate(0xfffff8e8.toInt(),0xffffdfaa.toInt(),
                        0xffffa54b.toInt(),18)
                    elevation=d(3).toFloat()
                }
                card.addView(text(icons[index],34),LinearLayout.LayoutParams(-1,d(55)))
                card.addView(text(progress.perkNames[index].uppercase(),15),
                    LinearLayout.LayoutParams(-1,d(60)))
                card.addView(text("OWNED: ${progress.perkCount(index)}",12),
                    LinearLayout.LayoutParams(-1,d(30)))
                card.addView(button("🪙 ${progress.perkPrices[index]}"){onBuy(index)},
                    LinearLayout.LayoutParams(-1,d(52)).apply{topMargin=d(5)})
                pair.addView(card,LinearLayout.LayoutParams(0,-2,1f).apply {
                    setMargins(d(4),d(4),d(4),d(4))
                })
            }
            body.addView(pair,LinearLayout.LayoutParams(-1,-2))
        }
        body.addView(text("Only earned offline coins. No in-app payments or fake gem balance.",12,
            Color.WHITE),LinearLayout.LayoutParams(-1,-2))
        back(body,onBack)
        return root
    }

    fun levels(world:Int,onWorld:(Int)->Unit,onPlay:(Int,Long)->Unit,onBack:()->Unit):View {
        val selected=world.coerceIn(0,7)
        val (root,body)=shell("LEVEL MAP",selected)
        val names=LevelEngine.names
        val tabs=HorizontalScrollView(host).apply {
            isHorizontalScrollBarEnabled=false
            val strip=row()
            for(i in names.indices) {
                strip.addView(button("${i+1}  ${names[i].uppercase()}",false) {onWorld(i)},
                    LinearLayout.LayoutParams(d(180),d(57)).apply {
                        leftMargin=d(4);rightMargin=d(4)
                    })
            }
            addView(strip)
        }
        body.addView(tabs,LinearLayout.LayoutParams(-1,d(65)))
        body.addView(text(names[selected].uppercase(),22,cream),
            LinearLayout.LayoutParams(-1,d(55)))
        val stars=progress.worldStars(selected)
        body.addView(text("★ $stars / 30    •    LEVEL ${progress.frontier(selected)}",18,
            Color.WHITE).apply {background=plate(0xff14498a.toInt(),0xff09254a.toInt(),
                0xff80ebff.toInt(),18)},LinearLayout.LayoutParams(-1,d(62)))
        val frontier=progress.frontier(selected).coerceIn(1,LevelEngine.LEVELS_PER_WORLD)
        val first=(frontier-3).coerceAtLeast(1)
        val last=(first+5).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)
        for(level in first..last) {
            val unlocked=level<=frontier
            val earned=progress.bestLevelStars(selected,level.toLong())
            val node=button(if(unlocked)"★".repeat(earned)+"  LEVEL $level  ▶"
                else "🔒  LEVEL $level",unlocked) {
                if(unlocked)onPlay(selected,level.toLong())
            }
            node.isEnabled=unlocked
            node.contentDescription=if(unlocked)"Play level $level, $earned stars"
                else "Locked level $level"
            body.addView(node,LinearLayout.LayoutParams(-1,d(64)).apply{
                topMargin=d(5);bottomMargin=d(5)
            })
        }
        body.addView(button("▶  PLAY LEVEL $frontier"){onPlay(selected,frontier.toLong())},
            LinearLayout.LayoutParams(-1,d(74)).apply{topMargin=d(20)})
        back(body,onBack)
        return root
    }
}
