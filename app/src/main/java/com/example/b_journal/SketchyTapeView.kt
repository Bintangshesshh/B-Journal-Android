package com.example.b_journal

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import kotlin.random.Random

class SketchyTapeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val sketchyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.MITER
    }

    private val hatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33000000") // Hitam transparan buat arsiran
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val hardShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#558A8A85")
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val extraPath = Path()
    private val shadowPath = Path()
    private val random = Random(42) // Lock seed biar gak kedip pas diketik

    init {
        setWillNotDraw(false)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) {
            val child = getChildAt(0)
            val left = paddingLeft + 20
            val top = paddingTop + 20
            child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount > 0) {
            val child = getChildAt(0)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val extraWidth = child.measuredWidth + paddingLeft + paddingRight + 60
            val extraHeight = child.measuredHeight + paddingTop + paddingBottom + 60
            setMeasuredDimension(resolveSize(extraWidth, widthMeasureSpec), resolveSize(extraHeight, heightMeasureSpec))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (childCount == 0) return

        val child = getChildAt(0)
        val l = child.left.toFloat()
        val t = child.top.toFloat()
        val r = child.right.toFloat()
        val b = child.bottom.toFloat()
        val offsetShadow = 20f

        shadowPath.reset()
        makeSuperSketchyRect(shadowPath, l + offsetShadow, t + offsetShadow, r + offsetShadow, b + offsetShadow, 3f)
        canvas.drawPath(shadowPath, hardShadowPaint)

        drawCrossHatch(canvas, l, t, r, b)

        path.reset()
        makeSuperSketchyRect(path, l, t, r, b, 6f)
        canvas.drawPath(path, sketchyBorderPaint)

        extraPath.reset()
        makeSuperSketchyRect(extraPath, l - 2f, t + 2f, r + 2f, b - 2f, 5f)
        canvas.drawPath(extraPath, sketchyBorderPaint)

        drawTornTape(canvas, l + 30f, t, -20f)
        drawTornTape(canvas, r - 50f, b, 25f)
    }

    private fun makeSuperSketchyRect(p: Path, left: Float, top: Float, right: Float, bottom: Float, maxDev: Float) {
        val segments = 16
        p.moveTo(left, top)

        // Atas
        for (i in 1..segments) {
            val x = left + (right - left) * i / segments
            val y = top + (random.nextFloat() * maxDev * 2 - maxDev) + Math.sin(i.toDouble() * 0.8).toFloat() * 3f
            p.lineTo(x, y)
        }
        // Kanan
        for (i in 1..segments) {
            val x = right + (random.nextFloat() * maxDev * 2 - maxDev) + Math.cos(i.toDouble() * 0.8).toFloat() * 3f
            val y = top + (bottom - top) * i / segments
            p.lineTo(x, y)
        }
        // Bawah
        for (i in 1..segments) {
            val x = right - (right - left) * i / segments
            val y = bottom + (random.nextFloat() * maxDev * 2 - maxDev) + Math.sin(i.toDouble() * 0.8).toFloat() * 3f
            p.lineTo(x, y)
        }
        // Kiri
        for (i in 1..segments) {
            val x = left + (random.nextFloat() * maxDev * 2 - maxDev) + Math.cos(i.toDouble() * 0.8).toFloat() * 3f
            val y = bottom - (bottom - top) * i / segments
            p.lineTo(x, y)
        }
        p.close()
    }

    private fun drawCrossHatch(canvas: Canvas, l: Float, t: Float, r: Float, b: Float) {
        val interval = 18f
        var i = l - (b - t)
        while (i < r) {
            canvas.drawLine(i, t, i + (b - t), b, hatchPaint)
            i += interval
        }
    }

    private fun drawTornTape(canvas: Canvas, cx: Float, cy: Float, rotation: Float) {
        canvas.save()
        canvas.rotate(rotation, cx, cy)

        val tw = 80f
        val th = 30f
        val tapePath = Path()

        val left = cx - tw / 2
        val right = cx + tw / 2
        val top = cy - th / 2
        val bottom = cy + th / 2

        tapePath.moveTo(left, top)
        tapePath.lineTo(right, top)

        var currY = top
        while (currY < bottom) {
            currY += 5f
            val stepX = if (random.nextBoolean()) right - 6f else right
            tapePath.lineTo(stepX, currY)
        }

        tapePath.lineTo(left, bottom)

        while (currY > top) {
            currY -= 5f
            val stepX = if (random.nextBoolean()) left + 6f else left
            tapePath.lineTo(stepX, currY)
        }

        tapePath.close()
        canvas.drawPath(tapePath, tapePaint)
        canvas.restore()
    }
}