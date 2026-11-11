package com.brendigo.bopavi

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
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
    // The footer must show the real installed app version, never the
    // hard-coded v1.0.0 label baked into an illustration.
    @Suppress("DEPRECATION")
    private fun installedVersion():String = runCatching {
        host.packageManager.getPackageInfo(host.packageName,0).versionName ?: "?"
    }.getOrDefault("?")
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
            // 12sp row labels and the compact version footer must never
            // use an auto-size minimum larger than their requested maximum.
            // Android throws IllegalArgumentException during Settings creation
            // for an invalid 13..12sp interval, then returns to the launcher.
            val minAutoSp=SettingsTextSizePolicy.minSp(size)
            setAutoSizeTextTypeUniformWithConfiguration(minAutoSp,size,1,
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
                     controlWidthDp:Int=96):View {
        val row=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(12),0,dp(9),0)
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
                setImageDrawable(BopaviUiIconDrawable(kind,Color.WHITE,dp(27)))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(dp(27),dp(27),Gravity.CENTER))
        },LinearLayout.LayoutParams(dp(30),dp(30)))
        row.addView(title(text,if(text=="Mode")12 else 14,navy).apply{
            gravity=Gravity.CENTER_VERTICAL or Gravity.START
            setSingleLine(true)
            ellipsize=android.text.TextUtils.TruncateAt.END
            setAutoSizeTextTypeUniformWithConfiguration(10,14,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            setShadowLayer(0f,0f,0f,Color.TRANSPARENT)
        },LinearLayout.LayoutParams(0,-1,1f).apply{marginStart=dp(9)})
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
            // Use native Switch touch/semantics but replace the platform's
            // flat outline with a stateful raised reference-green capsule.
            val onTrack=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xff86f761.toInt(),0xff32c62f.toInt(),
                    0xff128628.toInt())).apply {
                cornerRadius=dp(25).toFloat()
                setStroke(dp(2),0xff125b25.toInt())
                setSize(dp(51),dp(29))
            }
            val offTrack=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xfffaf0e0.toInt(),0xffcbbba8.toInt())).apply {
                cornerRadius=dp(25).toFloat()
                setStroke(dp(2),0xffb29c88.toInt())
                setSize(dp(51),dp(29))
            }
            trackDrawable=StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_checked),onTrack)
                addState(intArrayOf(),offTrack)
            }
            thumbDrawable=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.WHITE,0xffeff1f4.toInt(),
                    0xffc2cbd5.toInt())).apply {
                shape=GradientDrawable.OVAL
                setStroke(dp(2),0xff7d8da6.toInt())
                setSize(dp(27),dp(27))
            }
            trackTintList=null
            thumbTintList=null
            setOnCheckedChangeListener{_,checked->onChange(checked)}
        }

    private fun slider(name:String,initial:Int,onChange:(Int)->Unit):View {
        var lastSaved=initial
        val bar=SeekBar(host).apply{
            max=100
            progress=initial
            minimumHeight=dp(48)
            contentDescription="$name: $initial%"
            // Filled blue enamel track and pearl thumb are drawables owned by
            // a real SeekBar; dragging still persists the actual volume.
            val trackBack=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xfff0e7da.toInt(),0xffc7b7a2.toInt())).apply {
                cornerRadius=dp(11).toFloat()
                setStroke(dp(1),0xffb9a58e.toInt())
                setSize(dp(152),dp(14))
            }
            val trackFill=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xff58c9ff.toInt(),0xff0a99f4.toInt(),
                    0xff0870dc.toInt())).apply {
                cornerRadius=dp(11).toFloat()
                setStroke(dp(1),0xff0458b3.toInt())
                setSize(dp(152),dp(14))
            }
            progressDrawable=LayerDrawable(arrayOf(trackBack,
                ClipDrawable(trackFill,Gravity.START,ClipDrawable.HORIZONTAL))).apply {
                setId(0,android.R.id.background)
                setId(1,android.R.id.progress)
            }
            thumb=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xffffffff.toInt(),0xffe2eafa.toInt(),
                    0xffbdc8da.toInt())).apply {
                shape=GradientDrawable.OVAL
                setStroke(dp(2),0xff597198.toInt())
                setSize(dp(32),dp(32))
            }
            thumbTintList=null
            progressTintList=null
            progressBackgroundTintList=null
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
        // An independent speaker emblem ends the real native volume rail.
        return FrameLayout(host).apply {
            addView(bar,FrameLayout.LayoutParams(-1,-1).apply {
                rightMargin=dp(27)
            })
            addView(ImageView(host).apply {
                setImageDrawable(BopaviUiIconDrawable(
                    BopaviUiIconDrawable.Kind.SOUND,0xff3a6695.toInt(),dp(26)))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable=false
            },FrameLayout.LayoutParams(dp(26),dp(26),
                Gravity.END or Gravity.CENTER_VERTICAL))
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
               onPrivacy:()->Unit,onTerms:()->Unit,
               onNotifications:(Boolean)->Unit,onLanguage:()->Unit,
               onControls:()->Unit,onCloud:(Boolean)->Unit,
               onRestore:()->Unit,cloudEnabled:Boolean):View {
        val dm=host.resources.displayMetrics
        // Owner portrait uses nine live rows. The scrollable variant retains
        // all historical Sound, Reduced Motion and Difficulty controls.
        val reference=dm.widthPixels>=860 && dm.heightPixels>=1510 &&
            host.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT &&
            host.resources.configuration.fontScale<1.25f
        val language=progress.uiLanguage()
        val croatian=language=="hr"
        fun name(en:String,hr:String)=if(croatian)hr else en
        val legacyRows=listOf(
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
                SettingsLegalArrow(host,"Pročitaj politiku privatnosti",onPrivacy),
                controlWidthDp=52),
            line("Terms of Service",BopaviUiIconDrawable.Kind.RECORDS,
                SettingsLegalArrow(host,"Pročitaj uvjete korištenja",onTerms),
                controlWidthDp=52)
        )
        val addedRows=listOf(
            line(name("Notifications","Obavijesti"),BopaviUiIconDrawable.Kind.STAR,
                switchControl("Daily notifications",progress.notificationsEnabled(),onNotifications)),
            line(name("Language","Jezik"),BopaviUiIconDrawable.Kind.RECORDS,
                button(if(croatian)"Hrvatski" else "English",selected=true,click=onLanguage),
                controlWidthDp=122),
            line(name("Controls","Kontrole"),BopaviUiIconDrawable.Kind.FLIGHT,
                button("Touch",selected=true,click=onControls),controlWidthDp=122),
            line("Cloud Save",BopaviUiIconDrawable.Kind.SHIELD,
                switchControl("Cloud Save",cloudEnabled,onCloud))
        )
        // The owner order remains identical to the nine-row reference:
        // Music, Effects, Notifications, Language, Controls, Vibration,
        // Cloud Save, Privacy, Terms. The extra legacy controls remain
        // reachable through Controls and the accessible scroll layout.
        val rows=if(reference) listOf(
            legacyRows[0],legacyRows[1],addedRows[0],addedRows[1],
            addedRows[2],legacyRows[3],addedRows[3],legacyRows[6],legacyRows[7]
        ) else listOf(
            legacyRows[0],legacyRows[1],legacyRows[2],addedRows[0],
            addedRows[1],addedRows[2],legacyRows[3],legacyRows[4],
            legacyRows[5],addedRows[3],legacyRows[6],legacyRows[7]
        )
        val root=FrameLayout(host).apply{setBackgroundColor(0xff248fdf.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        // Narrow windows and large-font configurations keep a real
        // scrollable layout; no row is drawn off-screen as static decoration.
        if(reference){
            val stage=LevelMapStage(host)
            stage.useShopFullBleedReferenceCamera()
            root.addView(stage,FrameLayout.LayoutParams(-1,-1))
            stage.place(artwork(R.drawable.logo),203f,20f,535f,213f)
            stage.place(artwork(R.drawable.bopiportrait0),266f,216f,409f,332f)
            stage.place(button("‹",click=onBack).apply {
                contentDescription="HOME"
            },27f,22f,102f,102f,minTouch=true)
            stage.place(View(host).apply {
                background=SettingsWoodPanelDrawable()
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable=false
            },101f,559f,739f,1007f)
            stage.place(SettingsEmbossedTitle(host).apply {
                elevation=dp(8).toFloat()
            },182f,486f,577f,101f)
            // An etched separator groups the actual legal links below the
            // six real offline preferences. It is visual-only and non-clickable.
            stage.place(View(host).apply {
                background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xffffffff.toInt(),0xffcaaa83.toInt()))
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable=false
            },151f,1226f,638f,4f)
            rows.forEachIndexed{index,row->
                stage.place(row,SettingsReferenceLayout.left,
                    SettingsReferenceLayout.rows[index],
                    SettingsReferenceLayout.rowWidth,
                    SettingsReferenceLayout.rowHeight,minTouch=true)
            }
            // Real recovery of earned coin items and unlocks from an owner
            // selected JSON backup. This is not Play Billing restoration.
            stage.place(button("Restore Purchases",click=onRestore),203f,
                SettingsReferenceLayout.homeTop,535f,112f,minTouch=true)
            stage.place(title("v"+installedVersion(),13,0xffe1edf5.toInt()).apply {
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setShadowLayer(1f,0f,1f,0xff183c56.toInt())
            },778f,1624f,139f,42f)
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
            col.addView(button("Restore Purchases",click=onRestore),
                LinearLayout.LayoutParams(-1,dp(60)).apply{topMargin=dp(12)})
            col.addView(button("HOME",click=onBack),
                LinearLayout.LayoutParams(-1,dp(54)).apply{topMargin=dp(8)})
            col.addView(title("v"+installedVersion(),12,0xffe1edf5.toInt()).apply {
                gravity=Gravity.END or Gravity.CENTER_VERTICAL
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,dp(28)).apply{topMargin=dp(6)})
            scroll.addView(col)
            root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
            scroll.requestApplyInsets()
        }
        return root
    }
}
