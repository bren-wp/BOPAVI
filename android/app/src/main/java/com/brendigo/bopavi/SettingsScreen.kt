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
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

/**
 * Full native settings page. The reference portrait uses a shared logical
 * coordinate camera; narrow/rotated windows switch to native vertical scroll
 * rather than overlapping the 48dp accessible touch targets.
 *
 * Settings only expose real, offline-backed features. Cloud Save,
 * notifications, language downloads and IAP restoration do not exist here.
 */
internal class SettingsScreen(private val host:Activity,
                              private val progress:ProgressStore) {
    private val navy=0xff092457.toInt()
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private fun plate(top:Int,bottom:Int,border:Int,rounded:Int)=
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(top,bottom)).apply{
            cornerRadius=dp(rounded).toFloat()
            setStroke(dp(2),border)
        }
    private fun title(s:String,size:Int,color:Int=Color.WHITE):TextView=
        TextView(host).apply{
            text=s
            textSize=size.toFloat()
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(13,size,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            setShadowLayer(dp(2).toFloat(),0f,dp(3).toFloat(),navy)
        }
    private fun button(s:String,selected:Boolean=false,click:()->Unit):Button =
        Button(host).apply{
            text=s
            isAllCaps=false
            textSize=20f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(selected)navy else Color.WHITE)
            gravity=Gravity.CENTER
            minimumHeight=dp(48)
            setAutoSizeTextTypeUniformWithConfiguration(12,20,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                if(selected)plate(0xffffe568.toInt(),0xffffab23.toInt(),
                    0xffab580b.toInt(),22)
                else plate(0xff49d9ff.toInt(),0xff0758c5.toInt(),
                    0xff9beaff.toInt(),22),null)
            contentDescription=s
            elevation=dp(3).toFloat()
            setOnClickListener{click()}
        }
    private fun line(text:String,kind:BopaviUiIconDrawable.Kind,control:View):View {
        val row=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(8),dp(5),dp(9),dp(5))
            background=plate(0xfffff8e9.toInt(),0xfff1ddbc.toInt(),
                0xffddbc96.toInt(),20)
            minimumHeight=dp(48)
        }
        row.addView(ImageView(host).apply{
            setImageDrawable(BopaviUiIconDrawable(kind,0xff1877d5.toInt(),dp(36)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(43),dp(43)))
        row.addView(title(text,18,navy).apply{
            gravity=Gravity.CENTER_VERTICAL or Gravity.START
            setShadowLayer(0f,0f,0f,Color.TRANSPARENT)
        },LinearLayout.LayoutParams(0,-1,1f).apply{marginStart=dp(6)})
        row.addView(control,LinearLayout.LayoutParams(dp(140),-1))
        return row
    }
    private fun switchControl(name:String,value:Boolean,onChange:(Boolean)->Unit):Switch =
        Switch(host).apply{
            isChecked=value
            showText=false
            minimumHeight=dp(48)
            contentDescription=name
            thumbTintList=ColorStateList.valueOf(0xfff9fff3.toInt())
            trackTintList=ColorStateList.valueOf(0xff49bf39.toInt())
            setOnCheckedChangeListener{_,checked->onChange(checked)}
        }

    private fun slider(name:String,initial:Int,onChange:(Int)->Unit):SeekBar {
        var lastSaved=initial
        return SeekBar(host).apply{
            max=100
            progress=initial
            minimumHeight=dp(48)
            contentDescription="$name: $initial%"
            progressTintList=ColorStateList.valueOf(0xff098ce7.toInt())
            thumbTintList=ColorStateList.valueOf(0xfff8fcff.toInt())
            progressBackgroundTintList=ColorStateList.valueOf(0xffd7c6af.toInt())
            setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                private var tracking=false
                fun save(value:Int){
                    val safe=value.coerceIn(0,100)
                    if(lastSaved==safe)return
                    onChange(safe)
                    lastSaved=safe
                }
                override fun onProgressChanged(bar:SeekBar?,value:Int,fromUser:Boolean){
                    bar?.contentDescription="$name: $value%"
                    if(VolumeGesturePolicy.commitAsProgressChanges(fromUser,tracking))
                        save(value)
                }
                override fun onStartTrackingTouch(bar:SeekBar?){tracking=true}
                override fun onStopTrackingTouch(bar:SeekBar?){
                    tracking=false
                    bar?.progress?.let(::save)
                }
            })
        }
    }
    private fun difficulty(onChange:(Int)->Unit):View=
        LinearLayout(host).apply{
            gravity=Gravity.CENTER
            orientation=LinearLayout.HORIZONTAL
            for(index in progress.difficultyNames.indices){
                addView(button(progress.difficultyNames[index],
                    selected=index==progress.difficulty()){
                    if(index!=progress.difficulty())onChange(index)
                },LinearLayout.LayoutParams(0,dp(54),1f).apply{
                    setMargins(dp(2),0,dp(2),0)
                })
            }
        }
    private fun artwork(id:Int):ImageView=ImageView(host).apply{
        setImageResource(id)
        scaleType=ImageView.ScaleType.FIT_CENTER
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun create(onBack:()->Unit,onMusic:(Int)->Unit,onEffects:(Int)->Unit,
               onSound:(Boolean)->Unit,onHaptic:(Boolean)->Unit,
               onMotion:(Boolean)->Unit,onDifficulty:(Int)->Unit,
               onPrivacy:()->Unit,onTerms:()->Unit):View {
        val rows=listOf(
            line("Music",BopaviUiIconDrawable.Kind.MUSIC,
                slider("Glazba",progress.musicVolume(),onMusic)),
            line("Sound Effects",BopaviUiIconDrawable.Kind.SOUND,
                slider("Zvučni efekti",progress.effectsVolume(),onEffects)),
            line("Sound",BopaviUiIconDrawable.Kind.SOUND,
                switchControl("Zvuk uključen",progress.soundEnabled(),onSound)),
            line("Vibration",BopaviUiIconDrawable.Kind.FLIGHT,
                switchControl("Vibracija",progress.hapticEnabled(),onHaptic)),
            line("Reduced Motion",BopaviUiIconDrawable.Kind.WING,
                switchControl("Smanjeni pokret",progress.lessMotion(),onMotion)),
            line("Difficulty",BopaviUiIconDrawable.Kind.STAR,
                difficulty(onDifficulty)),
            line("Privacy Policy",BopaviUiIconDrawable.Kind.SHIELD,
                button("OPEN",click=onPrivacy)),
            line("Terms of Service",BopaviUiIconDrawable.Kind.RECORDS,
                button("OPEN",click=onTerms))
        )
        val root=FrameLayout(host).apply{setBackgroundColor(0xff248fdf.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val dm=host.resources.displayMetrics
        val reference=dm.widthPixels>=860 && dm.heightPixels>=1510 &&
            host.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT
        if(reference){
            val stage=LevelMapStage(host)
            root.addView(stage,FrameLayout.LayoutParams(-1,-1))
            stage.place(artwork(R.drawable.logo),203f,20f,535f,213f)
            stage.place(artwork(R.drawable.bopiportrait0),266f,216f,409f,332f)
            stage.place(button("‹",click=onBack),27f,22f,102f,102f,minTouch=true)
            stage.place(View(host).apply{
                background=plate(0xfffff7e1.toInt(),0xffe8c7a0.toInt(),
                    0xff9b572b.toInt(),34)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },101f,559f,739f,1007f)
            stage.place(title("SETTINGS",57),182f,486f,577f,101f)
            rows.forEachIndexed{index,row->
                stage.place(row,SettingsReferenceLayout.left,
                    SettingsReferenceLayout.rows[index],
                    SettingsReferenceLayout.rowWidth,
                    SettingsReferenceLayout.rowHeight,minTouch=true)
            }
            stage.place(button("HOME",click=onBack),203f,
                SettingsReferenceLayout.homeTop,535f,112f,minTouch=true)
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
            val col=LinearLayout(host).apply{
                orientation=LinearLayout.VERTICAL
                setPadding(dp(14),dp(7),dp(14),dp(16))
            }
            col.addView(button("‹  HOME",click=onBack),
                LinearLayout.LayoutParams(dp(130),dp(52)))
            col.addView(artwork(R.drawable.logo),
                LinearLayout.LayoutParams(-1,dp(91)))
            col.addView(artwork(R.drawable.bopiportrait0),
                LinearLayout.LayoutParams(-1,dp(135)))
            col.addView(title("SETTINGS",31),
                LinearLayout.LayoutParams(-1,dp(65)))
            val card=LinearLayout(host).apply{
                orientation=LinearLayout.VERTICAL
                background=plate(0xfffff7e1.toInt(),0xffe8c7a0.toInt(),
                    0xff99592d.toInt(),25)
                setPadding(dp(8),dp(10),dp(8),dp(12))
            }
            rows.forEach{row->
                card.addView(row,LinearLayout.LayoutParams(-1,dp(67)).apply{
                    bottomMargin=dp(6)
                })
            }
            col.addView(card,LinearLayout.LayoutParams(-1,-2))
            col.addView(button("HOME",click=onBack),
                LinearLayout.LayoutParams(-1,dp(60)).apply{topMargin=dp(12)})
            scroll.addView(col)
            root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
            scroll.requestApplyInsets()
        }
        return root
    }
}
