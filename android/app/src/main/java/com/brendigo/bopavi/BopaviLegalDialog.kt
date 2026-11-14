package com.brendigo.bopavi

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Genuine native HTML legal document inside a responsive 3D BOPAVI frame.
 *
 * The original owner Privacy page is an illustrated wood panel, NOT a
 * framework-white AlertDialog. Header, legal body and close are separate
 * native Views. The exact offline HTML, real links and ScrollView are
 * supplied unchanged by MainActivity.showLegalNotice.
 *
 * No owner screenshot is used as a button or document background.
 */
internal object BopaviLegalDialog {
    private fun dp(host:Activity,value:Int):Int=
        (value*host.resources.displayMetrics.density+.5f).toInt()

    fun show(host:Activity,title:String,scrolling:ScrollView) {
        val dialog=Dialog(host)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val wood=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xffffd890.toInt(),0xffc17e3d.toInt(),
                       0xff743816.toInt())).apply {
            cornerRadius=dp(host,22).toFloat()
            setStroke(dp(host,4),0xff71320e.toInt())
        }
        val parchment=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xffffffff.toInt(),0xfffff8e8.toInt(),
                       0xffffedce.toInt())).apply {
            cornerRadius=dp(host,14).toFloat()
            setStroke(dp(host,3),0xffffdf94.toInt())
        }
        val root=LinearLayout(host).apply {
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            background=wood
            setPadding(dp(host,8),dp(host,9),dp(host,8),dp(host,10))
            isFocusableInTouchMode=true
        }
        val header=TextView(host).apply {
            text=title
            textSize=22f
            isAllCaps=false
            gravity=Gravity.CENTER
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(Color.WHITE)
            setShadowLayer(dp(host,2).toFloat(),0f,dp(host,2).toFloat(),
                0xff16316a.toInt())
            background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.SKY)
            contentDescription=title
        }
        root.addView(header,LinearLayout.LayoutParams(-1,dp(host,66)).apply {
            bottomMargin=dp(host,4)
        })

        val docNumber=TextView(host).apply {
            text=if(title=="Politika privatnosti")"Dokument 01" else "Dokument 02"
            textSize=14f
            gravity=Gravity.CENTER_VERTICAL or Gravity.START
            typeface=Typeface.create("sans-serif",Typeface.BOLD)
            setTextColor(0xff11457f.toInt())
            setPadding(dp(host,17),0,0,0)
        }
        root.addView(docNumber,LinearLayout.LayoutParams(-1,dp(host,30)))
        scrolling.background=parchment
        scrolling.isFillViewport=false
        root.addView(scrolling,LinearLayout.LayoutParams(-1,0,1f).apply {
            bottomMargin=dp(host,7)
        })

        val footer=FrameLayout(host)
        val close=Button(host).apply {
            text="ZATVORI"
            isAllCaps=false
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            textSize=18f
            setTextColor(Color.WHITE)
            contentDescription="Zatvori "+title
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                Bopavi3DTheme.button(Bopavi3DTheme.Finish.GREEN),null)
            minimumHeight=dp(host,48)
            setOnClickListener{dialog.dismiss()}
        }
        footer.addView(close,FrameLayout.LayoutParams(
            dp(host,150),dp(host,50),Gravity.END or Gravity.CENTER_VERTICAL).apply {
            rightMargin=dp(host,7)
        })
        root.addView(footer,LinearLayout.LayoutParams(-1,dp(host,58)))

        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(.64f)
        dialog.setCancelable(true)
        dialog.show()
        val width=(host.resources.displayMetrics.widthPixels*.86f).toInt()
            .coerceAtMost(dp(host,510))
        val height=(host.resources.displayMetrics.heightPixels*.84f).toInt()
            .coerceAtMost(dp(host,970))
        dialog.window?.setLayout(width,height)
    }
}
