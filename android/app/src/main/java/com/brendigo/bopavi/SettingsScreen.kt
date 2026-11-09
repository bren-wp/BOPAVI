package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
            setSingleLine(true)
            ellipsize=android.text.TextUtils.TruncateAt.END
            setAutoSizeTextTypeUniformWithConfiguration(9,20,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            background=Bopavi3DTheme.button(if(selected)
                Bopavi3DTheme.Finish.GOLD else Bopavi3DTheme.Finish.SKY)
            contentDescription=s
            elevation=dp(3).toFloat()
            setOnClickListener{click()}
        }
    private fun line(text:String,kind:BopaviUiIconDrawable.Kind,control:View,
                     controlWidthDp:Int=114):View {
        val row=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(8),0,dp(9),0)
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.CREAM)
            minimumHeight=dp(48)
        }
        // Every row has an independent raised icon tile, while the setting
        // remains a real native Switch, SeekBar or Button beside it.
        row.addView(FrameLayout(host).apply{
            background=Bopavi3DTheme.panel(when(kind){
                BopaviUiIconDrawable.Kind.MUSIC->Bopavi3DTheme.Finish.VIOLET
                BopaviUiIconDrawable.Kind.SOUND->Bopavi3DTheme.Finish.GREEN
                BopaviUiIconDrawable.Kind.FLIGHT->Bopavi3DTheme.Finish.VIOLET
                BopaviUiIconDrawable.Kind.STAR->Bopavi3DTheme.Finish.GOLD
                BopaviUiIconDrawable.Kind.RECORDS->Bopavi3DTheme.Finish.GOLD
                else->Bopavi3DTheme.Finish.SKY
            })
            elevation=dp(3).toFloat()
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            addView(ImageView(host).apply{
                setImageDrawable(BopaviUiIconDrawable(kind,Color.WHITE,dp(32)))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(dp(32),dp(32),Gravity.CENTER))
        },LinearLayout.LayoutParams(dp(36),dp(36)))
        row.addView(title(text,14,navy).apply{
            gravity=Gravity.CENTER_VERTICAL or Gravity.START
            setSingleLine(true)
            ellipsize=android.text.TextUtils.TruncateAt.END
            setAutoSizeTextTypeUniformWithConfiguration(11,14,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            setShadowLayer(0f,0f,0f,Color.TRANSPARENT)
        },LinearLayout.LayoutParams(0,-1,1f).apply{marginStart=dp(6)})
        // Difficulty is a three-way selector. A 140dp group makes each option
        // narrower than the 48dp accessibility minimum once margins are added.
        row.addView(control,LinearLayout.LayoutParams(dp(controlWidthDp),-1))
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
                // The full local difficulty name remains in accessibility.
                // Short visible names prevent clipped ellipses on 941px art.
                val shortLabel=when(index) {
                    0 -> "EASY"
                    1 -> "NORMAL"
                    else -> "HARD"
                }
                addView(button(shortLabel,
                    selected=index==progress.difficulty()){
                    if(index!=progress.difficulty())onChange(index)
                }.apply{
                    // Reserve a single readable line for each real difficulty.
                    setSingleLine(true)
                    ellipsize=android.text.TextUtils.TruncateAt.END
                    setAutoSizeTextTypeUniformWithConfiguration(9,15,1,
                        android.util.TypedValue.COMPLEX_UNIT_SP)
                    // Android's default Button minWidth can override the equal
                    // weights and clip the remaining difficulty options.
                    minWidth=0
                    minimumWidth=0
                    contentDescription=(if(index==progress.difficulty())
                        "Selected " else "Choose ")+progress.difficultyNames[index]
                },LinearLayout.LayoutParams(0,dp(48),1f).apply{
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
            line("Mode",BopaviUiIconDrawable.Kind.STAR,
                difficulty(onDifficulty),controlWidthDp=162),
            line("Privacy Policy",BopaviUiIconDrawable.Kind.SHIELD,
                button("OPEN",click=onPrivacy).apply{
                    contentDescription="Pročitaj politiku privatnosti"
                }),
            line("Terms of Service",BopaviUiIconDrawable.Kind.RECORDS,
                button("OPEN",click=onTerms).apply{
                    contentDescription="Pročitaj uvjete korištenja"
                })
        )
        val root=FrameLayout(host).apply{setBackgroundColor(0xff248fdf.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val dm=host.resources.displayMetrics
        // The fixed artwork grid cannot fit 64dp switches at enlarged font
        // scales without overlapping adjacent rows. Use the native scroll
        // layout for accessibility instead of clipping interactive controls.
        val reference=dm.widthPixels>=860 && dm.heightPixels>=1510 &&
            host.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT &&
            host.resources.configuration.fontScale<1.25f
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
            stage.place(title("SETTINGS",57).apply{
                background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.GOLD)
                setTextColor(0xff61330a.toInt())
                elevation=dp(8).toFloat()
            },182f,486f,577f,101f)
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
            col.addView(title("SETTINGS",31).apply{
                background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.GOLD)
                setTextColor(0xff61330a.toInt())
            },LinearLayout.LayoutParams(-1,dp(65)))
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
