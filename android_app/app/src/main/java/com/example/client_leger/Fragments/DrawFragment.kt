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

        v.button_strokeErase.setOnClickListener {
            switchStrokeEraser(v)
        }

        v.button_normalErase.setOnClickListener {
            switchNormalEraser(v)
        }

        v.addView(DrawCanvas(activity!!.applicationContext, null, this.activity!!.intent.getStringExtra("username")))

        return v
    }

    private fun switchStrokeEraser(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(3) as DrawCanvas
        drawCanvasView.isStrokeErasing = !drawCanvasView.isStrokeErasing
        if (drawCanvasView.isNormalErasing) {
            drawCanvasView.isNormalErasing = false
            v.button_normalErase.toggle()
        }
    }

    private fun switchNormalEraser(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(3) as DrawCanvas
        drawCanvasView.isNormalErasing = !drawCanvasView.isNormalErasing
        if (drawCanvasView.isStrokeErasing) {
            drawCanvasView.isStrokeErasing = false
            v.button_strokeErase.toggle()
        }
    }

    private fun openColorPicker(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(3) as DrawCanvas
        val colorPicker = AmbilWarnaDialog(this.context, drawCanvasView.paintLine.color, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawCanvasView.paintLine.color = color
                }
            })
        colorPicker.show()
    }
}

class Stroke(var path: Path, var paint: Paint)

class DrawCanvas(ctx: Context, attr: AttributeSet?, private var username: String) : View(ctx, attr) {
    var paintLine: Paint = Paint()
    var paintLineWhite: Paint
    val paintLineDefault = Paint()
    var isStrokeErasing = false
    var isNormalErasing = false

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private var currentPath = Path()
    private var currentStartX = 0f
    private var currentStartY = 0f
    private val strokes = ArrayList<Stroke>()

    init {
        paintLine.isAntiAlias = true
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 7.0F
        paintLine.strokeCap = Paint.Cap.ROUND
        paintLineWhite = Paint(paintLine)
        paintLineWhite.color = Color.WHITE
        Communication.getDrawListener().subscribe{ obj ->
            strokeReceived(obj)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmap.eraseColor(Color.WHITE)
        bitmapCanvas.drawColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paintLineDefault)

        repeat(strokes.count()) {
            canvas.drawPath(strokes[it].path, strokes[it].paint)
        }

        val paint = if (isNormalErasing) paintLineWhite else paintLine
        canvas.drawPath(currentPath, paint)

        invalidate()
    }

    private fun removeStroke(index: Int) {
        //TODO: inform server of stroke removal
        strokes.removeAt(index)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked

        if (isStrokeErasing) {
            checkForStrokesToErase(event)
        } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.x, event.y)
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnded()
        } else {
            touchMoved(event)
        }

        invalidate()
        return true
    }

    private fun checkForStrokesToErase(event: MotionEvent) {
        var strokeFound = false

        for (i in 0 until strokes.size) {
            if (strokes[i].paint.color == Color.WHITE) {
                continue
            }

            val pm = PathMeasure(strokes[i].path, false)

            val nbStep: Int = pm.length.toInt() / 20
            val speed = pm.length / nbStep
            val coordinates = FloatArray(2)

            var distance = 0f
            while (distance < pm.length) {
                pm.getPosTan(distance, coordinates, null)
                val eraserHalfSize = 16
                val xOnLine = coordinates[0]
                val yOnLine = coordinates[1]

                if (xOnLine <= event.x.toInt() + eraserHalfSize && xOnLine >= event.x.toInt() - eraserHalfSize) {
                    if (yOnLine <= event.y.toInt() + eraserHalfSize && yOnLine >= event.y.toInt() - eraserHalfSize) {
                        removeStroke(i)
                        strokeFound = true
                        break
                    }
                }

                distance += speed
            }

            if (strokeFound)
                break
        }
    }

    private fun touchMoved(event: MotionEvent) {
        sendStroke(currentStartX, event.x, currentStartY, event.y)

        currentPath.quadTo(
            currentStartX,
            currentStartY,
            (event.x + currentStartX) / 2,
            (event.y + currentStartY) / 2
        )

        currentStartX = event.x
        currentStartY = event.y
    }

    private fun strokeReceived(obj: JSONObject) {
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

        strokes.add(Stroke(path, paint))
        draw(bitmapCanvas)
    }

    private fun sendStroke(startPointX: Float, finishPointX: Float, startPointY: Float, finishPointY: Float) {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("startPosX", startPointX)
        obj.put("startPosY", startPointY)
        obj.put("endPosX", finishPointX)
        obj.put("endPosY", finishPointY)
        obj.put("color", if (isNormalErasing) Color.WHITE else paintLine.color)
        obj.put("width", paintLine.strokeWidth) //TODO custom width if isNormalErasing

        SocketIO.sendMessage("gameplay", obj)
    }

    private fun touchEnded() {
        if (!isStrokeErasing) {
            val paint = Paint(paintLine)
            if (isNormalErasing)
                paint.color = Color.WHITE
            strokes.add(Stroke(Path(currentPath), paint))
        }

        currentPath.reset()
    }

    private fun touchStarted(x: Float, y: Float) {
        currentStartX = x
        currentStartY = y
        currentPath.moveTo(x, y)
    }
}


