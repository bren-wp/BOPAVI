package com.brendigo.bopavi

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

internal class GameOverPodium(host:Activity):View(host){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val rock=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val save=canvas.save()
        canvas.scale(width/605f,height/216f)
        rock.reset()
        rock.moveTo(22f,55f);rock.lineTo(575f,55f)
        rock.lineTo(552f,174f);rock.lineTo(428f,214f)
        rock.lineTo(169f,212f);rock.lineTo(45f,168f)
        rock.close()
        p.color=0xff886f74.toInt();p.style=Paint.Style.FILL
        canvas.drawPath(rock,p)
        p.color=0xff423e5a.toInt();p.style=Paint.Style.STROKE
        p.strokeWidth=8f;canvas.drawPath(rock,p)
        // Deterministic stone facets: no extra sprites, allocations or
        // fake pickups. The grassy top remains above the shaded rock faces.
        p.style=Paint.Style.FILL
        for(i in 0..7){
            val x=55f+i*62f
            rock.reset()
            rock.moveTo(x,85f)
            rock.lineTo(x+46f,85f)
            rock.lineTo(x+31f,178f+(i%2)*10f)
            rock.lineTo(x+8f,160f)
            rock.close()
            p.color=if(i%2==0)0x664b364d else 0x667ec6c4
            canvas.drawPath(rock,p)
        }
        p.color=0xff448a2b.toInt()
        canvas.drawOval(12f,11f,591f,103f,p)
        p.color=0xff8bdf42.toInt()
        canvas.drawOval(31f,4f,572f,84f,p)
        p.color=0xffc0ef66.toInt()
        canvas.drawOval(66f,5f,539f,48f,p)
        for(i in 0..12) {
            val x=41f+i*43f
            val y=32f+(i%3)*12f
            p.color=Color.WHITE
            canvas.drawCircle(x-4f,y,5f,p)
            canvas.drawCircle(x+4f,y,5f,p)
            canvas.drawCircle(x,y-4f,5f,p)
            canvas.drawCircle(x,y+4f,5f,p)
            p.color=0xffffd73d.toInt();canvas.drawCircle(x,y,3f,p)
        }
        canvas.restoreToCount(save)
    }
}
