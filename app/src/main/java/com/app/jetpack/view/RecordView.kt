package com.app.jetpack.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.app.jetpack.R
import com.app.lib_common.ext.dp

class RecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr),
    View.OnClickListener, View.OnLongClickListener {
    companion object {
        private const val PROGRESS_INTERVAL = 100L
    }

    private val mRadius: Int
    private val mProgressWidth: Int
    private val mProgressColor: Int
    private val mFillColor: Int
    private val mMaxDuration: Int

    private lateinit var mFillPaint: Paint
    private lateinit var mProgressPaint: Paint

    private val mHandler = MyHandler(this)
    private var mMaxProgressValue = 0
    private var mProgressValue = 0
    private var mIsRecording = false
    private var mStartRecordTime = 0L

    private var mListener: OnRecordListener? = null


    init {
        context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes).apply {
            mRadius = getDimensionPixelOffset(R.styleable.RecordView_radius, 0)
            mProgressWidth = getDimensionPixelOffset(R.styleable.RecordView_progress_width, 3.dp)
            mProgressColor = getColor(R.styleable.RecordView_progress_color, Color.RED)
            mFillColor = getColor(R.styleable.RecordView_fill_color, Color.WHITE)
            mMaxDuration = getInteger(R.styleable.RecordView_duration, 10)
            recycle()
        }

        mMaxProgressValue = (mMaxDuration * 1000 / PROGRESS_INTERVAL).toInt()
        initPaint()
        initTouchListener()
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    private fun initPaint() {
        mFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mFillColor
            style = Paint.Style.FILL
        }
        mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mProgressColor
            style = Paint.Style.STROKE
            strokeWidth = mProgressWidth.toFloat()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchListener() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsRecording = true
                    mStartRecordTime = System.currentTimeMillis()
                    mHandler.sendEmptyMessage(0)
                }

                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    if (now - mStartRecordTime > ViewConfiguration.getLongPressTimeout()) {
                        finishRecord()
                    }
                    mHandler.removeCallbacksAndMessages(null)
                    mIsRecording = false
                    mStartRecordTime = 0
                    mProgressValue = 0
                    postInvalidate()
                }
            }
            false
        }
    }

    private fun finishRecord() {
        mListener?.onFinish()
    }

    override fun onClick(v: View?) {
        mListener?.onClick()
    }

    override fun onLongClick(v: View?): Boolean {
        mListener?.onLongClick()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mIsRecording) {
            canvas?.drawCircle(width / 2f, height / 2f, width / 2f, mFillPaint)
            val sweepAngle = (mProgressValue * 1f / mMaxProgressValue) * 360
            canvas?.drawArc(
                mProgressWidth / 2f,
                mProgressWidth / 2f,
                width.toFloat() - mProgressWidth / 2,
                height.toFloat() - mProgressWidth / 2,
                -90f,
                sweepAngle,
                false,
                mProgressPaint
            )
        } else {
            canvas?.drawCircle(width / 2f, height / 2f, mRadius.toFloat(), mFillPaint)
        }
    }

    private class MyHandler(private val view: RecordView) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            view.updateValue()
        }
    }

    private fun updateValue() {
        mProgressValue++
        postInvalidate()
        if (mProgressValue <= mMaxProgressValue) {
            mHandler.sendEmptyMessageDelayed(0, PROGRESS_INTERVAL)
        } else {
            finishRecord()
        }
    }

    fun setOnRecordListener(listener: OnRecordListener) {
        mListener = listener
    }

    interface OnRecordListener {
        fun onClick()
        fun onLongClick()
        fun onFinish()
    }
}