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
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Communication.Communication.getGameClearListener
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

    private lateinit var fragmentView: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): ViewGroup {
        fragmentView = inflater.inflate(R.layout.fragment_draw, container, false) as ViewGroup

        val canvasView = DrawCanvas(
            activity!!.applicationContext,
            null,
            this.activity!!.intent.getStringExtra("username")
        )

        canvasView.layoutParams = LinearLayout.LayoutParams(1000, 750)

        fragmentView.ConstraintLayout_canvasView.addView(canvasView)

        fragmentView.button_change_color.setOnClickListener {
            openColorPicker(canvasView)
        }

        fragmentView.button_strokeErase.setOnClickListener {
            switchStrokeEraser(canvasView)
        }

        fragmentView.button_normalErase.setOnClickListener {
            switchNormalEraser(canvasView)
        }

        fragmentView.button_changeWidth.setOnClickListener {
            openWidthSelector(canvasView)
        }

        fragmentView.button_round.setOnClickListener {
            switchDrawWithCircle(canvasView)
            fragmentView.button_square.isChecked = false
            fragmentView.button_round.isChecked = true
        }

        fragmentView.button_square.setOnClickListener {
            switchDrawWithSquare(canvasView)
            fragmentView.button_square.isChecked = true
            fragmentView.button_round.isChecked = false
        }

        return fragmentView
    }

    private fun switchDrawWithCircle(v: DrawCanvas) {
        v.switchDrawWithCircle()
    }

    private fun switchDrawWithSquare(v: DrawCanvas) {
        v.switchDrawWithSquare()
    }

    private fun switchStrokeEraser(v: DrawCanvas) {
        v.isStrokeErasing = !v.isStrokeErasing
        if (v.isNormalErasing) {
            v.isNormalErasing = false
            fragmentView.button_normalErase.toggle()
        }
    }

    private fun switchNormalEraser(v: DrawCanvas) {
        v.isNormalErasing = !v.isNormalErasing
        if (v.isStrokeErasing) {
            v.isStrokeErasing = false
            fragmentView.button_strokeErase.toggle()
        }
    }

    private fun openColorPicker(v: DrawCanvas) {
        val colorPicker = AmbilWarnaDialog(this.context, v.paintLine.color, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    v.paintLine.color = color
                }
            })
        colorPicker.show()
    }

    private fun openWidthSelector(v: DrawCanvas) {
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
        seekBar.progress = v.paintLine.strokeWidth.toInt()

        okButton.setOnClickListener {
            val minStrokeSize = 5.0f
            v.paintLine.strokeWidth = max(minStrokeSize, seekBar.progress.toFloat())
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
    var isDrawer = false
    private val eraserHalfSize = 4
    private var bitmapNeedsToUpdate = false
    private var paintScreen = Paint()
    private var currentStartX = 0
    private var currentStartY = 0
    private var segments = ArrayList<Segment>()
    private var strokeJustEnded = true
    private var drawListener: Disposable
    private var roleListener: Disposable
    private var clearListener: Disposable
    private var drawerSub: Disposable
    private val matrixSquareSize = 32
    private var lastErasePoint: Point? = null
    private var segmentsToBeRemoved = ArrayList<Segment>()
    private lateinit var matrix: Array<Array<ArrayList<Segment>>>
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas

    init {
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 16.0F
        paintLine.strokeCap = Paint.Cap.ROUND

        drawListener = Communication.getDrawListener().subscribe { obj ->
            strokeReceived(obj)
        }

        roleListener = Communication.getDrawerUpdateListener().subscribe {
            clearStrokes()
        }

        clearListener = getGameClearListener().subscribe {
            clearStrokes()
        }

        drawerSub = Communication.getDrawerUpdateListener().subscribe { res ->
            isDrawer = res.getString("username") == username
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawListener.dispose()
        roleListener.dispose()
        drawerSub.dispose()
        clearListener.dispose()
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
        if (!isDrawer) {
            return true
        }

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
                checkForStrokesToErase(x, y, isStrokeErasing)
                sendErase(x, y, isStrokeErasing)
            }
        } else if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            addSegment(Point(x,y))
            sendStroke(x, y, x, y, false)
            currentStartX = x
            currentStartY = y
        } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            if (strokeJustEnded) {
                currentStartX = x
                currentStartY = y
            }
            touchMoved(currentStartX, currentStartY, x, y)
            currentStartX = x
            currentStartY = y
        } else if (event.actionMasked == MotionEvent.ACTION_UP){
            sendStroke(x, y, x, y, true)
            strokeJustEnded = true
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

            checkForStrokesToErase(newX, newY, isStrokeErasing)
            sendErase(newX, newY, isStrokeErasing)
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

        matrix = Array((height / matrixSquareSize) + 1) {
            Array((width / matrixSquareSize) + 1) {
                ArrayList<Segment>()
            }
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
                segmentsToBeRemoved.add(segment.nextSegment!!)
                batchErase(segment.nextSegment!!)
            }
        }

        if (segment.previousSegment != null) {
            if (segment.previousSegment!!.paint.color != Color.TRANSPARENT) {
                segment.previousSegment!!.paint.color = Color.TRANSPARENT
                segmentsToBeRemoved.add(segment.previousSegment!!)
                batchErase(segment.previousSegment!!)
            }
        }
    }

    private fun removeSegmentFromMatrix(segment: Segment) {
        val width = paintLine.strokeWidth / 2
        val middle      = Point(segment.point.x / matrixSquareSize, segment.point.y / matrixSquareSize)
        val left        = Point((segment.point.x - width).toInt() / matrixSquareSize, middle.y)
        val right       = Point((segment.point.x + width).toInt() / matrixSquareSize, middle.y)
        val top         = Point(middle.x, (segment.point.y + width).toInt() / matrixSquareSize)
        val bottom      = Point(middle.x, (segment.point.y - width).toInt() / matrixSquareSize)

        synchronized(matrix) {
            matrix[middle.y][middle.x].remove(segment)
            if (top.y != middle.y) {
                matrix[top.y][top.x].remove(segment)
                val topLeft = Point(
                    (segment.point.x - width).toInt() / matrixSquareSize,
                    (segment.point.y + width).toInt() / matrixSquareSize
                )
                if (topLeft.x != middle.x) {
                    matrix[topLeft.y][topLeft.x].remove(segment)
                }
                val topRight = Point(
                    (segment.point.x + width).toInt() / matrixSquareSize,
                    (segment.point.y + width).toInt() / matrixSquareSize
                )
                if (topRight.x != middle.x) {
                    matrix[topRight.y][topRight.x].remove(segment)
                }
            } else if (bottom.y != middle.y) {
                matrix[bottom.y][bottom.x].remove(segment)
                val bottomLeft = Point(
                    (segment.point.x - width).toInt() / matrixSquareSize,
                    (segment.point.y - width).toInt() / matrixSquareSize
                )
                if (bottomLeft.x != middle.x) {
                    matrix[bottomLeft.y][bottomLeft.x].remove(segment)
                }
                val bottomRight = Point(
                    (segment.point.x + width).toInt() / matrixSquareSize,
                    (segment.point.y - width).toInt() / matrixSquareSize
                )
                if (bottomRight.x != middle.x) {
                    matrix[bottomRight.y][bottomRight.x].remove(segment)
                }
            }
            if (left.x != middle.x) {
                matrix[left.y][left.x].remove(segment)
            }
            if (right.x != middle.x) {
                matrix[right.y][right.x].remove(segment)
            }
        }
    }

    private fun addSegmentToMatrix(segment: Segment) {
        val width = paintLine.strokeWidth / 2
        val middle      = Point(segment.point.x / matrixSquareSize, segment.point.y / matrixSquareSize)
        val left        = Point((segment.point.x - width).toInt() / matrixSquareSize, middle.y)
        val right       = Point((segment.point.x + width).toInt() / matrixSquareSize, middle.y)
        val top         = Point(middle.x, (segment.point.y + width).toInt() / matrixSquareSize)
        val bottom      = Point(middle.x, (segment.point.y - width).toInt() / matrixSquareSize)

        synchronized(matrix) {
            matrix[middle.y][middle.x].add(segment)
            if (top.y != middle.y) {
                matrix[top.y][top.x].add(segment)
                val topLeft = Point(
                    (segment.point.x - width).toInt() / matrixSquareSize,
                    (segment.point.y + width).toInt() / matrixSquareSize
                )
                if (topLeft.x != middle.x) {
                    matrix[topLeft.y][topLeft.x].add(segment)
                }
                val topRight = Point(
                    (segment.point.x + width).toInt() / matrixSquareSize,
                    (segment.point.y + width).toInt() / matrixSquareSize
                )
                if (topRight.x != middle.x) {
                    matrix[topRight.y][topRight.x].add(segment)
                }
            } else if (bottom.y != middle.y) {
                matrix[bottom.y][bottom.x].add(segment)
                val bottomLeft = Point(
                    (segment.point.x - width).toInt() / matrixSquareSize,
                    (segment.point.y - width).toInt() / matrixSquareSize
                )
                if (bottomLeft.x != middle.x) {
                    matrix[bottomLeft.y][bottomLeft.x].add(segment)
                }
                val bottomRight = Point(
                    (segment.point.x + width).toInt() / matrixSquareSize,
                    (segment.point.y - width).toInt() / matrixSquareSize
                )
                if (bottomRight.x != middle.x) {
                    matrix[bottomRight.y][bottomRight.x].add(segment)
                }
            }
            if (left.x != middle.x) {
                matrix[left.y][left.x].add(segment)
            }
            if (right.x != middle.x) {
                matrix[right.y][right.x].add(segment)
            }
        }
    }

    private fun checkPointForErase(pointX: Int, pointY: Int, isStroke: Boolean): Boolean {
        var strokeFound = false

        synchronized(matrix) {
            for (segment in matrix[pointY / matrixSquareSize][pointX / matrixSquareSize]) {
                if (segment.paint.color == Color.TRANSPARENT) {
                    continue
                }

                val eraserHalfSize = segment.paint.strokeWidth / 2.0f

                if (segment.point.x + eraserHalfSize >= pointX &&
                    segment.point.x - eraserHalfSize <= pointX &&
                    segment.point.y + eraserHalfSize >= pointY &&
                    segment.point.y - eraserHalfSize <= pointY
                ) {
                    segment.paint.color = Color.TRANSPARENT
                    segmentsToBeRemoved.add(segment)
                    if (isStroke) {
                        batchErase(segment)
                    }

                    strokeFound = true
                }
            }
        }

        return strokeFound
    }

    private fun checkForStrokesToErase(pointX: Int, pointY: Int, isStroke: Boolean) {
        val strokeFound = (
            checkPointForErase(pointX, pointY, isStroke) ||
            checkPointForErase(pointX + eraserHalfSize, pointY, isStroke) ||
            checkPointForErase(pointX - eraserHalfSize, pointY, isStroke) ||
            checkPointForErase(pointX, pointY + eraserHalfSize, isStroke) ||
            checkPointForErase(pointX, pointY - eraserHalfSize, isStroke))

        if (strokeFound) {
            //bitmapNeedsToUpdate = true

            val paintWhite = Paint()
            paintWhite.color = Color.WHITE
            paintWhite.style = Paint.Style.STROKE
            paintWhite.strokeCap = Paint.Cap.ROUND

            synchronized(segmentsToBeRemoved) {
                for (segment in segmentsToBeRemoved) {
                    paintWhite.strokeWidth = segment.paint.strokeWidth
                    bitmapCanvas.drawPoint(segment.point.x.toFloat(), segment.point.y.toFloat(), paintWhite)

                    synchronized(segments) {
                        for (s in matrix[segment.point.y / matrixSquareSize][segment.point.x / matrixSquareSize]) {
                            bitmapCanvas.drawPoint(s.point.x.toFloat(), s.point.y.toFloat(), s.paint)
                        }
                    }
                }
            }

            for (segment in segmentsToBeRemoved) {
                removeSegmentFromMatrix(segment)
            }

            segmentsToBeRemoved = ArrayList()
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
            addSegment(Point(startX, startY))

            return
        }

        val directionX = (destX - startX) / distance
        val directionY = (destY - startY) / distance

        for (i in 0..distance.toInt()) {
            val newX = (startX + directionX * i).toInt()
            val newY = (startY + directionY * i).toInt()
            addSegment(Point(newX, newY))
            bitmapCanvas.drawPoint(newX.toFloat(), newY.toFloat(), paintLine)
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

            addSegmentToMatrix(segments[segments.size - 1])
        }

        strokeJustEnded = false
    }

    private fun strokeReceived(obj: JSONObject) {
        when {
            obj.getString("type") == "ink" -> {
                if (isDrawer) {
                    return
                }

                if (obj.getBoolean("isEnd")) {
                    strokeJustEnded = true
                    return
                }

                if (strokeJustEnded) {
                    bitmapNeedsToUpdate = true
                }

                if (!isValidPoint(obj.getInt("startPosX"), obj.getInt("startPosY")) ||
                    !isValidPoint(obj.getInt("endPosX"), obj.getInt("endPosY"))) {
                    strokeJustEnded = true
                    return
                }

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
                if (!isValidPoint(obj.getInt("x"), obj.getInt("y"))) {
                    return
                }
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
