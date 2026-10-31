package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Full native, accessible, offline records; no mock leaderboard entries. */
internal class LeaderboardScreen(private val host:Activity){
    private val navy=0xff0b2b60.toInt()
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private fun frame(a:Int,b:Int,c:Int,r:Int=19)=
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(a,b)).apply{
            cornerRadius=dp(r).toFloat()
            setStroke(dp(2),c)
        }
    private fun label(s:String,size:Int,color:Int=Color.WHITE):TextView=
        TextView(host).apply{
            text=s
            textSize=size.toFloat()
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(11,size,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
        }
    private fun home(back:()->Unit):Button=Button(host).apply{
        text="HOME"
        isAllCaps=false
        textSize=27f
        setTextColor(navy)
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        contentDescription="HOME"
        minimumHeight=dp(48)
        background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
            frame(0xffffe877.toInt(),0xffff940d.toInt(),0xffffe9b4.toInt(),32),null)
        elevation=dp(5).toFloat()
        setOnClickListener{back()}
    }
    private fun statistic(name:String,value:Number,icon:BopaviUiIconDrawable.Kind):View{
        val card=LinearLayout(host).apply{
            gravity=Gravity.CENTER_VERTICAL
            orientation=LinearLayout.HORIZONTAL
            setPadding(dp(6),0,dp(6),0)
            background=frame(0xfffff8e9.toInt(),0xffedcdab.toInt(),0xffbd8e66.toInt())
            contentDescription="$name: $value"
            isFocusable=true
        }
        card.addView(ImageView(host).apply{
            setImageDrawable(BopaviUiIconDrawable(icon,0xff1889d4.toInt(),dp(26)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(34),dp(34)))
        val values=LinearLayout(host).apply{orientation=LinearLayout.VERTICAL}
        values.addView(label(name,13,navy),LinearLayout.LayoutParams(-1,0,1f))
        values.addView(label(LocalLeaderboardPresentation.number(value),20,
            0xffa9540b.toInt()),LinearLayout.LayoutParams(-1,0,1f))
        card.addView(values,LinearLayout.LayoutParams(0,-1,1f))
        return card
    }
    private fun score(item:RankedEntry):View{
        val e=item.entry
        val card=LinearLayout(host).apply{
            gravity=Gravity.CENTER_VERTICAL
            orientation=LinearLayout.HORIZONTAL
            setPadding(dp(7),0,dp(7),0)
            background=frame(if(item.rank<=3)0xfffff2d2.toInt() else 0xffe8f4ff.toInt(),
                if(item.rank<=3)0xffebc681.toInt() else 0xffb7ddf5.toInt(),
                if(item.rank<=3)0xffc98c3e.toInt() else 0xff70b3e0.toInt())
            isFocusable=true
            contentDescription=item.description
        }
        card.addView(label("#"+item.rank,20,0xff9a5516.toInt()),
            LinearLayout.LayoutParams(dp(43),-1))
        val player=LinearLayout(host).apply{orientation=LinearLayout.VERTICAL}
        player.addView(label(e.name,16,navy).apply{
            gravity=Gravity.START or Gravity.CENTER_VERTICAL
        },LinearLayout.LayoutParams(-1,0,1f))
        player.addView(label(LevelEngine.names[e.world]+" · "+
            listOf("Opušteno","Standardno","Izazovno")[e.difficulty],12,
            0xff306290.toInt()).apply{
            gravity=Gravity.START or Gravity.CENTER_VERTICAL
        },LinearLayout.LayoutParams(-1,0,1f))
        card.addView(player,LinearLayout.LayoutParams(0,-1,1f))
        val numbers=LinearLayout(host).apply{orientation=LinearLayout.VERTICAL}
        numbers.addView(label(LocalLeaderboardPresentation.number(e.score),19,
            0xffa45916.toInt()),LinearLayout.LayoutParams(-1,0,1f))
        numbers.addView(label("${e.gates} GATES",12,navy),
            LinearLayout.LayoutParams(-1,0,1f))
        card.addView(numbers,LinearLayout.LayoutParams(dp(115),-1))
        return card
    }
    fun create(summary:LeaderboardSummary,records:List<LeaderboardEntry>,
               onBack:()->Unit):View{
        val scores=LocalLeaderboardPresentation.rows(records)
        val root=FrameLayout(host).apply{setBackgroundColor(0xff178cdb.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x72021d44,0x1659bfe9,0x92032352.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val stats=listOf(
            statistic("BEST SCORE",summary.best,BopaviUiIconDrawable.Kind.RECORDS),
            statistic("BEST FOOD",summary.food,BopaviUiIconDrawable.Kind.BERRY),
            statistic("BEST GATES",summary.gates,BopaviUiIconDrawable.Kind.GATE),
            statistic("ENDLESS RUNS",summary.runs,BopaviUiIconDrawable.Kind.FLIGHT),
            statistic("WING FLAPS",summary.flaps,BopaviUiIconDrawable.Kind.WING))
        val dm=host.resources.displayMetrics
        val reference=dm.widthPixels>=860 && dm.heightPixels>=1510 &&
            host.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT
        if(reference){
            val stage=LevelMapStage(host)
            root.addView(stage,FrameLayout.LayoutParams(-1,-1))
            stage.place(ImageView(host).apply{
                setImageResource(R.drawable.bopiportrait0)
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },288f,7f,365f,205f)
            stage.place(label("LEADERBOARD",67),92f,202f,757f,117f)
            stage.place(label("OFFLINE • THIS DEVICE",25,
                0xffffeaa5.toInt()),205f,305f,531f,58f)
            stats.take(3).forEachIndexed{i,v->
                stage.place(v,50f+i*295f,376f,253f,99f)
            }
            stats.drop(3).forEachIndexed{i,v->
                stage.place(v,187f+i*298f,482f,270f,96f)
            }
            stage.place(label("TOP 10 LOCAL SCORES",22),
                205f,577f,531f,48f)
            if(scores.isEmpty())stage.place(label(
                "NO SCORES YET — PLAY TO SET A RECORD",20),
                113f,730f,715f,180f)
            scores.forEachIndexed{i,v->
                stage.place(score(v),66f,LeaderboardLayout.firstRowY+
                    i*LeaderboardLayout.rowStep,809f,LeaderboardLayout.rowHeight)
            }
            stage.place(home(onBack),203f,LeaderboardLayout.homeY,535f,120f,minTouch=true)
            stage.setOnApplyWindowInsetsListener{_,insets->
                if(Build.VERSION.SDK_INT>=30){
                    val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    stage.setSafe(safe.left,safe.top,safe.right,safe.bottom)
                }else{
                    @Suppress("DEPRECATION")
                    stage.setSafe(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                }
                insets
            }
            stage.requestApplyInsets()
        }else{
            val scroll=ScrollView(host).apply{
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
            val col=LinearLayout(host).apply{
                orientation=LinearLayout.VERTICAL
                setPadding(dp(12),dp(8),dp(12),dp(12))
            }
            col.addView(home(onBack),LinearLayout.LayoutParams(-1,dp(54)))
            col.addView(label("LEADERBOARD",30),LinearLayout.LayoutParams(-1,dp(62)))
            col.addView(label("OFFLINE • THIS DEVICE",17,0xffffeaa5.toInt()),
                LinearLayout.LayoutParams(-1,dp(42)))
            stats.forEach{v->
                col.addView(v,LinearLayout.LayoutParams(-1,dp(59)).apply{
                    bottomMargin=dp(5)
                })
            }
            col.addView(label("TOP 10 LOCAL SCORES",18),
                LinearLayout.LayoutParams(-1,dp(53)))
            if(scores.isEmpty())col.addView(label(
                "NO SCORES YET — PLAY TO SET A RECORD",17),
                LinearLayout.LayoutParams(-1,dp(108)))
            scores.forEach{v->
                col.addView(score(v),LinearLayout.LayoutParams(-1,dp(71)).apply{
                    bottomMargin=dp(6)
                })
            }
            scroll.addView(col)
            root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
            scroll.requestApplyInsets()
        }
        return root
    }
}
