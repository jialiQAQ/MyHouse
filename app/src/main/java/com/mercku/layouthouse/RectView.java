package com.mercku.layouthouse;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yanqiong.ran on 2019-07-11.
 */
public class RectView extends View {

    public static final int NONE_SCALE = -1;
    public static final int NONE_POINT = 0;
    public static final int LEFT_TOP = 1;
    public static final int RIGHT_TOP = 1 + 1;
    public static final int RIGHT_BOTTOM = 1 + 1 + 1;
    public static final int LEFT_BOTTOM = 1 + 1 + 1 + 1;
    private Context mContext;
    private float currentX;
    private float currentY;
    private float downX;
    private float downY;
    private int mWidth;
    private int mHeight;

    private static final String TAG = "ryq-RectView";

    @interface TouchNear {
    }

    public static final int MOVE_ERROR = -1024;
    public static final int MOVE_H = 90;
    public static final int MOVE_V = 90 + 1;
    public static final int MOVE_VH = 90 + 1 + 1;

    @interface MoveDirection {
    }


    @TouchNear
    int currentNEAR = NONE_POINT;

    private Paint mPaint;
    private RectF mRect;

    private float NEAR = 0;

    public RectView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public RectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RectView(Context context, @Nullable AttributeSet attrs,
                    int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initView();
    }

    private void initView() {
        mWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mHeight = mContext.getResources().getDisplayMetrics().heightPixels;


        NEAR = Math.min(mWidth, mHeight) / 10;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//用于防止边缘的锯齿
        mPaint.setColor(Color.BLACK);//设置颜色
        mPaint.setStyle(Paint.Style.STROKE);//设置样式为空心矩形
        mPaint.setStrokeWidth(12f);//设置空心矩形边框的宽度
        mPaint.setAlpha(1000);//设置透明度

        mRect = new RectF();
        mRect.set(0, 0, 200, 200);
        android.util.Log.d(TAG, "initView  mWidth=" + mWidth + " mHeight=" + mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        android.util.Log.d(TAG, "onSizeChanged  w=" + w + " h=" + h + " oldw=" + oldw + " oldh=" + oldh);
        // mRect.set(mWidth/2-100, mHeight/2-100, mWidth/2+100, mHeight/2+100); // first ui
        //mRect.set(w/2-100, h/2-100, w/2+100, h/2+100);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        android.util.Log.d(TAG, "onMeasure  widthMeasureSpec=" + widthMeasureSpec + " heightMeasureSpec=" + heightMeasureSpec);
        android.util.Log.d(TAG, "onMeasure  getWidth()=" + getWidth() + " getHeight()=" + getHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        android.util.Log.d(TAG, "onLayout  changed=" + changed + " left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        android.util.Log.d(TAG, "onDraw  left=" + mRect.left + " right=" + mRect.right + " top=" + mRect.top + " bottom=" + mRect.bottom);
        canvas.drawRect(mRect, mPaint);
        //canvas.drawRect(new Rect(100,300,400,600),mPaint);//绘制矩形，并设置矩形框显示的位置
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.currentX = event.getX();
        this.currentY = event.getY();
        Log.d(TAG, "onTouchEvent event.getAction()===> " +
                event.getAction() + " currentX=" + currentX
                + " currentY=" + currentY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.downX = event.getX();
                this.downY = event.getY();
                currentNEAR = checkNear();
                Log.d(TAG, "curXrentNEAR===> " + currentNEAR);
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentNEAR == NONE_POINT) {
                    // do move...
                    int canMove = MOVE_VH;//canMove();
                    Log.d(TAG, "onTouchEvent canMove?===> " + canMove());

                    float dx = currentX - downX;
                    float dy = currentY - downY;
                    Log.d(TAG, "onTouchEvent dx=" + dx + " , dy=" + dy);
                    float newL = mRect.left + dx;//roundLength(mRect.left + dx, mWidth);
                    float newR = mRect.right + dx;//roundLength(mRect.right + dx, mWidth);
                    float newT = mRect.top + dy;//roundLength(mRect.top + dy, mHeight);
                    float newB = mRect.bottom + dy;//roundLength(mRect.bottom + dy, mHeight);
                    Log.d(TAG, "currentNEAR===> " + currentNEAR);
                    switch (canMove) {
                        case MOVE_H:
                            if (!distortionInMove(mRect, newL, mRect.top, newR, mRect.bottom)) {
                                mRect.set(newL, mRect.top, newR, mRect.bottom);
                            }
                            downX = currentX;
                            downY = currentY;
                            break;
                        case MOVE_V:
                            if (!distortionInMove(mRect, mRect.left, newT, mRect.right, newB)) {
                                mRect.set(mRect.left, newT, mRect.right, newB);
                            }
                            downX = currentX;
                            downY = currentY;
                            break;
                        case MOVE_VH:
//                            mRect.inset(dx, dy);
                            if (!distortionInMove(mRect, newL, newT, newR, newB)) {
                                mRect.set(newL, newT, newR, newB);
                            }
                            downX = currentX;
                            downY = currentY;
                            break;
                        case MOVE_ERROR:
                            break;
                    }
                } else {
                    // do drag crop
                    //currentX = roundLength(currentX, mWidth);
                    //currentY = roundLength(currentY, mHeight);
                    switch (currentNEAR) {
                        case LEFT_TOP:
                            mRect.set(currentX, currentY, mRect.right, mRect.bottom);
                            break;
                        case LEFT_BOTTOM:
                            mRect.set(currentX, mRect.top, mRect.right, currentY);
                            break;
                        case RIGHT_TOP:
                            mRect.set(mRect.left, currentY, currentX, mRect.bottom);
                            break;
                        case RIGHT_BOTTOM:
                            mRect.set(mRect.left, mRect.top, currentX, currentY);
                            break;
                    }
                }
                postInvalidate(); // update ui
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * 移动的时候是否变形了
     */
    private boolean distortionInMove(RectF mRect, float cL, float cT, float cR, float cB) {
        return Math.abs((cR - cL) - (mRect.right - mRect.left)) > 0.001
                || Math.abs((cB - cT) - (mRect.bottom - mRect.top)) > 0.001;
    }

    private float roundLength(float w, float max) {
        if (w < 0) {
            return 0;
        } /*else if (w > max) {
            return max;
        } */ else {
            return w;
        }
    }

    @TouchNear
    private int checkNear() {

        boolean nearLT = near(currentX, currentY, mRect.left, mRect.top);
        if (nearLT) {
            return LEFT_TOP;
        }
        boolean nearLB = near(currentX, currentY, mRect.left, mRect.bottom);
        if (nearLB) {
            return LEFT_BOTTOM;
        }
        boolean nearRT = near(currentX, currentY, mRect.right, mRect.top);
        if (nearRT) {
            return RIGHT_TOP;
        }
        boolean nearRB = near(currentX, currentY, mRect.right, mRect.bottom);
        if (nearRB) {
            return RIGHT_BOTTOM;
        }
        return NONE_POINT;
    }


    /**
     * when can move?
     * if the mRect is not the max,then can move
     *
     * @return
     */
    @MoveDirection
    int canMove() {
        /*if (touchEdge()) {
            return MOVE_ERROR;
        }*/
        if (!mRect.contains(currentX, currentY)) {
            return MOVE_ERROR;
        }
        return MOVE_VH;
        /*if (mRect.right - mRect.left == mWidth
                && mRect.bottom - mRect.top == mHeight) {
            return MOVE_ERROR;
        } else if (mRect.right - mRect.left == mWidth
                && mRect.bottom - mRect.top != mHeight) {
            return MOVE_V;
        } else if (mRect.right - mRect.left != mWidth
                && mRect.bottom - mRect.top == mHeight) {
            return MOVE_H;
        } else {
            return MOVE_VH;
        }*/
    }

    /**
     * 超出边界
     *
     * @return true, false
     */
    boolean touchEdge() {
        return mRect.left < 0 || mRect.right > mWidth
                || mRect.top < 0 || mRect.bottom > mHeight;
    }

    boolean near(PointF one, PointF other) {
        float dx = Math.abs(one.x - other.x);
        float dy = Math.abs(one.y - other.y);
        return Math.pow(dx * dx + dy * dy, 0.5) <= NEAR;
    }

    boolean near(float x1, float y1, float x2, float y2) {
        float dx = Math.abs(x1 - x2);
        float dy = Math.abs(y1 - y2);
        return Math.pow(dx * dx + dy * dy, 0.5) <= NEAR;
    }
}
/*https://blog.csdn.net/DucklikeJAVA/article/details/80947956 */
     