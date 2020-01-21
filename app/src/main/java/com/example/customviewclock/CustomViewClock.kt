package com.example.customviewclock

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import java.lang.Exception
import java.util.*


class CustomViewClock: View {

    companion object {
        private val DEFAULT_STROKE_WIDTH = 10f
        private val DEFAULT_HOUR_HAND_WIDTH = 15f
        private val DEFAULT_MINUTE_HAND_WIDTH = 10f
        private val DEFAULT_SECOND_HAND_WIDTH = 5f
        private val DEFAULT_COLOR = Color.BLACK
        private val DEFAULT_IS_DRAW_HOUR_NUMBER = true
        private val DEFAULT_NUMBER_TEXT_SIZE = 0f
        private val DEFAULT_IS_DRAW_MINUTE = true
        private val DEFAULT_IS_DRAW_SECOND_HAND = true
        private val DEFAULT_IS_TICTAC = true

        private val HOUR_HAND_RATIO = 2.8f / 6f
        private val MINUTE_HAND_RATIO = 3.5f / 6f
        private val SECOND_HAND_RATIO = 4.3f / 6f
    }

    private var mStrokeWidth = DEFAULT_STROKE_WIDTH
    private var mStrokeColor = DEFAULT_COLOR
    private var mHourHandWidth = DEFAULT_HOUR_HAND_WIDTH
    private var mHourHandColor = DEFAULT_COLOR
    private var mMinuteHandWidth = DEFAULT_MINUTE_HAND_WIDTH
    private var mMinuteHandColor = DEFAULT_COLOR
    private var mSecondHandWidth = DEFAULT_SECOND_HAND_WIDTH
    private var mSecondHandColor = DEFAULT_COLOR
    private var mNumberTextSize = DEFAULT_NUMBER_TEXT_SIZE
    private var mNumberColor = DEFAULT_COLOR
    private var mIsDrawHourNumber = DEFAULT_IS_DRAW_HOUR_NUMBER
    private var mIsDrawMinute = DEFAULT_IS_DRAW_MINUTE
    private var mIsDrawSecondHand = DEFAULT_IS_DRAW_SECOND_HAND
    private var mTime: MyTime = MyTime.now()
    private var mIsTictac = DEFAULT_IS_TICTAC

    private var mOutlineCirclePaint = Paint()
    private var mNumberPaint = Paint()
    private var mMinutePaint = Paint()
    private var mHourHandPaint = Paint()
    private var mSecondHandPaint = Paint()
    private var mMinuteHandPaint = Paint()

    constructor(context: Context) : this(context, null) {
        initialize(context, null)
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initialize(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            drawOutlineCircle(canvas)
            drawClockwise(canvas)
            if (mIsDrawHourNumber) {
                drawNumber(canvas)
            }
            if (mIsDrawMinute) {
                drawMinute(canvas)
            }
        }
        super.onDraw(canvas)
    }

    fun getTime(): MyTime {
        return mTime
    }

    fun setTime(time: MyTime) {
        mTime = time
    }

    private fun drawClockwise(canvas: Canvas) {
        val radius = getRadius()
        val center = getCenter()
        val hourHandCoordinate = getCoordinates(mTime.toSeconds() / 3600f * 5f, HOUR_HAND_RATIO * radius)
        canvas.drawLine(center.first, center.second, hourHandCoordinate.first, hourHandCoordinate.second, mHourHandPaint)
        val minuteHandCoordinate = getCoordinates(mTime.minute + mTime.second / 60f, MINUTE_HAND_RATIO * radius)
        canvas.drawLine(center.first, center.second, minuteHandCoordinate.first, minuteHandCoordinate.second, mMinuteHandPaint)
        if (mIsDrawSecondHand) {
            val secondHandCoordinate = getCoordinates(mTime.second.toFloat(), SECOND_HAND_RATIO * radius)
            canvas.drawLine(center.first, center.second, secondHandCoordinate.first, secondHandCoordinate.second, mSecondHandPaint)
        }
    }

    private fun drawOutlineCircle(canvas: Canvas) {
        canvas.drawCircle(getCenter().first, getCenter().second, getRadius() - mStrokeWidth / 2, mOutlineCirclePaint)
        canvas.drawPoint(getCenter().first, getCenter().second, mOutlineCirclePaint)
    }

    private fun drawMinute(canvas: Canvas) {
        mMinutePaint.strokeWidth = Math.min(Math.max(3f, getRadius() / 1000f * 10f), 10f)
        for (i in 1..60) {
            val radius = getRadius()
            var startRatio = 5.6f
            if (i % 5 == 0)
                startRatio = 5.3f
            val (x1,y1) = getCoordinates(i * 1f, startRatio / 6f * radius)
            val (x2,y2) = getCoordinates(i * 1f, 5.9f / 6f * radius)
            canvas.drawLine(x1,y1,x2,y2, mMinutePaint)
        }
    }

    private fun drawNumber(canvas: Canvas) {
        var textSize = mNumberTextSize
        if (textSize == 0f) textSize = getRadius() / 5f
        mNumberPaint.textSize = textSize
        for (i in 1..12) {
            val radius = getRadius()
            val (x, y) = getCoordinates(i * 5f, 4.8f / 6f * radius)
            val bounds = Rect()
            mNumberPaint.getTextBounds(i.toString(), 0, i.toString().length, bounds)
            canvas.drawText(
                i.toString(),
                x - bounds.width() / 2f,
                y + bounds.height() / 2f,
                mNumberPaint
            )
        }
    }

    private fun getCoordinates(seconds: Float, r: Float, c: Pair<Float,Float> = getCenter()): Pair<Float, Float> {
        var angle = Math.max(seconds, 0.001f) * 2 * Math.PI / 60f
        val y = r * (1 - Math.cos(angle))
        val x = y / Math.tan(angle / 2)
        return Pair(c.first + x.toFloat(), c.second - r + y.toFloat())
    }

    private fun getRadius() = Math.min(measuredWidth, measuredHeight) / 2f
    private fun getCenter() = Pair(measuredWidth / 2f, measuredHeight / 2f)

    private fun initializePaint() {
        mOutlineCirclePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mStrokeWidth
            color = mStrokeColor
        }
        mNumberPaint.apply {
            color = mNumberColor
        }
        mHourHandPaint.apply {
            strokeWidth = mHourHandWidth
            color = mHourHandColor
        }
        mSecondHandPaint.apply {
            strokeWidth = mSecondHandWidth
            color = mSecondHandColor
        }
        mMinuteHandPaint.apply {
            strokeWidth = mMinuteHandWidth
            color = mMinuteHandColor
        }
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray: TypedArray = context
                .obtainStyledAttributes(attrs, R.styleable.CustomViewClock)

            mStrokeWidth = typedArray.getFloat(R.styleable.CustomViewClock_stroke_width, DEFAULT_STROKE_WIDTH)
            mStrokeColor = typedArray.getColor(R.styleable.CustomViewClock_stroke_color, DEFAULT_COLOR)
            mHourHandWidth = typedArray.getFloat(R.styleable.CustomViewClock_hour_hand_width, DEFAULT_HOUR_HAND_WIDTH)
            mHourHandColor = typedArray.getColor(R.styleable.CustomViewClock_hour_hand_color, DEFAULT_COLOR)
            mMinuteHandWidth = typedArray.getFloat(R.styleable.CustomViewClock_minute_hand_width, DEFAULT_MINUTE_HAND_WIDTH)
            mMinuteHandColor = typedArray.getColor(R.styleable.CustomViewClock_minute_hand_color, DEFAULT_COLOR)
            mSecondHandWidth = typedArray.getFloat(R.styleable.CustomViewClock_second_hand_width, DEFAULT_SECOND_HAND_WIDTH)
            mSecondHandColor = typedArray.getColor(R.styleable.CustomViewClock_second_hand_color, DEFAULT_COLOR)
            mNumberTextSize = typedArray.getDimension(R.styleable.CustomViewClock_number_text_size, DEFAULT_NUMBER_TEXT_SIZE)
            mNumberColor = typedArray.getColor(R.styleable.CustomViewClock_number_color, DEFAULT_COLOR)
            mIsDrawHourNumber = typedArray.getBoolean(R.styleable.CustomViewClock_is_draw_hour_number, DEFAULT_IS_DRAW_HOUR_NUMBER)
            mIsDrawMinute = typedArray.getBoolean(R.styleable.CustomViewClock_is_draw_minute, DEFAULT_IS_DRAW_MINUTE)
            mIsDrawSecondHand = typedArray.getBoolean(R.styleable.CustomViewClock_is_draw_second_hand, DEFAULT_IS_DRAW_SECOND_HAND)
            val times = typedArray.getString(R.styleable.CustomViewClock_time)
            if (times != null)
                mTime = MyTime.parse(times)
            mIsTictac = typedArray.getBoolean(R.styleable.CustomViewClock_is_tictac, DEFAULT_IS_TICTAC)
        }
        initializePaint()
    }

    data class MyTime(var hour: Int, var minute: Int, var second: Int) {
        companion object {
            fun now(): MyTime {
                val time = Calendar.getInstance().time
                return MyTime(time.hours, time.minutes, time.seconds)
            }

            fun parse(s: String): MyTime {
                try {
                    val times = s.split(":")
                    return MyTime(times[0].toInt(), times[1].toInt(), times[2].toInt())
                } catch (e: Exception) {
                    return now()
                }
            }
        }

        fun setTime(seconds: Int) {
            hour = seconds / 3600
            minute = (seconds % 3600) / 60
            second = (seconds % 3600) % 60
        }

        fun toSeconds(): Int {
            return hour * 3600 + minute * 60 + second
        }

        fun add(seconds: Int) {
            setTime((toSeconds() + seconds) % 86400)
        }
    }
}