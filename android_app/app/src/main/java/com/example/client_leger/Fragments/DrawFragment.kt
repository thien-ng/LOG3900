package com.example.client_leger.Fragments

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createBitmap
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.*
import android.widget.Button
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.example.client_leger.Communication.Communication
import com.example.client_leger.R
import com.example.client_leger.SocketIO
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_draw.view.*
import org.json.JSONObject
import yuku.ambilwarna.AmbilWarnaDialog
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


class DrawFragment: Fragment() {

    private val canvasViewChildPosition = 6

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

        v.button_changeWidth.setOnClickListener {
            openWidthSelector(v)
        }

        v.button_round.setOnClickListener {
            switchDrawWithCircle(v)
            v.button_square.isChecked = false
            v.button_round.isChecked = true
        }

        v.button_square.setOnClickListener {
            switchDrawWithSquare(v)
            v.button_square.isChecked = true
            v.button_round.isChecked = false
        }

        v.addView(DrawCanvas(activity!!.applicationContext, null, this.activity!!.intent.getStringExtra("username")))

        return v
    }

    private fun switchDrawWithCircle(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(canvasViewChildPosition) as DrawCanvas
        drawCanvasView.switchDrawWithCircle()
    }

    private fun switchDrawWithSquare(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(canvasViewChildPosition) as DrawCanvas
        drawCanvasView.switchDrawWithSquare()
    }

    private fun switchStrokeEraser(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(canvasViewChildPosition) as DrawCanvas
        drawCanvasView.isStrokeErasing = !drawCanvasView.isStrokeErasing
        if (drawCanvasView.isNormalErasing) {
            drawCanvasView.isNormalErasing = false
            v.button_normalErase.toggle()
        }
    }

    private fun switchNormalEraser(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(canvasViewChildPosition) as DrawCanvas
        drawCanvasView.isNormalErasing = !drawCanvasView.isNormalErasing
        if (drawCanvasView.isStrokeErasing) {
            drawCanvasView.isStrokeErasing = false
            v.button_strokeErase.toggle()
        }
    }

    private fun openColorPicker(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(canvasViewChildPosition) as DrawCanvas
        val colorPicker = AmbilWarnaDialog(this.context, drawCanvasView.paintLine.color, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawCanvasView.paintLine.color = color
                }
            })
        colorPicker.show()
    }

    private fun openWidthSelector(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(canvasViewChildPosition) as DrawCanvas
        val view = layoutInflater.inflate(R.layout.popup_change_width, null)
        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBar_changeWidth)
        val okButton = view.findViewById<Button>(R.id.button_changeWidthOk)

        updateSeekBarThumbSize(seekBar)
        seekBar.progress = drawCanvasView.paintLine.strokeWidth.toInt()

        okButton.setOnClickListener {
            val minStrokeSize = 5.0f
            drawCanvasView.paintLine.strokeWidth = max(minStrokeSize, seekBar.progress.toFloat())
            popupWindow.dismiss()
        }

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) { }

            override fun onStartTrackingTouch(seekBar: SeekBar) { }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateSeekBarThumbSize(seekBar)
            }
        })

    }

    private fun updateSeekBarThumbSize(seekBar: SeekBar) {
        val th = ShapeDrawable(OvalShape())
        val minThumbWidth = 5
        th.intrinsicWidth = seekBar.progress + minThumbWidth
        th.intrinsicHeight = seekBar.progress + minThumbWidth
        seekBar.thumb = th
    }
}

class Segment(var path: Path, var paint: Paint, var previousSegment: Segment?, var nextSegment: Segment?)

class DrawCanvas(ctx: Context, attr: AttributeSet?, private var username: String) : View(ctx, attr) {
    var paintLine: Paint = Paint()
    var isStrokeErasing = false
    var isNormalErasing = false
    private var bitmapNeedsToUpdate = false
    private var paintScreen = Paint()
    private var currentStroke = Path()
    private var currentStartX = 0f
    private var currentStartY = 0f
    private var segments = ArrayList<Segment>()
    private var strokeJustEnded = true
    private var drawListener: Disposable
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas

    init {
        paintLine.isAntiAlias = true
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 16.0F
        paintLine.strokeCap = Paint.Cap.ROUND

        drawListener = Communication.getDrawListener().subscribe{ obj ->
            strokeReceived(obj)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawListener.dispose()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmap.eraseColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmapNeedsToUpdate) {
            redrawPathsToBitmap()
        }
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, paintScreen)
        canvas.drawPath(currentStroke, paintLine)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if ((isStrokeErasing || isNormalErasing)) {
            if (isValidErasePoint(event.x, event.y)) {
                checkForStrokesToErase(event.x, event.y, isStrokeErasing)
                sendErase(event.x, event.y, isStrokeErasing)
            }
        } else if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            currentStartX = event.x
            currentStartY = event.y
        } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            touchMoved(currentStartX, currentStartY, event.x, event.y)
        } else if (event.actionMasked == MotionEvent.ACTION_UP){
            strokeJustEnded = true
            bitmapCanvas.drawPath(Path(currentStroke), Paint(paintLine))
            currentStroke.reset()
        }

        return true
    }

    fun switchDrawWithCircle() {
        paintLine.strokeCap = Paint.Cap.ROUND
    }

    fun switchDrawWithSquare() {
        paintLine.strokeCap = Paint.Cap.SQUARE
    }

    fun clearStrokes() {
        segments = ArrayList()
        currentStroke.reset()
        bitmapNeedsToUpdate = true
        postInvalidate()
    }

    private fun isValidErasePoint(x: Float, y: Float): Boolean {
        if (x > bitmap.width ||
            x < 0 ||
            y > bitmap.height ||
            y < 0) {

            return false
        }

        if (bitmap.getPixel(x.toInt(), y.toInt()) == Color.WHITE) {
            return false
        }

        return true
    }

    private fun redrawPathsToBitmap() {
        bitmap.eraseColor(Color.WHITE)
        for (i in 0 until segments.count()) {
            bitmapCanvas.drawPath(segments[i].path, segments[i].paint)
        }

        bitmapNeedsToUpdate = false
    }

    private fun batchErase(segment: Segment) {
        // recursive breadth search
        if (segment.nextSegment != null) {
            if (segment.nextSegment!!.paint.color != Color.TRANSPARENT) {
                segment.nextSegment!!.paint.color = Color.TRANSPARENT
                batchErase(segment.nextSegment!!)
            }
        }

        if (segment.previousSegment != null) {
            if (segment.previousSegment!!.paint.color != Color.TRANSPARENT) {
                segment.previousSegment!!.paint.color = Color.TRANSPARENT
                batchErase(segment.previousSegment!!)
            }
        }
    }

    private fun checkForStrokesToErase(pointX: Float, pointY: Float, isStroke: Boolean) {
        var strokeFound = false

        for (i in 0 until segments.count()) {
            if (segments[i].paint.color == Color.TRANSPARENT) {
                continue
            }

            val pm = PathMeasure(segments[i].path, false)
            val coordinates = FloatArray(2)

            pm.getPosTan(pm.length / 2.0f, coordinates, null)
            val eraserHalfSize = segments[i].paint.strokeWidth / 2.0f
            val xOnLine = coordinates[0]
            val yOnLine = coordinates[1]

            if (xOnLine <= pointX.toInt() + eraserHalfSize && xOnLine >= pointX.toInt() - eraserHalfSize) {
                if (yOnLine <= pointY.toInt() + eraserHalfSize && yOnLine >= pointY.toInt() - eraserHalfSize) {
                    segments[i].paint.color = Color.TRANSPARENT
                    if (isStroke) {
                        batchErase(segments[i])
                    }

                    strokeFound = true
                }
            }
        }

        if (strokeFound) {
            bitmapNeedsToUpdate = true
            postInvalidate()
        }
    }

    private fun touchMoved(startX: Float, startY: Float, destX: Float, destY: Float) {
        val deltaX = (destX - currentStartX)
        val deltaY = (destY - currentStartY)
        val distance = sqrt(deltaX.pow(2.0F) + deltaY.pow(2.0F))

        if (distance == 0.0F) {
            return
        }

        val directionX = (destX - currentStartX) / distance
        val directionY = (destY - currentStartY) / distance

        for (i in 1..(distance / paintLine.strokeWidth).toInt()) {
            val newX = startX + directionX * paintLine.strokeWidth * i
            val newY = startY + directionY * paintLine.strokeWidth * i

            currentStroke.moveTo(currentStartX, currentStartY)
            currentStroke.lineTo(newX, newY)
            sendStroke(currentStartX, newX, currentStartY, newY, strokeJustEnded)
            addSegment(currentStartX, newX, currentStartY, newY, strokeJustEnded)

            currentStartX = newX
            currentStartY = newY
        }

        postInvalidate()
    }

    private fun addSegment(startX: Float, destX: Float,startY: Float, destY: Float, isNew: Boolean) {
        val newSegment = Path()
        newSegment.moveTo(startX, startY)
        newSegment.lineTo(destX, destY)
        segments.add(Segment(newSegment, Paint(paintLine), null, null))
        if (segments.size - 2 >= 0 && !isNew) {
            // segments.size - 1 is the index of the segment we just added,
            // segments.size - 2 is the index of the segment just before it.
            segments[segments.size - 1].previousSegment = segments[segments.size - 2]
            segments[segments.size - 2].nextSegment = segments[segments.size - 1]
        }

        strokeJustEnded = false
    }

    private fun strokeReceived(obj: JSONObject) {
        when {
            obj.getString("type") == "ink" -> {
                currentStroke.moveTo(obj.getInt("startPosX").toFloat(), obj.getInt("startPosY").toFloat())
                currentStroke.lineTo(obj.getInt("endPosX").toFloat(), obj.getInt("endPosY").toFloat())

                paintLine.isAntiAlias = true
                paintLine.color = obj.getInt("color")
                paintLine.style = Paint.Style.STROKE
                paintLine.strokeWidth = obj.getInt("width").toFloat()
                paintLine.strokeCap =
                    if (obj.getString("format") == "circle")
                        Paint.Cap.ROUND
                    else
                        Paint.Cap.SQUARE

                addSegment(
                    obj.getInt("startPosX").toFloat(),
                    obj.getInt("endPosX").toFloat(),
                    obj.getInt("startPosY").toFloat(),
                    obj.getInt("endPosY").toFloat(),
                    obj.getBoolean("isEnd")
                )

                if (obj.getBoolean("isEnd")) {
                    currentStroke.reset()
                    bitmapNeedsToUpdate = true
                }
            }
            obj.getString("type") == "eraser" -> {
                currentStroke.reset()
                checkForStrokesToErase(
                    obj.getInt("x").toFloat(),
                    obj.getInt("y").toFloat(),
                    obj.getString("eraser") == "stroke"
                )
            }
        }

        postInvalidate()
    }

    private fun sendStroke(startPointX: Float, finishPointX: Float, startPointY: Float, finishPointY: Float, isEnd: Boolean) {
        val obj = JSONObject()
        obj.put("type", "ink")
        obj.put("username", username)
        obj.put("startPosX", startPointX)
        obj.put("startPosY", startPointY)
        obj.put("endPosX", finishPointX)
        obj.put("endPosY", finishPointY)
        obj.put("color", paintLine.color)
        obj.put("width", paintLine.strokeWidth)
        obj.put("isEnd", isEnd)
        obj.put("format", if (paintLine.strokeCap == Paint.Cap.ROUND) "circle" else "square")

        SocketIO.sendMessage("gameplay", obj)
    }

    private fun sendErase(x: Float, y: Float, isStroke: Boolean) {
        val obj = JSONObject()
        obj.put("type", "eraser")
        obj.put("username", username)
        obj.put("x", x)
        obj.put("y", y)
        obj.put("eraser", if (isStroke) "stroke" else "point")

        SocketIO.sendMessage("gameplay", obj)
    }
}


