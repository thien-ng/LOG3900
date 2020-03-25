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
import kotlinx.android.synthetic.main.fragment_draw.view.*
import org.json.JSONObject
import yuku.ambilwarna.AmbilWarnaDialog
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


class DrawFragment: Fragment() {

    private val canvasViewChildPosition = 4

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

        v.addView(DrawCanvas(activity!!.applicationContext, null, this.activity!!.intent.getStringExtra("username")))

        return v
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
            drawCanvasView.paintLine.strokeWidth = seekBar.progress.toFloat()
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

class Stroke(var middle: PointFloat, var segments: ArrayList<Segment>)

class PointFloat(var x: Float, var y: Float)

class DrawCanvas(ctx: Context, attr: AttributeSet?, private var username: String) : View(ctx, attr) {
    var paintLine: Paint = Paint()
    var isStrokeErasing = false
    var isNormalErasing = false
    private var lastErasePoint: PointFloat? = null
    private var paintScreen = Paint()
    private var currentStroke = Path()
    private var currentStartX = 0f
    private var currentStartY = 0f
    private val segments = ArrayList<Segment>()
    private val strokes = ArrayList<Stroke>()
    private var strokeJustEnded = false
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas

    init {
        paintLine.isAntiAlias = true
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 16.0F
        paintLine.strokeCap = Paint.Cap.ROUND
        Communication.getDrawListener().subscribe{ obj ->
            strokeReceived(obj)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmap.eraseColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, paintScreen)
        canvas.drawPath(currentStroke, paintLine)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked

        if (isStrokeErasing || isNormalErasing) {
            if (action == MotionEvent.ACTION_MOVE) {
                if (lastErasePoint != null) {
                    val distance = distance(lastErasePoint!!, PointFloat(event.x, event.y))
                    val directionX = (event.x - lastErasePoint!!.x) / distance
                    val directionY = (event.y - lastErasePoint!!.y) / distance
                    val startX = lastErasePoint!!.x
                    val startY = lastErasePoint!!.y

                    for (i in 1..(distance / 15).toInt()) {
                        val newX = startX + directionX * 15 * i
                        val newY = startY + directionY * 15 * i
                        checkForStrokesToErase(PointFloat(newX, newY))
                        lastErasePoint!!.x = newX
                        lastErasePoint!!.y = newY
                    }
                }

            } else {
                checkForStrokesToErase(PointFloat(event.x, event.y))
                lastErasePoint = PointFloat(event.x, event.y)
            }
        } else if (action == MotionEvent.ACTION_DOWN) {
            currentStroke.moveTo(event.x, event.y)
            currentStartX = event.x
            currentStartY = event.y
        } else if (action == MotionEvent.ACTION_MOVE){
            touchMoved(event)
        } else if (action == MotionEvent.ACTION_UP) {
            strokeJustEnded = true
            bitmapCanvas.drawPath(Path(currentStroke), Paint(paintLine))
            currentStroke.reset()
        }

        invalidate()
        return true
    }

    private fun redrawPathsToBitmap() {
        bitmap.eraseColor(Color.WHITE)
        for (segment in segments) {
            bitmapCanvas.drawPath(segment.path, segment.paint)
        }
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

    private fun checkForStrokesToErase(point: PointFloat) {
        if (point.x > bitmap.width ||
            point.x < 0 ||
            point.y > bitmap.height ||
            point.y < 0) {

            return
        }

        if (bitmap.getPixel(point.x.toInt(), point.y.toInt()) == Color.WHITE) {
            return
        }

        var strokeFound = false
        var strokesToRemove = ArrayList<Stroke>()

        for (stroke in strokes) {
            if ((stroke.middle.x - point.x).absoluteValue <= 300 &&
                (stroke.middle.y - point.y).absoluteValue <= 300) {

                for (segment in stroke.segments) {
                    if (segment.paint.color == Color.TRANSPARENT) {
                        continue
                    }

                    val pm = PathMeasure(segment.path, false)
                    val coordinates = FloatArray(2)

                    pm.getPosTan(pm.length / 2.0f, coordinates, null)

                    val eraserHalfSize = segment.paint.strokeWidth / 2.0f
                    val xOnLine = coordinates[0]
                    val yOnLine = coordinates[1]

                    if (xOnLine <= point.x.toInt() + eraserHalfSize && xOnLine >= point.x.toInt() - eraserHalfSize) {
                        if (yOnLine <= point.y.toInt() + eraserHalfSize && yOnLine >= point.y.toInt() - eraserHalfSize) {
                            segment.paint.color = Color.TRANSPARENT
                            if (isStrokeErasing) {
                                batchErase(segment)
                            }

                            var allSegmentsErased = true
                            for (newSegment in stroke.segments) {
                                if (newSegment.paint.color != Color.TRANSPARENT) {
                                    allSegmentsErased = false
                                    break
                                }
                            }

                            if (allSegmentsErased) {
                                strokesToRemove.add(stroke)
                            }

                            //TODO: use new emits to send erase point
                            strokeFound = true
                        }
                    }
                }
            }
        }

        if (strokeFound)
            for (stroke in strokesToRemove) {
                strokes.remove(stroke)
            }
            redrawPathsToBitmap()
    }

    private fun distance(point1: PointFloat, point2: PointFloat): Float {
        val deltaX = (point2.x - point1.x)
        val deltaY = (point2.y - point1.y)

        return sqrt(deltaX.pow(2.0F) + deltaY.pow(2.0F))
    }

    private fun touchMoved(event: MotionEvent) {
        val startX = currentStartX
        val startY = currentStartY
        val distance = distance(
            PointFloat(currentStartX, currentStartY),
            PointFloat(event.x, event.y)
        )

        Log.w("draw", "distance: $distance")

        if (distance == 0.0F) {
            return
        }

        val directionX = (event.x - currentStartX) / distance
        val directionY = (event.y - currentStartY) / distance

        strokes.add(Stroke(
            PointFloat(startX + directionX * distance / 2, startY + directionY * distance / 2),
            ArrayList())
        )

        for (i in 1..(distance / paintLine.strokeWidth).toInt()) {
            val newX = startX + directionX * paintLine.strokeWidth * i
            val newY = startY + directionY * paintLine.strokeWidth * i
            addSegment(newX, newY)
            currentStartX = newX
            currentStartY = newY
        }

        currentStroke.lineTo(currentStartX, currentStartY)
    }

    private fun addSegment(destX: Float, destY: Float) {
        sendStroke(currentStartX, destX, currentStartY, destY)

        val path = Path()
        path.moveTo(currentStartX, currentStartY)
        path.lineTo(destX, destY)

        val segment = Segment(path, Paint(paintLine), null, null)

        segments.add(segment)
        if (segments.size - 2 >= 0 && !strokeJustEnded) {
            // segments.size - 1 is the index of the segment we just added,
            // segments.size - 2 is the index of the segment just before it.
            segments[segments.size - 1].previousSegment = segments[segments.size - 2]
            segments[segments.size - 2].nextSegment = segments[segments.size - 1]
        }

        strokes[strokes.count() - 1].segments.add(segment)

        strokeJustEnded = false
    }

    private fun strokeReceived(obj: JSONObject) {
        val path = Path()
        path.moveTo(obj.getInt("startPosX").toFloat(), obj.getInt("startPosY").toFloat())
        path.lineTo(obj.getInt("endPosX").toFloat(), obj.getInt("endPosY").toFloat())

        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = obj.getInt("color")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = obj.getInt("width").toFloat()
        paint.strokeCap = Paint.Cap.ROUND

        segments.add(Segment(path, paint, null, null))
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
}


