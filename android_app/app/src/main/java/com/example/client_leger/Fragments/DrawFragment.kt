package com.example.client_leger.Fragments

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.util.Log
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

        v.addView(DrawCanvas(activity!!.applicationContext, null, this.activity!!.intent.getStringExtra("username")))

        return v
    }

    private fun switchStrokeEraser(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(2) as DrawCanvas
        drawCanvasView.isErasemode = !drawCanvasView.isErasemode
    }

    private fun openColorPicker(v: ViewGroup) {
        val drawCanvasView = v.getChildAt(2) as DrawCanvas
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
    var isErasemode = false

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private var currentPath = Path()
    private var currentStartX = 0f
    private var currentStartY = 0f
    private val moveTolerence = 5f
    private val strokes = ArrayList<Stroke>()
    private var paintScreen: Paint = Paint()


    init {
        paintLine.isAntiAlias = true
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 7.0F
        paintLine.strokeCap = Paint.Cap.ROUND
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

        canvas.drawPath(currentPath, paintLine)

        repeat(strokes.count()) {
            canvas.drawPath(strokes[it].path, paintLine)
        }
    }

    private fun removeStroke(index: Int) {
        strokes.removeAt(index)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked


/*
int x, y;
Path tempPath = new Path(); // Create temp Path
tempPath.moveTo(x,y); // Move cursor to point
RectF rectangle = new RectF(x-1, y-1, x+1, y+1); // create rectangle with size 2xp
tempPath.addRect(rectangle, Path.Direction.CW); // add rect to temp path
tempPath.op(pathToDetect, Path.Op.DIFFERENCE); // get difference with our PathToCheck
if (tempPath.isEmpty()) // if out path cover temp path we get empty path in result
{
    Log.d(TAG, "Path contains this point");
    return true;
}
else
{
    Log.d(TAG, "Path don't contains this point");
    return false;
}
*/

        if (isErasemode) {
            for (i in 0 until strokes.size) {
                val r = RectF()
                val pComp = Point(event.x.toInt(), event.y.toInt())
                val mPath: Path = strokes[i].path
                mPath.computeBounds(r, false)
                Log.d("erase", "r.height: " + r.height())
                Log.d("erase", "r.width: " + r.width())
                Log.d("erase", "r.top: " + r.top)
                Log.d("erase", "r.bottom: " + r.bottom)
                Log.d("erase", "r.left: " + r.left)
                Log.d("erase", "r.right: " + r.right)
                if (r.contains(pComp.x.toFloat(), pComp.y.toFloat())) {

                    Log.d("erase", "FOUND!")
                    removeStroke(i)
                    break
                }
            }
            return false
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

    private fun touchMoved(event: MotionEvent) {
        if ( kotlin.math.abs(event.x - currentStartX) >= moveTolerence &&
             kotlin.math.abs(event.y - currentStartY) >= moveTolerence ) {

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

    private fun touchEnded() {
        strokes.add(Stroke(currentPath, paintLine))

        bitmapCanvas.drawPath(currentPath, paintLine)
        currentPath.reset()
    }

    private fun touchStarted(x: Float, y: Float) {
        currentStartX = x
        currentStartY = y

        //TODO: currentPath = Path() ???

        currentPath.moveTo(x, y)
    }
}


