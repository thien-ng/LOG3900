package com.example.client_leger.Fragments

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class DrawFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DrawCanvas(activity!!.applicationContext, null)
    }
}

class DrawCanvas: View {

    private var paint = Paint()
    private var path = Path()

    constructor(ctx: Context, attr: AttributeSet?): super(ctx, attr) {
        paint.isAntiAlias = true
        paint.color = (Color.BLACK)
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawPath(path, paint)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val x = e!!.x
        val y = e!!.y

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> { path.lineTo(x, y) }
            MotionEvent.ACTION_UP -> {}
            else -> { return false }
        }

        invalidate()
        return true
    }

}