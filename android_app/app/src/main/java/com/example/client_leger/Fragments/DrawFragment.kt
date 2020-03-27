package com.example.client_leger.Fragments

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createBitmap
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.util.Log
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
    private var currentStartX = 0
    private var currentStartY = 0
    private var segments = ArrayList<Segment>()
    private var strokeJustEnded = true
    private var drawListener: Disposable
    private var lastErasePoint: Point? = null
    private var roleListener: Disposable
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private lateinit var matrix: Array<Array<ArrayList<Segment>>>

    init {
        paintLine.isAntiAlias = true
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 16.0F
        paintLine.strokeCap = Paint.Cap.ROUND

        drawListener = Communication.getDrawListener().subscribe{ obj ->
            strokeReceived(obj)
        }

        roleListener = Communication.getDrawerUpdateListener().subscribe{
            clearStrokes()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawListener.dispose()
        roleListener.dispose()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmap.eraseColor(Color.WHITE)
        matrix = Array(h / 100) { Array(w / 100) { ArrayList<Segment>() } }

        //val testPath = Path()
        //testPath.moveTo(100.0F, 100.0F)
        //testPath.lineTo(150.0F, 150.0F)
        //matrix[1][1].add(Segment(testPath, paintLine, null, null))
        //testPath.moveTo(200.0F, 100.0F)
        //testPath.lineTo(250.0F, 150.0F)
        //matrix[1][2].add(Segment(testPath, paintLine, null, null))
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmapNeedsToUpdate) {
            redrawPathsToBitmap()
        }
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, paintScreen)
        canvas.drawPath(currentStroke, paintLine)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        if ((isStrokeErasing || isNormalErasing)) {
            if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                if (lastErasePoint != null) {
                    val distance = distance(lastErasePoint!!, Point(x, y))
                    val directionX = (x - lastErasePoint!!.x) / distance
                    val directionY = (y - lastErasePoint!!.y) / distance
                    val startX = lastErasePoint!!.x
                    val startY = lastErasePoint!!.y

                    for (i in 1..(distance / 15).toInt()) {
                        val newX = (startX + directionX * 15 * i).toInt()
                        val newY = (startY + directionY * 15 * i).toInt()
                        if (isValidErasePoint(newX, newY)) {
                            checkForStrokesToErase(newX, newY, isStrokeErasing)
                            sendErase(newX, newY, isStrokeErasing)
                        }
                        lastErasePoint!!.x = newX
                        lastErasePoint!!.y = newY
                    }
                }
            } else {
                lastErasePoint = Point(x, y)
                if (isValidErasePoint(x, y)) {
                    checkForStrokesToErase(x, y, isStrokeErasing)
                    sendErase(x, y, isStrokeErasing)
                }
            }
        } else if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            currentStartX = x
            currentStartY = y
        } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            touchMoved(currentStartX, currentStartY, x, y)
        } else if (event.actionMasked == MotionEvent.ACTION_UP){
            strokeJustEnded = true
            bitmapCanvas.drawPath(Path(currentStroke), Paint(paintLine))
            currentStroke.reset()
        }

        return true
    }


    private fun distance(point1: Point, point2: Point): Float {
        val deltaX = (point2.x - point1.x).toFloat()
        val deltaY = (point2.y - point1.y).toFloat()

        return sqrt(deltaX.pow(2) + deltaY.pow(2))
    }

    fun switchDrawWithCircle() {
        paintLine.strokeCap = Paint.Cap.ROUND
    }

    fun switchDrawWithSquare() {
        paintLine.strokeCap = Paint.Cap.SQUARE
    }

    private fun clearStrokes() {
        synchronized(segments) {
            segments = ArrayList()
        }
        currentStroke.reset()
        bitmapNeedsToUpdate = true
        postInvalidate()
    }

    private fun isValidErasePoint(x: Int, y: Int): Boolean {
        if (x > bitmap.width ||
            x < 0 ||
            y > bitmap.height ||
            y < 0) {

            return false
        }

        if (bitmap.getPixel(x, y) == Color.WHITE) {
            return false
        }

        return true
    }

    private fun redrawPathsToBitmap() {
        bitmap.eraseColor(Color.WHITE)
        synchronized(segments) {
            for (segment in segments) {
                bitmapCanvas.drawPath(segment.path, segment.paint)
            }
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

    private fun checkForStrokesToErase(pointX: Int, pointY: Int, isStroke: Boolean) {
        var strokeFound = false

        synchronized(segments) {
            for (segment in matrix[pointY / 100][pointX / 100]) {
                if (segment.paint.color == Color.TRANSPARENT) {
                    continue
                }

                val pm = PathMeasure(segment.path, false)
                val coordinates = FloatArray(2)

                pm.getPosTan(pm.length / 2.0f, coordinates, null)
                val eraserHalfSize = segment.paint.strokeWidth / 2.0f
                val xOnLine = coordinates[0].toInt()
                val yOnLine = coordinates[1].toInt()

                if (xOnLine <= pointX + eraserHalfSize && xOnLine >= pointX - eraserHalfSize) {
                    if (yOnLine <= pointY + eraserHalfSize && yOnLine >= pointY - eraserHalfSize) {
                        segment.paint.color = Color.TRANSPARENT
                        if (isStroke) {
                            batchErase(segment)
                        }

                        strokeFound = true
                    }
                }
            }
        }

        if (strokeFound) {
            bitmapNeedsToUpdate = true
            postInvalidate()
        }
    }

    private fun touchMoved(startX: Int, startY: Int, destX: Int, destY: Int) {
        val distance = distance(Point(startX, startY), Point(destX, destY))

        if (distance == 0.0F) {
            return
        }

        val directionX = (destX - currentStartX) / distance
        val directionY = (destY - currentStartY) / distance

        for (i in 1..(distance / paintLine.strokeWidth).toInt()) {
            val newX = (startX + directionX * paintLine.strokeWidth * i).toInt()
            val newY = (startY + directionY * paintLine.strokeWidth * i).toInt()

            currentStroke.moveTo(currentStartX.toFloat(), currentStartY.toFloat())
            currentStroke.lineTo(newX.toFloat(), newY.toFloat())
            sendStroke(currentStartX, newX, currentStartY, newY, strokeJustEnded)
            addSegment(currentStartX, newX, currentStartY, newY, strokeJustEnded)

            currentStartX = newX
            currentStartY = newY
        }

        postInvalidate()
    }

    private fun addSegment(startX: Int, destX: Int, startY: Int, destY: Int, isNew: Boolean) {
        val newSegment = Path()
        newSegment.moveTo(startX.toFloat(), startY.toFloat())
        newSegment.lineTo(destX.toFloat(), destY.toFloat())

        synchronized(segments) {
            segments.add(Segment(newSegment, Paint(paintLine), null, null))
            if (segments.size - 2 >= 0 && !isNew) {
                // segments.size - 1 is the index of the segment we just added,
                // segments.size - 2 is the index of the segment just before it.
                segments[segments.size - 1].previousSegment = segments[segments.size - 2]
                segments[segments.size - 2].nextSegment = segments[segments.size - 1]
            }

            matrix[startY / 100][startX / 100].add(segments[segments.size - 1])
            if (startY / 100 != destY / 100 || startX / 100 != destX / 100) {
                matrix[destY / 100][destX / 100].add(segments[segments.size - 1])
            }
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
                    obj.getInt("startPosX"),
                    obj.getInt("endPosX"),
                    obj.getInt("startPosY"),
                    obj.getInt("endPosY"),
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
                    obj.getInt("x"),
                    obj.getInt("y"),
                    obj.getString("eraser") == "stroke"
                )
            }
        }

        postInvalidate()
    }

    private fun sendStroke(startPointX: Int, finishPointX: Int, startPointY: Int, finishPointY: Int, isEnd: Boolean) {
        val obj = JSONObject()
        obj.put("event", "draw")
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

    private fun sendErase(x: Int, y: Int, isStroke: Boolean) {
        val obj = JSONObject()
        obj.put("event", "draw")
        obj.put("type", "eraser")
        obj.put("username", username)
        obj.put("x", x)
        obj.put("y", y)
        obj.put("eraser", if (isStroke) "stroke" else "point")

        SocketIO.sendMessage("gameplay", obj)
    }
}


