package com.example.client_leger.Fragments

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Communication.Communication
import com.example.client_leger.R
import com.example.client_leger.SocketIO
import kotlinx.android.synthetic.main.fragment_draw.view.*
import org.json.JSONObject
import yuku.ambilwarna.AmbilWarnaDialog

class DrawFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): ViewGroup {
        val v = inflater.inflate(R.layout.fragment_draw, container, false) as ViewGroup

        v.button_change_color.setOnClickListener {
            openColorPicker(v)
        }

        v.addView(DrawCanvas(activity!!.applicationContext, null, this.activity!!.intent.getStringExtra("username")))

        return v
    }

    private fun openColorPicker(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(1) as DrawCanvas
        val colorPicker = AmbilWarnaDialog(this.context, drawCanvasView.paintLine.color, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawCanvasView.paintLine.color = color
                }
            })
        colorPicker.show()
    }
}

class DrawCanvas(ctx: Context, attr: AttributeSet?, private var username: String) :
    View(ctx, attr) {
    var paintLine: Paint = Paint()
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private var paintScreen: Paint = Paint()
    private var pathMap: HashMap<Int, Path>
    private var previousPointMap: HashMap<Int, Point>

    init {
        paintLine.isAntiAlias = true
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 7.0F
        paintLine.strokeCap = Paint.Cap.ROUND
        pathMap = HashMap()
        previousPointMap = HashMap()
        Communication.getDrawListener().subscribe{ obj ->
            drawReceived(obj)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmap.eraseColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paintScreen)

        repeat(pathMap.count()) {
            canvas.drawPath(pathMap[it]!!, paintLine)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val actionIndex = event.actionIndex

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.x, event.y, event.getPointerId(actionIndex))
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(event.getPointerId(actionIndex))
        } else {
            touchMoved(event)
        }

        invalidate()
        return true
    }

    private fun touchMoved(event: MotionEvent) {
        for(i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)

            if (pathMap.containsKey(pointerId)) {
                val point = previousPointMap[pointerId]!!

                sendStroke(point.x.toFloat(), event.x, point.y.toFloat(), event.y)

                pathMap[pointerId]!!.quadTo(
                    point.x.toFloat(),
                    point.y.toFloat(),
                    (event.x + point.x.toFloat()) / 2,
                    (event.y + point.y.toFloat()) / 2
                )

                point.x = event.x.toInt()
                point.y = event.y.toInt()
            }
        }
    }

    private fun drawReceived(obj: JSONObject) {
        val path = Path()
        path.moveTo(obj.getInt("startPosX").toFloat(), obj.getInt("startPosY").toFloat())
        path.lineTo(obj.getInt("endPosX").toFloat(), obj.getInt("endPosY").toFloat())

        val paint = Paint()
        paint.isAntiAlias = true
        //paint.color = obj.getInt("color") TODO: get received color
        paint.color = Color.BLACK         //TODO: and remove this line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = obj.getInt("width").toFloat()
        paint.strokeCap = Paint.Cap.ROUND

        bitmapCanvas.drawPath(path, paint)
        path.reset()
        invalidate()
    }

    private fun sendStroke(startPointX: Float, finishPointX: Float, startPointY: Float, finishPointY: Float) {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("startPosX", startPointX)
        obj.put("startPosY", startPointY)
        obj.put("endPosX", finishPointX)
        obj.put("endPosY", finishPointY)
        obj.put("color", paintLine.color)
        obj.put("width", paintLine.strokeWidth)

        SocketIO.sendMessage("gameplay", obj)
    }

    private fun touchEnded(pointerId: Int) {
        val path = pathMap[pointerId]!!
        bitmapCanvas.drawPath(path, paintLine)
        path.reset()
    }

    private fun touchStarted(x: Float, y: Float, pointerId: Int) {
        val path: Path
        val point: Point

        if (pathMap.containsKey(pointerId)) {
            path = pathMap[pointerId]!!
            point = previousPointMap[pointerId]!!
        } else {
            path = Path()
            pathMap[pointerId] = path
            point = Point()
            previousPointMap[pointerId] = point
        }

        path.moveTo(x, y)
        point.x = x.toInt()
        point.y = y.toInt()
    }
}