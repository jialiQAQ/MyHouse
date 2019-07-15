package com.mercku.layouthouse;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanqiong.ran on 2019-07-12.
 * refer to:https://blog.csdn.net/zhongwn/article/details/51984476
 */
public class MyHouseView extends View {
    private Context mContext;

    private static final String TAG = "ryq-MyHouseView";
    private Scroller mScroller;
    private int mMaxWidth;
    private int mMaxHeight;
    private List<RectF> mRects = new ArrayList<RectF>();
    private ArrayList<String> mNameArrays = new ArrayList<String>();
    private Paint mPaint;
    private float mCurrentY;
    private float mCurrentX;
    private int mCurrentNEAR;
    private static final float NEAR = 18f;
    private static final float MIN_MOVE_DIS = 6f;
    private static final float WALL_WIDTH = 18f;
    private static final int NONE_TOUCH = -1;
    private static final int NONE_POINT = 99;
    private static final int LEFT_TOP = 1;
    private static final int RIGHT_TOP = 1 + 1;
    private static final int RIGHT_BOTTOM = 1 + 1 + 1;
    private static final int LEFT_BOTTOM = 1 + 1 + 1 + 1;
    private int mSelectedViewIndex;
    private float mScrollStartX;
    private float mScrollStartY;
    private float mLastDownX;
    private float mLastDownY;
    private float mScrollEndX;
    private float mScrollEndY;
    private Paint mTextPaint;
    private Paint mFocusedPaint;
    private int mLastSeletedViewIndex;
    private Paint mGridPaint;

    public MyHouseView(Context context) {
        super(context);
        init(context);
    }

    public MyHouseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "widthMeasureSpec " + widthMeasureSpec + "heightMeasureSpec " + heightMeasureSpec);
        /***自身宽*/
        int measureSelfHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "widthMeasure " + MeasureSpec.getSize(heightMeasureSpec) + " widthMode " + MeasureSpec.getMode(widthMeasureSpec));
        Log.d(TAG, "heightMeasure " + measureSelfHeight + "heightMode " + MeasureSpec.getMode(heightMeasureSpec));


        //设置viewGroup的宽高，也可以在onlayout中通过layoutParams设置
        //totalHeight = getScreenSize(mContext).heightPixels * childCount;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout left " + l + "  top " + t + "  right " + r + "  bottom " + b);
        Log.d(TAG, "onLayout heightPixels " + getScreenSize(mContext).heightPixels);
//        LayoutParams lp = getLayoutParams();
//        totalHeight = getScreenSize(mContext).heightPixels * childCount;
//        lp.height = totalHeight;//设置viewgroup总高度
//        setLayoutParams(lp);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Log.d(TAG, "draw canvas " + canvas + " getWidth()=" + getWidth() + " getHeight()=" + getHeight());

        final int width = getWidth();  //hdpi 480x800
        final int height = getHeight();
        // canvas.translate(width / 2, height / 2);
        final int edgeWidth = 10;
        final int space = 60;   //长宽间隔
        int vertz = 0;
        int hortz = 0;
        for (int i = 0; i < 100; i++) {
            canvas.drawLine(0, vertz, width, vertz, mGridPaint);
            canvas.drawLine(hortz, 0, hortz, height, mGridPaint);
            vertz += space;
            hortz += space;

        }
        for (int index = 0; index < mRects.size(); index++) {
            RectF rect = mRects.get(index);
            if (mSelectedViewIndex == index) {
                canvas.drawRect(rect, mFocusedPaint);
            } else {
                canvas.drawRect(rect, mPaint);
            }
            canvas.drawText(mNameArrays.get(index), (rect.right - rect.left) / 2 + rect.left, (rect.bottom - rect.top) / 2 + rect.top, mTextPaint);
        }

    }

    private void init(Context context) {
        mContext = context;
        mScroller = new Scroller(mContext);
        mMaxHeight = context.getResources().getDisplayMetrics().heightPixels + 480;
        mMaxWidth = context.getResources().getDisplayMetrics().widthPixels + 480;
        mNameArrays.add("客厅");
        mNameArrays.add("卧室");
        mNameArrays.add("厨房");
        mNameArrays.add("卫生间");
        mNameArrays.add("卧室");
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(WALL_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);//用于防止边缘的锯齿
        mPaint.setAlpha(1000);//设置透明度

        mFocusedPaint = new Paint();
        mFocusedPaint.setColor(Color.DKGRAY);
        mFocusedPaint.setStrokeWidth(WALL_WIDTH);
        mFocusedPaint.setStyle(Paint.Style.STROKE);
        mFocusedPaint.setAntiAlias(true);//用于防止边缘的锯齿
        mFocusedPaint.setAlpha(1000);//设置透明度

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);//用于防止边缘的锯齿
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.text_size_h14));
        mTextPaint.setAlpha(1000);//设置透明度

        mGridPaint = new Paint();
        mGridPaint.setColor(Color.RED);
        mGridPaint.setStrokeJoin(Paint.Join.ROUND);
        mGridPaint.setStrokeCap(Paint.Cap.ROUND);
        mGridPaint.setStrokeWidth(1f);
    }

    public void addView() {
        mRects.add(new RectF(0, 0, 200, 200));
        postInvalidate();
    }

    public void deleteView() {
        if (mSelectedViewIndex == NONE_TOUCH) {
            Toast.makeText(mContext, "请先选择一个房间", Toast.LENGTH_LONG).show();
        } else {
            mRects.remove(mSelectedViewIndex);
            mSelectedViewIndex = NONE_TOUCH;
            mLastSeletedViewIndex = NONE_TOUCH;
            postInvalidate();
        }

    }

    public void renameView() {
        if (mSelectedViewIndex == NONE_TOUCH) {
            Toast.makeText(mContext, "请先选择一个房间", Toast.LENGTH_LONG).show();
        } else {
            mRects.remove(mSelectedViewIndex);
            mSelectedViewIndex = NONE_TOUCH;
            mLastSeletedViewIndex = NONE_TOUCH;
            postInvalidate();
        }
    }

    /**
     * 首先选中房间，然后才能进行操作
     * mLastSeletedViewIndex 为上次选中的房间
     * mSelectedViewIndex 为这次选中的房间
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent event.getAction() = " + event.getAction());
        mCurrentX = event.getX();
        mCurrentY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownX = event.getX();
                mLastDownY = event.getY();
                mScrollStartX = getScrollX();
                mScrollStartY = getScrollY();
                if (mSelectedViewIndex == NONE_TOUCH) {
                    mSelectedViewIndex = checkSelectedView();
                } else {
                    mLastSeletedViewIndex = mSelectedViewIndex;
                    mSelectedViewIndex = checkSelectedView();
                }
                //这次选择的跟上次不同,则清除上次的选择
                if (mSelectedViewIndex != NONE_TOUCH && mLastSeletedViewIndex != mSelectedViewIndex) {
                    mLastSeletedViewIndex = NONE_TOUCH;
                }

                Log.d(TAG, "ACTION_DOWN mCurrentX = " + mCurrentX);
                Log.d(TAG, "ACTION_DOWN mCurrentY = " + mCurrentY);
                Log.d(TAG, "ACTION_DOWN mSelectedViewIndex = " + mSelectedViewIndex);
                if (mSelectedViewIndex != NONE_TOUCH) {
                    mCurrentNEAR = checkNear(mSelectedViewIndex);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE mCurrentNEAR = " + mCurrentNEAR + " mLastSeletedViewIndex=" + mLastSeletedViewIndex
                        + " mSelectedViewIndex=" + mSelectedViewIndex + " mCurrentNEAR=" + mCurrentNEAR);
                if (mLastSeletedViewIndex != NONE_TOUCH) {
                    if (mSelectedViewIndex != NONE_TOUCH) {
                        if (mCurrentNEAR == NONE_POINT) {
                            //move
                            RectF rect = mRects.get(mSelectedViewIndex);
                            float newLeft = rect.left + mCurrentX - mLastDownX;
                            float newRight = rect.right + mCurrentX - mLastDownX;
                            float newTop = rect.top + mCurrentY - mLastDownY;
                            float newBottom = rect.bottom + mCurrentY - mLastDownY;
                            rect.set(newLeft, newTop, newRight, newBottom);
                            mLastDownX = mCurrentX;
                            mLastDownY = mCurrentY;
                        } else {
                            //scale
                            //currentY = roundLength(currentY, mHeight);
                            RectF rect = mRects.get(mSelectedViewIndex);
                            if (Math.abs(rect.right - mCurrentX) + 480 > mMaxWidth) {
                                mMaxWidth = (int) Math.abs(rect.right - mCurrentX) + 480;
                            }
                            if (Math.abs(rect.bottom - mCurrentY) + 480 > mMaxHeight) {
                                mMaxHeight = (int) Math.abs(rect.bottom - mCurrentY) + 480;
                            }
                            switch (mCurrentNEAR) {
                                case LEFT_TOP:
                                    rect.set(mCurrentX, mCurrentY, rect.right, rect.bottom);
                                    break;
                                case LEFT_BOTTOM:
                                    rect.set(mCurrentX, rect.top, rect.right, mCurrentY);
                                    break;
                                case RIGHT_TOP:
                                    rect.set(rect.left, mCurrentY, mCurrentX, rect.bottom);
                                    break;
                                case RIGHT_BOTTOM:
                                    rect.set(rect.left, rect.top, mCurrentX, mCurrentY);
                                    break;
                            }
                        }
                        postInvalidate();
                    }
                } else if (mSelectedViewIndex == NONE_TOUCH) {
                    float dy, dx;
                    dy = mLastDownY - mCurrentY;
                    dx = mLastDownX - mCurrentX;
                    Log.d(TAG, "ACTION_MOVE dy = " + dy + " dx=" + dx);
                    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > MIN_MOVE_DIS) {
                        // scrollBy((int) dx, 0);
                        // invalidate();
                        // smoothScrollTo((int) dx, 0);
                    } else if (Math.abs(dx) <= Math.abs(dy) && Math.abs(dy) > MIN_MOVE_DIS) {
                        // scrollBy(0, (int) dy);
                        // invalidate();
                        //smoothScrollTo(0, (int) dy);
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mLastSeletedViewIndex == NONE_TOUCH) {
                    if (mSelectedViewIndex != NONE_TOUCH) {
                        postInvalidate();
                    }
                } else {
                    mSelectedViewIndex = NONE_TOUCH;
                    mLastSeletedViewIndex = NONE_TOUCH;
                    postInvalidate();
                }
                break;
        }
        return true;//需要返回true否则down后无法执行move和up操作
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void smoothScrollTo(int destX, int destY) {
        Log.d(TAG, "smoothScrollTo destX = " + destX + " destY=" + destY + " getScrollX()=" + getScrollX() + " getScrollY()=" + getScrollY());
        mScroller.startScroll(getScrollX(), getScrollY(), destX - getScrollX(), destY - getScrollY(), 1000);
        invalidate();
    }

    private int checkSelectedView() {
        int selectedViewIndex = NONE_TOUCH;
        for (int index = 0; index < mRects.size(); index++) {
            RectF rect = mRects.get(index);
            Log.d(TAG, "checkSelectedView rect.left = " + rect.left
                    + " rect.right=" + rect.right
                    + " rect.top=" + rect.top
                    + " rect.bottom=" + rect.bottom
            );
            if (mCurrentX >= (rect.left - NEAR) && mCurrentX <= (rect.right + NEAR)
                    && mCurrentY >= (rect.top - NEAR) && mCurrentY <= (rect.bottom + NEAR)) {
                selectedViewIndex = index;
                break;
            }
        }
        return selectedViewIndex;
    }

    private int checkNear(int selectedViewIndex) {

        RectF rect = mRects.get(selectedViewIndex);
        boolean nearLT = near(mCurrentX, mCurrentY, rect.left, rect.top);
        if (nearLT) {
            return LEFT_TOP;
        }
        boolean nearLB = near(mCurrentX, mCurrentY, rect.left, rect.bottom);
        if (nearLB) {
            return LEFT_BOTTOM;
        }
        boolean nearRT = near(mCurrentX, mCurrentY, rect.right, rect.top);
        if (nearRT) {
            return RIGHT_TOP;
        }
        boolean nearRB = near(mCurrentX, mCurrentY, rect.right, rect.bottom);
        if (nearRB) {
            return RIGHT_BOTTOM;
        }
        return NONE_POINT;


    }

    boolean near(float x1, float y1, float x2, float y2) {
        float dx = Math.abs(x1 - x2);
        float dy = Math.abs(y1 - y2);
        return Math.pow(dx * dx + dy * dy, 0.5) <= NEAR;
    }

    /**
     * 获取屏幕大小，这个可以用一个常量不用每次都获取
     *
     * @param context
     * @return
     */
    public static DisplayMetrics getScreenSize(Context context) {
        return context.getResources().getDisplayMetrics();
    }

}
