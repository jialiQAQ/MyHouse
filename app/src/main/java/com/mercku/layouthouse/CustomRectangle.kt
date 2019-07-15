package com.mercku.layouthouse

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Created by yanqiong.ran on 2019-07-11.
 */
import android.view.MotionEvent

class CustomRectangle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    lateinit private var mContext: Context
    private var mWidth = 0f
    private var mHeight = 0f
    private var currentX: Float = 0.toFloat()
    private var currentY: Float = 0.toFloat()
    private var downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()

    internal var currentNEAR = NONE_POINT

    lateinit private var mPaint: Paint

    private var oval: RectF? = null

    private var NEAR = 0f

    internal annotation class TouchNear


/*    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        initView()
    }*/

    init {
        mPaint = Paint()
        mPaint.color = Color.blue(88)
        mPaint.strokeWidth = 10f
        mPaint.isAntiAlias = true

        mPaint.style = Paint.Style.STROKE

        mWidth = mContext.resources?.displayMetrics?.widthPixels!!.toFloat()

        mHeight = mContext.resources?.displayMetrics?.heightPixels!!.toFloat()

        oval = RectF()
        oval!!.set(mWidth / 2 - 100, mHeight / 2 - 100, mWidth / 2 + 100, mHeight / 2 + 100) // first ui

    }

    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        NEAR = Math.min(mWidth, mHeight) / 10
        // oval = RectF()
        //oval!!.set(mWidth / 2 - 100, mHeight / 2 - 100, mWidth / 2 + 100, mHeight / 2 + 100) // first ui

    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(oval!!, mPaint!!)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.currentX = event.x
        this.currentY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.downX = event.x
                this.downY = event.y
                currentNEAR = checkNear()
                android.util.Log.w("RYQ", "currentNEAR===> $currentNEAR")
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentNEAR == NONE_POINT) {
                    // do move...
                    val canMove = canMove()
                    // LogUtils.e("canMove? $canMove")
                    val dx = currentX - downX
                    val dy = currentY - downY
                    //LogUtils.w("dx=$dx , dy=$dy")
                    /*val newL = roundLength(oval!!.left + dx, mWidth)
                    val newR = roundLength(oval!!.right + dx, mWidth)
                    val newT = roundLength(oval!!.top + dy, mHeight)
                    val newB = roundLength(oval!!.bottom + dy, mHeight)*/
                    val newL = roundLength(oval!!.left + dx, mWidth)
                    val newR = roundLength(oval!!.right + dx, mWidth)
                    val newT = roundLength(oval!!.top + dy, mHeight)
                    val newB = roundLength(oval!!.bottom + dy, mHeight)

                    when (canMove) {
                        MOVE_H -> {
                            if (!distortionInMove(oval!!, newL, oval!!.top, newR, oval!!.bottom)) {
                                oval!!.set(newL, oval!!.top, newR, oval!!.bottom)
                            }
                            downX = currentX
                            downY = currentY
                        }
                        MOVE_V -> {
                            if (!distortionInMove(oval!!, oval!!.left, newT, oval!!.right, newB)) {
                                oval!!.set(oval!!.left, newT, oval!!.right, newB)
                            }
                            downX = currentX
                            downY = currentY
                        }
                        MOVE_VH -> {
                            //                            oval.inset(dx, dy);
                            if (!distortionInMove(oval!!, newL, newT, newR, newB)) {
                                oval!!.set(newL, newT, newR, newB)
                            }
                            downX = currentX
                            downY = currentY
                        }
                        MOVE_ERROR -> {
                        }
                    }
                } else {
                    // do drag crop
                    currentX = roundLength(currentX, mWidth)
                    currentY = roundLength(currentY, mHeight)
                    when (currentNEAR) {
                        LEFT_TOP -> oval!!.set(currentX, currentY, oval!!.right, oval!!.bottom)
                        LEFT_BOTTOM -> oval!!.set(currentX, oval!!.top, oval!!.right, currentY)
                        RIGHT_TOP -> oval!!.set(oval!!.left, currentY, currentX, oval!!.bottom)
                        RIGHT_BOTTOM -> oval!!.set(oval!!.left, oval!!.top, currentX, currentY)
                    }
                }
                postInvalidate() // update ui
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }

    /**
     * 移动的时候是否变形了
     */
    private fun distortionInMove(oval: RectF, cL: Float, cT: Float, cR: Float, cB: Float): Boolean {
        return Math.abs(cR - cL - (oval.right - oval.left)) > 0.001 || Math.abs(cB - cT - (oval.bottom - oval.top)) > 0.001
    }

    private fun roundLength(w: Float, max: Float): Float {
        return if (w < 0) {
            0f
        } else if (w > max) {
            max
        } else {
            w
        }
    }

    @TouchNear
    private fun checkNear(): Int {

        val nearLT = near(currentX, currentY, oval!!.left, oval!!.top)
        if (nearLT) {
            return LEFT_TOP
        }
        val nearLB = near(currentX, currentY, oval!!.left, oval!!.bottom)
        if (nearLB) {
            return LEFT_BOTTOM
        }
        val nearRT = near(currentX, currentY, oval!!.right, oval!!.top)
        if (nearRT) {
            return RIGHT_TOP
        }
        val nearRB = near(currentX, currentY, oval!!.right, oval!!.bottom)
        return if (nearRB) {
            RIGHT_BOTTOM
        } else NONE_POINT
    }


    /**
     * when can move?
     * if the oval is not the max,then can move
     *
     * @return
     */
    internal fun canMove(): Int {
        if (touchEdge()) {
            return MOVE_ERROR
        }
        if (!oval!!.contains(currentX, currentY)) {
            return MOVE_ERROR
        }
        return if (oval!!.right - oval!!.left <= 0 && oval!!.bottom - oval!!.top <= 0) {
            MOVE_ERROR
        } else {
            MOVE_VH
        }
        /*return if (oval!!.right - oval!!.left == mWidth && oval!!.bottom - oval!!.top == mHeight) {
            MOVE_ERROR
        } else if (oval!!.right - oval!!.left == mWidth && oval!!.bottom - oval!!.top != mHeight) {
            MOVE_V
        } else if (oval!!.right - oval!!.left != mWidth && oval!!.bottom - oval!!.top == mHeight) {
            MOVE_H
        } else {
            MOVE_VH
        }*/
    }

    /**
     * 超出边界
     *
     * @return true, false
     */
    internal fun touchEdge(): Boolean {
        return (oval!!.left < 0 || oval!!.right > mWidth
                || oval!!.top < 0 || oval!!.bottom > mHeight)
    }

    internal fun near(one: PointF, other: PointF): Boolean {
        val dx = Math.abs(one.x - other.x)
        val dy = Math.abs(one.y - other.y)
        return Math.pow((dx * dx + dy * dy).toDouble(), 0.5) <= NEAR
    }

    internal fun near(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val dx = Math.abs(x1 - x2)
        val dy = Math.abs(y1 - y2)
        return Math.pow((dx * dx + dy * dy).toDouble(), 0.5) <= NEAR
    }

    companion object {


        val NONE_POINT = 0
        val LEFT_TOP = 1
        val RIGHT_TOP = 1 + 1
        val RIGHT_BOTTOM = 1 + 1 + 1
        val LEFT_BOTTOM = 1 + 1 + 1 + 1

        val MOVE_ERROR = -1024
        val MOVE_H = 90
        val MOVE_V = 90 + 1
        val MOVE_VH = 90 + 1 + 1
    }
}