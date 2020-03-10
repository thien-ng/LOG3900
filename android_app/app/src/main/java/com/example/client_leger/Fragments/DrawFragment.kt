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
        return DrawCanvas(activity!!.applicationContext, null, this.activity!!.intent.getStringExtra("username"))
    }
}

class DrawCanvas: View {

    private var paint = Paint()
    private var path = Path()
    private var username: String
    private var startPointX: Float = 0.0F
    private var startPointY: Float = 0.0F
    private var finishPointX: Float = 0.0F
    private var finishPointY: Float = 0.0F

    private var newPath = Path()
    private var newPaint = Paint()
    private var isExternal = false

    constructor(ctx: Context, attr: AttributeSet?, username: String): super(ctx, attr) {
        paint.isAntiAlias = true
        paint.color = (Color.BLACK)
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        this.username = username

        Communication.getDrawListener().subscribe{ obj ->
            draw(obj)
        }
    }

    private fun draw(obj: JSONObject) {
        newPath.moveTo(obj.getInt("startPosX").toFloat(), obj.getInt("startPosY").toFloat())
        newPath.lineTo(obj.getInt("endPosX").toFloat(), obj.getInt("endPosY").toFloat())

        // TODO get the actual color
        // newPaint.color = obj.getInt("color")
        newPaint.isAntiAlias = true
        newPaint.strokeJoin = Paint.Join.ROUND
        newPaint.style = Paint.Style.STROKE

        newPaint.color = (Color.BLACK)
        newPaint.strokeWidth = obj.getInt("width").toFloat()
        isExternal = true
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (isExternal)
            canvas!!.drawPath(newPath, newPaint)
        else
            canvas!!.drawPath(path, paint)
    }

    private fun sendStroke() {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("startPosX", startPointX)
        obj.put("startPosY", startPointY)
        obj.put("endPosX", finishPointX)
        obj.put("endPosY", finishPointY)
        obj.put("color", paint.color)
        obj.put("width", paint.strokeWidth)

        SocketIO.sendMessage("gameplay", obj)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val x = e!!.x
        val y = e!!.y

        isExternal = false

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