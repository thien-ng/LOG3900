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

class Segment(var point: Point, var paint: Paint, var previousSegment: Segment?, var nextSegment: Segment?)

class DrawCanvas(ctx: Context, attr: AttributeSet?, private var username: String) : View(ctx, attr) {
    var paintLine: Paint = Paint()
    var isStrokeErasing = false
    var isNormalErasing = false
    private var bitmapNeedsToUpdate = false
    private var paintScreen = Paint()
    private var currentStartX = 0
    private var currentStartY = 0
    private var segments = ArrayList<Segment>()
    private var strokeJustEnded = true
    private var drawListener: Disposable
    private var lastErasePoint: Point? = null
    private var roleListener: Disposable
    private val matrixSquareSize = 100
    private lateinit var matrix: Array<Array<ArrayList<Segment>>>
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
        matrix = Array((h / matrixSquareSize) + 1) {
            Array((w / matrixSquareSize) + 1) {
                ArrayList<Segment>()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmapNeedsToUpdate) {
            redrawPathsToBitmap()
        }

        canvas.drawBitmap(bitmap, 0.0F, 0.0F, paintScreen)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        if (!isValidPoint(x, y)) {
            strokeJustEnded = true

            return true
        }

        if ((isStrokeErasing || isNormalErasing)) {
            if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                if (lastErasePoint != null) {
                    eraseInLine(x, y)
                }
            } else {
                lastErasePoint = Point(x, y)
                if (bitmap.getPixel(x, y) != Color.WHITE) {
                    checkForStrokesToErase(x, y, isStrokeErasing)
                    sendErase(x, y, isStrokeErasing)
                }
            }
        } else if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            addSegment(Point(x,y)) //todo test reception
            currentStartX = x
            currentStartY = y
        } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            touchMoved(currentStartX, currentStartY, x, y)
            currentStartX = x
            currentStartY = y
        } else if (event.actionMasked == MotionEvent.ACTION_UP){
            strokeJustEnded = true
            sendStroke(x, y, x, y, true)
            redrawPathsToBitmap()
        }

        return true
    }

    private fun eraseInLine(destX: Int, destY: Int) {
        val distance = distance(lastErasePoint!!, Point(destX, destY))

        if (distance == 0.0F) {
            return
        }

        val directionX = (destX - lastErasePoint!!.x) / distance
        val directionY = (destY - lastErasePoint!!.y) / distance

        for (i in 0..distance.toInt()) {
            val newX = (lastErasePoint!!.x + directionX * i).toInt()
            val newY = (lastErasePoint!!.y + directionY * i).toInt()

            if (bitmap.getPixel(newX, newY) != Color.WHITE) {
                checkForStrokesToErase(newX, newY, isStrokeErasing)
                sendErase(newX, newY, isStrokeErasing)
            }
        }

        lastErasePoint!!.x = destX
        lastErasePoint!!.y = destY
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

        bitmapNeedsToUpdate = true
        postInvalidate()
    }

    private fun isValidPoint(x: Int, y: Int): Boolean {
        return !(x > bitmap.width || x < 0 || y > bitmap.height || y < 0)
    }

    private fun redrawPathsToBitmap() {
        bitmap.eraseColor(Color.WHITE)
        synchronized(segments) {
            for (segment in segments) {
                bitmapCanvas.drawPoint(segment.point.x.toFloat(), segment.point.y.toFloat(), segment.paint)
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
            for (segment in matrix[pointY / matrixSquareSize][pointX / matrixSquareSize]) {
                if (segment.paint.color == Color.TRANSPARENT) {
                    continue
                }

                val eraserHalfSize = segment.paint.strokeWidth / 2.0f

                if (segment.point.x + eraserHalfSize >= pointX &&
                    segment.point.x - eraserHalfSize <= pointX &&
                    segment.point.y + eraserHalfSize >= pointY &&
                    segment.point.y - eraserHalfSize <= pointY ) {
                    segment.paint.color = Color.TRANSPARENT
                    if (isStroke) {
                        batchErase(segment)
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

    private fun touchMoved(startX: Int, startY: Int, destX: Int, destY: Int) {
        sendStroke(startX, startY, destX, destY, false)
        divideAndAddSegment(startX, startY, destX, destY)
        postInvalidate()
    }

    private fun divideAndAddSegment(startX: Int, startY: Int, destX: Int, destY: Int) {
        val distance = distance(Point(startX, startY), Point(destX, destY))

        if (distance == 0.0F) {
            return
        }

        val directionX = (destX - startX) / distance
        val directionY = (destY - startY) / distance

        for (i in 0..distance.toInt()) {
            val newX = (startX + directionX * i).toInt()
            val newY = (startY + directionY * i).toInt()
            addSegment(Point(newX, newY))
        }
    }

    private fun addSegment(point: Point) {
        synchronized(segments) {
            segments.add(Segment(point, Paint(paintLine), null, null))
            if (segments.size - 2 >= 0 && !strokeJustEnded) {
                // segments.size - 1 is the index of the segment we just added,
                // segments.size - 2 is the index of the segment just before it.
                segments[segments.size - 1].previousSegment = segments[segments.size - 2]
                segments[segments.size - 2].nextSegment = segments[segments.size - 1]
            }

            val middle      = Point(point.x / matrixSquareSize, point.y / matrixSquareSize)
            val left        = Point((point.x - paintLine.strokeWidth / 2).toInt() / matrixSquareSize, middle.y)
            val right       = Point((point.x + paintLine.strokeWidth / 2).toInt() / matrixSquareSize, middle.y)
            val top         = Point(middle.x, (point.y + paintLine.strokeWidth / 2).toInt() / matrixSquareSize)
            val bottom      = Point(middle.x, (point.y - paintLine.strokeWidth / 2).toInt() / matrixSquareSize)

            matrix[middle.y][middle.x].add(segments[segments.size - 1])
            if (top.y != middle.y) {
                matrix[top.y][top.x].add(segments[segments.size - 1])
                val topLeft     = Point((point.x - paintLine.strokeWidth / 2).toInt() / matrixSquareSize,
                    (point.y + paintLine.strokeWidth / 2).toInt() / matrixSquareSize)
                if (topLeft.x != middle.x) {
                    matrix[topLeft.y][topLeft.x].add(segments[segments.size - 1])
                }
                val topRight    = Point((point.x + paintLine.strokeWidth / 2).toInt() / matrixSquareSize,
                    (point.y + paintLine.strokeWidth / 2).toInt() / matrixSquareSize)
                if (topRight.x != middle.x) {
                    matrix[topRight.y][topRight.x].add(segments[segments.size - 1])
                }
            } else if (bottom.y != middle.y) {
                matrix[bottom.y][bottom.x].add(segments[segments.size - 1])
                val bottomLeft  = Point((point.x - paintLine.strokeWidth / 2).toInt() / matrixSquareSize,
                    (point.y - paintLine.strokeWidth / 2).toInt() / matrixSquareSize)
                if (bottomLeft.x != middle.x) {
                    matrix[bottomLeft.y][bottomLeft.x].add(segments[segments.size - 1])
                }
                val bottomRight = Point((point.x + paintLine.strokeWidth / 2).toInt() / matrixSquareSize,
                    (point.y - paintLine.strokeWidth / 2).toInt() / matrixSquareSize)
                if (bottomRight.x != middle.x) {
                    matrix[bottomRight.y][bottomRight.x].add(segments[segments.size - 1])
                }
            }
            if (left.x != middle.x) {
                matrix[left.y][left.x].add(segments[segments.size - 1])
            }
            if (right.x != middle.x) {
                matrix[right.y][right.x].add(segments[segments.size - 1])
            }
        }

        strokeJustEnded = false
    }

    private fun strokeReceived(obj: JSONObject) {
        when {
            obj.getString("type") == "ink" -> {
                if (obj.getBoolean("isEnd")) {
                    strokeJustEnded = true
                    return
                }

                if (strokeJustEnded) {
                    bitmapNeedsToUpdate = true
                }

                paintLine.isAntiAlias = true
                paintLine.color = obj.getInt("color")
                paintLine.style = Paint.Style.STROKE
                paintLine.strokeWidth = obj.getInt("width").toFloat()
                paintLine.strokeCap =
                    if (obj.getString("format") == "circle")
                        Paint.Cap.ROUND
                    else
                        Paint.Cap.SQUARE

                divideAndAddSegment(
                    obj.getInt("startPosX"),
                    obj.getInt("startPosY"),
                    obj.getInt("endPosX"),
                    obj.getInt("endPosY")
                )
            }
            obj.getString("type") == "eraser" -> {
                checkForStrokesToErase(
                    obj.getInt("x"),
                    obj.getInt("y"),
                    obj.getString("eraser") == "stroke"
                )
            }
        }

        postInvalidate()
    }

    private fun sendStroke(startPointX: Int, startPointY: Int, finishPointX: Int, finishPointY: Int, isEnd: Boolean) {
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
