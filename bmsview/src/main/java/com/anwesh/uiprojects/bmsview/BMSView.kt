package com.anwesh.uiprojects.bmsview

/**
 * Created by anweshmishra on 19/07/18.
 */

import android.graphics.Paint
import android.graphics.Canvas
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Color

val BMS_NODES : Int = 5

fun Canvas.drawBall(gap : Float, y : Float, r : Float, scale : Float,  paint : Paint) {
    paint.style = Paint.Style.STROKE
    save()
    translate(gap * scale, y - r)
    rotate(360f * scale)
    drawCircle(0f, 0f, r, paint)
    drawLine(0f, 0f, 0f, -r * 0.6f, paint)
    restore()
}

fun Canvas.drawStep(gap : Float, scale : Float, paint : Paint) {
    drawLine(0f, 0f, gap, -gap * scale, paint)
}

fun Canvas.drawBSMNode(i : Int, j : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = (Math.min(w, h) * 0.9f) / BMS_NODES
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap  = Paint.Cap.ROUND
    paint.color = Color.parseColor("#2ecc71")
    save()
    translate(0.05f * w + i * gap, 0.95f * h - i * gap)
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    drawStep(gap, sc1, paint)
    if (i == j) {
        drawBall(gap, -gap * sc2, gap/10, sc2, paint)
    }
    restore()
}

class BMSView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class BMSState(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class BMSAnimator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(60)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false 
            }
        }
    }
}
