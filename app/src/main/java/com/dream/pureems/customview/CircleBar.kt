package com.dream.pureems.customview

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Cap
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.dream.pureems.R
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.roundToInt
import com.dream.pureems.ViewUtils.dp2px

class CircleBar @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(mContext, attrs, defStyleAttr) {
    private val mArcColor: Int
    private var mArcWidth: Int
    private val mCenterTextColor: Int
    private val mCenterTextSize: Int
    private var mCircleRadius: Int
    private var arcPaint: Paint? = null
    private var arcCirclePaint: Paint? = null
    private var centerTextPaint: Paint? = null
    private var arcRectF: RectF? = null
    private var textBoundRect: Rect? = null
    private var mCurData = 0.0f
    private var isPercentData = true
    private val arcStartColor: Int
    private val arcEndColor: Int
    private var startCirclePaint: Paint? = null
    private val myFormatter = DecimalFormat("####.00")
    private fun initPaint() {
        startCirclePaint = Paint(1)
        startCirclePaint!!.style = Paint.Style.FILL
        startCirclePaint!!.color = arcStartColor
        arcCirclePaint = Paint(1)
        arcCirclePaint!!.style = Paint.Style.STROKE
        arcCirclePaint!!.strokeWidth = mArcWidth.toFloat()
        arcCirclePaint!!.color = ContextCompat.getColor(mContext, R.color.colorCirclebg)
        arcCirclePaint!!.strokeCap = Cap.ROUND
        arcPaint = Paint(1)
        arcPaint!!.style = Paint.Style.STROKE
        arcPaint!!.strokeWidth = mArcWidth.toFloat()
        arcPaint!!.color = mArcColor
        arcPaint!!.strokeCap = Cap.ROUND
        centerTextPaint = Paint(1)
        centerTextPaint!!.style = Paint.Style.FILL
        centerTextPaint!!.color = mCenterTextColor
        centerTextPaint!!.textSize = mCenterTextSize.toFloat()
        arcRectF = RectF()
        textBoundRect = Rect()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        val size = if (width > height) {
            height
        } else {
            width
        }
        if (mCircleRadius > size / 2) {
            mCircleRadius = size / 2
        }
        if (mArcWidth > size / 7) {
            mArcWidth = size / 7
            arcCirclePaint!!.strokeWidth = mArcWidth.toFloat()
            arcPaint!!.strokeWidth = mArcWidth.toFloat()
        }
        setMeasuredDimension(
            measureDimension(size),
            measureDimension(size)
        )
    }

    private fun measureDimension(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        var result: Int
        if (specMode == 1073741824) {
            result = specSize
        } else {
            result = mCircleRadius * 2
            if (specMode == -2147483648) {
                result = result.coerceAtMost(specSize)
            }
        }
        return result
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        canvas.rotate(-90.0f, (width / 2).toFloat(), (height / 2).toFloat())
        arcRectF!![(width / 2 - mCircleRadius + mArcWidth / 2).toFloat(), (height / 2 - mCircleRadius + mArcWidth / 2).toFloat(), (width / 2 + mCircleRadius - mArcWidth / 2).toFloat()] =
            (height / 2 + mCircleRadius - mArcWidth / 2).toFloat()
        canvas.drawArc(arcRectF!!, 0.0f, 360.0f, false, arcCirclePaint!!)
        arcPaint!!.shader = SweepGradient(
            (width / 2).toFloat(),
            (height / 2).toFloat(), arcStartColor, arcEndColor
        )
        canvas.drawArc(
            arcRectF!!, 0.0f, 360.0f * mCurData / 100.0f, false,
            arcPaint!!
        )
        canvas.rotate(90.0f, (width / 2).toFloat(), (height / 2).toFloat())
        canvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2 - mCircleRadius + mArcWidth / 2).toFloat(),
            (mArcWidth / 2).toFloat(), startCirclePaint!!
        )
        val data = if (isPercentData) {
            myFormatter.format(mCurData)
        } else {
            myFormatter.format(mCurData / 25F)
        }
        centerTextPaint!!.getTextBounds(data, 0, data.length, textBoundRect)
        canvas.drawText(
            data,
            (width / 2 - textBoundRect!!.width() / 2).toFloat(),
            (height / 2 + textBoundRect!!.height() / 2).toFloat(), centerTextPaint!!
        )
    }

    private fun dipToPx(dp: Float): Int {
        //获得当前手机dp与px的转换关系
        val scale: Float = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun setPercentData(data: Float, interpolator: TimeInterpolator?, isPrecent: Boolean) {
        var temp = data
        if (!isPrecent) {
            temp *= 25F
        }
        val valueAnimator = ValueAnimator.ofFloat(mCurData, temp)
        valueAnimator.duration = (abs(mCurData - temp) * 30).toLong()
        valueAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            mCurData = (value * 10.0f).roundToInt().toFloat() / 10.0f
            invalidate()
        }
        valueAnimator.interpolator = interpolator
        valueAnimator.start()
        isPercentData = isPrecent
    }

    init {
        val typedArray =
            mContext.obtainStyledAttributes(attrs, R.styleable.CircleBar, defStyleAttr, 0)
        mArcColor = typedArray.getColor(R.styleable.CircleBar_arcColor, 16711680)
        mArcWidth = typedArray.getDimensionPixelSize(
            R.styleable.CircleBar_arcWidth, dp2px(
                mContext, 20.0f
            )
        )
        mCenterTextColor = typedArray.getColor(R.styleable.CircleBar_centerTextColor, 255)
        mCenterTextSize = typedArray.getDimensionPixelSize(
            R.styleable.CircleBar_centerTextSize, dp2px(
                mContext, 20.0f
            )
        )
        mCircleRadius = typedArray.getDimensionPixelSize(
            R.styleable.CircleBar_circleRadius, dp2px(
                mContext, 100.0f
            )
        )
        arcStartColor = typedArray.getColor(
            R.styleable.CircleBar_arcStartColor, ContextCompat.getColor(
                mContext, R.color.colorStart
            )
        )
        arcEndColor = typedArray.getColor(
            R.styleable.CircleBar_arcEndColor, ContextCompat.getColor(
                mContext, R.color.colorEnd
            )
        )
        typedArray.recycle()
        initPaint()
    }


}