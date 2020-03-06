package com.example.client_leger.Fragments

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Communication.Communication
import com.example.client_leger.SocketIO
import kotlinx.android.synthetic.main.fragment_chat.view.*
import org.json.JSONArray
import org.json.JSONObject

class DrawFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Communication.getDrawListener().subscribe{ receptMes ->
            Log.w("draw", "draw received by: " + receptMes.getString("username"))
        }
        return DrawCanvas(activity!!.applicationContext, null)
    }
}

class DrawCanvas: View {

    private var paint = Paint()
    private var path = Path()
    private var startPointX: Float = 0.0F
    private var startPointY: Float = 0.0F
    private var finishPointX: Float = 0.0F
    private var finishPointY: Float = 0.0F

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

    private fun sendStroke() {
        val obj = JSONObject()
        obj.put("arenaID", 0)
        obj.put("username", "Testusername")
        obj.put("startPosX", startPointX)
        obj.put("startPosY", startPointY)
        obj.put("endPosX", finishPointX)
        obj.put("endPosY", finishPointY)

        SocketIO.sendStroke("draw", obj)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val x = e!!.x
        val y = e!!.y

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                startPointX = x
                startPointY = y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                finishPointX = x
                finishPointY = y
                sendStroke()
                startPointX = finishPointX
                startPointY = finishPointY
            }
            MotionEvent.ACTION_UP -> {}
            else -> { return false }
        }

        invalidate()
        return true
    }

}