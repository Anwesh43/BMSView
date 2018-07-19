package com.anwesh.uiprojects.bmsview

/**
 * Created by anweshmishra on 19/07/18.
 */

import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Color

val BMS_NODES : Int = 5

fun Canvas.drawBall(gap : Float, y : Float, deg : Float, r : Float, scale : Float,  paint : Paint) {
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = r / 5
    val degGap : Float = 360f / BMS_NODES
    save()
    translate(gap * scale, y - r)
    rotate(deg + degGap * scale)
    drawCircle(0f, 0f, r, paint)
    drawLine(0f, 0f, 0f, -r * 0.6f, paint)
    restore()
}

fun Canvas.drawStep(gap : Float, scale : Float, paint : Paint) {
    drawLine(0f, -gap * scale, gap, -gap * scale, paint)
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
    val deg : Float = 360f/ BMS_NODES
    drawStep(gap, sc1, paint)
    if (i == j) {
        drawBall(gap, -gap * sc1, i * deg, gap/5, sc2, paint)
    }
    restore()
}

class BMSView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class BMSNode(var i : Int, val state : BMSState = BMSState()) {

        private var next : BMSNode? = null

        private var prev : BMSNode? = null

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun draw(j : Int, canvas : Canvas, paint : Paint) {
            canvas.drawBSMNode(i, j, state.scale, paint)
            next?.draw(j, canvas, paint)
        }

        fun addNeighbor() {
            if (i < BMS_NODES - 1) {
                next = BMSNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun getNext(dir : Int, cb : () -> Unit) : BMSNode {
            var curr : BMSNode? = this.prev
            if (dir == 1) {
                curr = this.next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedBMS(var i : Int) {

        private var root :BMSNode = BMSNode(0)

        private var curr : BMSNode = root

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw( curr.i, canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {i, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(i, scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : BMSView) {

        private val animator : BMSAnimator = BMSAnimator(view)

        private val lbms : LinkedBMS = LinkedBMS(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lbms.draw(canvas, paint)
            animator.animate {
                lbms.update {j, scale ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lbms.startUpdating {
                animator.start()
            }
        }

    }

    companion object {
        fun create(activity : Activity) : BMSView {
            val view : BMSView = BMSView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
