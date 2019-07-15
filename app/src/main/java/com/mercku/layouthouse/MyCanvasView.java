package com.mercku.layouthouse;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by yanqiong.ran on 2019-07-12.
 * refer to:https://blog.csdn.net/zhongwn/article/details/51984476
 */
public class MyCanvasView extends ViewGroup {
    private Context mContext;

    private int mScreenHeight;

    private Scroller mScroller;

    private int totalHeight;

    private static final String TAG = "ryq-MyCanvasView";

    public MyCanvasView(Context context) {
        super(context);
        init(context);
    }

    public MyCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "widthMeasureSpec " + widthMeasureSpec+"heightMeasureSpec " + heightMeasureSpec);
        /***自身宽*/
        int measureSelfWidth = measureRealWidth(widthMeasureSpec);
        int measureSelfHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "measureRealWidth " + measureSelfWidth+" widthMode " + MeasureSpec.getMode(widthMeasureSpec));
        Log.d(TAG, "heightMeasure " + measureSelfHeight+"heightMode " + MeasureSpec.getMode(heightMeasureSpec));

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
        //设置viewGroup的宽高，也可以在onlayout中通过layoutParams设置
        //totalHeight = getScreenSize(mContext).heightPixels * childCount;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout left " + l + "  top " + t + "  right " + r + "  bottom " + b);
        Log.d(TAG, "onLayout heightPixels " + getScreenSize(mContext).heightPixels);
        int childCount = getChildCount();
//        LayoutParams lp = getLayoutParams();
//        totalHeight = getScreenSize(mContext).heightPixels * childCount;
//        lp.height = totalHeight;//设置viewgroup总高度
//        setLayoutParams(lp);
        Log.d(TAG, "onLayout childCount " + childCount);
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            //childView.layout(l, i * mScreenHeight, r, (i + 1) * mScreenHeight);
            childView.layout(l,t,r,b);
        }
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Log.d(TAG, "draw canvas " + canvas);
    }

    private void init(Context context) {
        mContext = context;
        mScreenHeight = getScreenSize(mContext).heightPixels;
        mScroller = new Scroller(mContext);

    }

    /***
     * 获取真实的宽高 比如200px
     *
     * @param widthMeasureSpec
     * @return
     */
    public int measureRealWidth(int widthMeasureSpec) {
        int result = 200;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int realWidth = MeasureSpec.getSize(widthMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                //MeasureSpec.EXACTLY：精确值模式： 控件的layout_width或layout_heiht指定为具体值，比如200dp，或者指定为match_parent（占据父view的大小），系统返回的是这个模式
                result = realWidth;
                Log.d(TAG, "EXACTLY result " + result);
                break;
            case MeasureSpec.AT_MOST:
                // MeasureSpec.AT_MOST: 最大值模式，控件的layout_width或layout_heiht指定为wrap_content时，控件大小一般随着控件的子控件或内容的变化而变化，此时控件的尺寸不能超过父控件
                result = Math.min(result, realWidth);
                Log.d(TAG, "AT_MOST result " + result);
                break;
            case MeasureSpec.UNSPECIFIED:
                // MeasureSpec.UNSPECIFIED:不指定其大小测量模式，通常在绘制定义view的时候才会使用，即多大由开发者在onDraw()的时候指定大小
                result = realWidth;
                Log.d(TAG, "UNSPECIFIED result " + result);
                break;
        }
        return result;
    }

    private float lastDownY;
    private float mScrollStart;
    private float mScrollEnd;

     @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent event.getAction() = " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastDownY = event.getY();
                mScrollStart = getScrollX();
                Log.d(TAG, "ACTION_DOWN lastDownY = " + lastDownY);
                Log.d(TAG, "ACTION_DOWN mScrollStart = " + mScrollStart);
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                float dy;
                dy = lastDownY - currentY;
                Log.d(TAG, "ACTION_MOVE dy = " + dy);
                Log.d(TAG, "ACTION_MOVE getScrollY() = " + getScrollY());
                Log.d(TAG, "ACTION_MOVE getHeight()  = " + getHeight());
                Log.d(TAG, "ACTION_MOVE getHeight() - mScreenHeight = " + (getHeight() - mScreenHeight));
                if (getScrollY() < 0) {
                    dy = 0;
                    //最顶端，超过0时，不再下拉，要是不设置这个，getScrollY一直是负数
//                    setScrollY(0);
                } else if (getScrollY() > getHeight() - mScreenHeight) {
                    dy = 0;
                    //滑到最底端时，不再滑动，要是不设置这个，getScrollY一直是大于getHeight() - mScreenHeight的数，无法再滑动
//                    setScrollY(getHeight() - mScreenHeight);
                }
                scrollBy(0, (int) dy);
                //不断的设置Y，在滑动的时候子view就会比较顺畅
                lastDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mScrollEnd = getScrollY();
                int dScrollY = (int) (mScrollEnd - mScrollStart);
                if (mScrollEnd < 0) {// 最顶端：手指向下滑动，回到初始位置
                    Log.d(TAG, "mScrollEnd < 0" + dScrollY);
                    mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
                } else if (mScrollEnd > getHeight() - mScreenHeight) {//已经到最底端，手指向上滑动回到底部位置
                    Log.d(TAG, "getHeight() - mScreenHeight - (int) mScrollEnd " + (getHeight() - mScreenHeight - (int) mScrollEnd));
                    mScroller.startScroll(0, getScrollY(), 0, getHeight() - mScreenHeight - (int) mScrollEnd);
                }
                postInvalidate();// 重绘执行computeScroll()
                break;
        }
        return true;//需要返回true否则down后无法执行move和up操作
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
