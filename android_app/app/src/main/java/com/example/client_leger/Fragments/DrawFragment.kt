package com.example.client_leger.Fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
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
import kotlinx.android.synthetic.main.fragment_draw.view.*
import org.json.JSONObject
import yuku.ambilwarna.AmbilWarnaDialog

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
        val popup = AlertDialog.Builder(v.context)
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

class DrawCanvas(ctx: Context, attr: AttributeSet?, private var username: String) : View(ctx, attr) {
    var paintLine: Paint = Paint()
    var isStrokeErasing = false
    var isNormalErasing = false
    private var currentPath = Path()
    private var currentStartX = 0f
    private var currentStartY = 0f
    private val segments = ArrayList<Segment>()

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

    override fun onDraw(canvas: Canvas) {
        for (segment in segments) {
            canvas.drawPath(segment.path, segment.paint)
        }

        canvas.drawPath(currentPath, paintLine)

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked

        if (isStrokeErasing || isNormalErasing) {
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

        for (segment in segments) {
            if (segment.paint.color == Color.TRANSPARENT) {
                continue
            }

            val pm = PathMeasure(segment.path, false)

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
                        segment.paint.color = Color.TRANSPARENT
                        //TODO: inform server of stroke removal
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

        val newSegment = Path()
        newSegment.moveTo(currentStartX, currentStartY)
        newSegment.moveTo(event.x, event.y)

        segments.add(Segment(newSegment, Paint(paintLine), null, null))

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

    private fun touchEnded() {
        //if (!isStrokeErasing) { //TODO: check needed?
        //    val paint = Paint(paintLine)
        //    segments.add(Segment(Path(currentPath), paint, null, null))
        //}

        currentPath.reset()
    }

    private fun touchStarted(x: Float, y: Float) {
        currentStartX = x
        currentStartY = y
        currentPath.moveTo(x, y)
    }
}


