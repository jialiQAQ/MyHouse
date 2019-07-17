package com.mercku.layouthouse;


/**
 * Created by yanqiong.ran on 2019-07-17.
 */
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;
import com.agsw.FabricView.DrawableObjects.CDrawable;
import com.agsw.FabricView.FabricView;

/**
 * @author wangyao
 * @package com.example.administrator.myapplication
 * @date 2017/6/27  14:05
 * @describe 实现一个画布的放置控件、缩放画布、控件之间的连线、画布的手势识别、缩放后手势滑动的识别
 * @project
 */

public class ActionEditorCanvasView extends FabricView {

    private Context mContext;
    // Scale and zoom in/out factor.
    private static final int INIT_ZOOM_SCALES_INDEX = 0;
    private int mCurrentZoomScaleIndex = INIT_ZOOM_SCALES_INDEX;
    private static final float[] ZOOM_SCALES = new float[]{1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
    private float mViewScale = ZOOM_SCALES[INIT_ZOOM_SCALES_INDEX];

    protected boolean mScrollable = true;
    private ScaleGestureDetector mScaleGestureDetector;                 //缩放手势
    private GestureDetector mTapGestureDetector;                        //手势监听类
    private int mPanningPointerId = MotionEvent.INVALID_POINTER_ID;
    private Point mPanningStart = new Point();
    private int mOriginalScrollX;
    private int mOriginalScrollY;
    private float mOffSetViewScroll;

    // Default desired width of the view in pixels.
    private static final int DESIRED_WIDTH = 2048;
    // Default desired height of the view in pixels.
    private static final int DESIRED_HEIGHT = 2048;
    // Interactive Modes
    public static final int DRAW_MODE = 0;              //可以绘制线段的模式
    public static final int SELECT_MODE = 1; // TODO Support Object Selection.
    public static final int ROTATE_MODE = 2; // TODO Support Object ROtation.
    public static final int LOCKED_MODE = 3;              //空模式

    // Default Background Styles         背景颜色
    public static final int BACKGROUND_STYLE_BLANK = 0;
    public static final int BACKGROUND_STYLE_NOTEBOOK_PAPER = 1;
    public static final int BACKGROUND_STYLE_GRAPH_PAPER = 2;


    public ActionEditorCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
    }

    /**
     * The method onFinishInflate() will be called after all children have been added.
     * 这个方法是所有的子view被添加之后调用
     */
    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        // Setting the child view's pivot point to (0,0) means scaling leaves top-left corner in
        // place means there is no need to adjust view translation.
        this.setPivotX(0);
        this.setPivotY(0);

        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(mScrollable);                 //水平滑动滚动条的设置
        setVerticalScrollBarEnabled(mScrollable);                   //竖直滑动滚动条的设置
        setBackgroundMode(BACKGROUND_STYLE_GRAPH_PAPER);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());         //设置手势缩放的监听

        mTapGestureDetector = new GestureDetector(getContext(), new TapGestureListener());          //设置手势的监听

    }

    @Override
    public void setBackgroundMode(int mBackgroundMode) {
        super.setBackgroundMode(mBackgroundMode);
    }

    /**
     * 设置画布所处在的模式,可以添加并设置其他的模式
     */
    @Override
    public void setInteractionMode(int interactionMode) {
        if (interactionMode == DRAW_MODE) {
            super.setInteractionMode(DRAW_MODE);
        } else if (interactionMode == LOCKED_MODE) {
            super.setInteractionMode(LOCKED_MODE);
        }else if (interactionMode==SELECT_MODE){
            super.setInteractionMode(SELECT_MODE);
        }

    }

    /**
     * 增加画布里面的控件或其他实现了CDrawable接口的类
     */
    public boolean addCanvasDrawable(CDrawable cDrawable) {
        super.mDrawableList.add(cDrawable);
        return true;
    }

    /**
     * 清除画布里面的控件
     */
    public void cleanPager() {
        super.cleanPage();
    }

    /**
     * 重置画布的大小尺寸
     */
    public void resetView() {
        // Reset scrolling state.
        mPanningPointerId = MotionEvent.INVALID_POINTER_ID;
        mPanningStart.set(0, 0);
        mOriginalScrollX = 0;
        mOriginalScrollY = 0;
        updateScaleStep(INIT_ZOOM_SCALES_INDEX);
        scrollTo((int) this.getX(), (int) this.getY());
    }


    @Override
    public boolean onTouchDrawMode(MotionEvent event) {
        event.offsetLocation(getScrollX(), getScrollY());                //缩放后偏移的距离，保证缩放后触点跟缩放前对应
        return super.onTouchDrawMode(event);
    }

    @Override
    public boolean onTouchSelectMode(MotionEvent event) {
        return super.onTouchSelectMode(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mTapGestureDetector.onTouchEvent(event);
        // TODO: 2017/6/29 可根据触发事件做到动作控件的拖动
        return super.onTouchEvent(event);
    }

    /**
     * 画布实现缩小的方法
     *
     * @return
     */
    public boolean zoomOut() {
//        if (mScrollable && mCurrentZoomScaleIndex > 0) {
        if (mCurrentZoomScaleIndex > 0) {
            updateScaleStep(mCurrentZoomScaleIndex - 1);
            return true;
        }
        return false;
    }


    /**
     * 画布实现放大的方法
     *
     * @return
     */
    public boolean zoomIn() {
        if (mCurrentZoomScaleIndex < ZOOM_SCALES.length - 1) {
            updateScaleStep(mCurrentZoomScaleIndex + 1);
            return true;
        }
        return false;
    }

    /**
     * 缩放的具体实现
     *
     * @param newScaleIndex
     */
    private void updateScaleStep(int newScaleIndex) {
        if (newScaleIndex != mCurrentZoomScaleIndex) {
            final float oldViewScale = mViewScale;

            mCurrentZoomScaleIndex = newScaleIndex;
            mViewScale = ZOOM_SCALES[mCurrentZoomScaleIndex];

            final float scaleDifference = mViewScale - oldViewScale;
            scrollBy((int) (scaleDifference * getMeasuredWidth() / 2),
                    (int) (scaleDifference * getMeasuredHeight() / 2));

            this.setScaleX(mViewScale);
            this.setScaleY(mViewScale);
            this.requestLayout();
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                getMeasuredSize(widthMeasureSpec, DESIRED_WIDTH),
                getMeasuredSize(heightMeasureSpec, DESIRED_HEIGHT));

    }

    private static int getMeasuredSize(int measureSpec, int desiredSize) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else if (mode == MeasureSpec.AT_MOST) {
            return Math.min(size, desiredSize);
        } else {
            return desiredSize;
        }

    }


    private class TapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            // TODO: 2017/6/29 长按可以设置模式为连线
            Toast.makeText(mContext,"实现了长按手势",Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // Focus point at the start of the pinch gesture. This is used for computing proper scroll
        // offsets during scaling, as well as for simultaneous panning.
        private float mStartFocusX;
        private float mStartFocusY;
        // View scale at the beginning of the gesture. This is used for computing proper scroll
        // offsets during scaling.
        private float mStartScale;
        // View scroll offsets at the beginning of the gesture. These provide the reference point
        // for adjusting scroll in response to scaling and panning.
        private int mStartScrollX;
        private int mStartScrollY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mStartFocusX = detector.getFocusX();
            mStartFocusY = detector.getFocusY();
            mStartScrollX = getScrollX();
            mStartScrollY = getScrollY();

            mStartScale = mViewScale;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float oldViewScale = mViewScale;

            final float scaleFactor = detector.getScaleFactor();
            mViewScale *= scaleFactor;

            if (mViewScale < ZOOM_SCALES[0]) {
                mCurrentZoomScaleIndex = 0;
                mViewScale = ZOOM_SCALES[mCurrentZoomScaleIndex];
            } else if (mViewScale > ZOOM_SCALES[ZOOM_SCALES.length - 1]) {
                mCurrentZoomScaleIndex = ZOOM_SCALES.length - 1;
                mViewScale = ZOOM_SCALES[mCurrentZoomScaleIndex];
            } else {
                // find nearest zoom scale
                float minDist = Float.MAX_VALUE;
                // If we reach the end the last one was the closest
                int index = ZOOM_SCALES.length - 1;
                for (int i = 0; i < ZOOM_SCALES.length; i++) {
                    float dist = Math.abs(mViewScale - ZOOM_SCALES[i]);
                    if (dist < minDist) {
                        minDist = dist;
                    } else {
                        // When it starts increasing again we've found the closest
                        index = i - 1;
                        break;
                    }
                }
                mCurrentZoomScaleIndex = index;
            }

          /*  if (shouldDrawGrid()) {
                mGridRenderer.updateGridBitmap(mViewScale);
            }*/

            ActionEditorCanvasView.this.setScaleX(mViewScale);
            ActionEditorCanvasView.this.setScaleY(mViewScale);

            // Compute scroll offsets based on difference between original and new scaling factor
            // and the focus point where the gesture started. This makes sure that the scroll offset
            // is adjusted to keep the focus point in place on the screen unless there is also a
            // focus point shift (see next scroll component below).
            final float scaleDifference = mViewScale - mStartScale;
            final int scrollScaleX = (int) (scaleDifference * mStartFocusX);
            final int scrollScaleY = (int) (scaleDifference * mStartFocusY);

            // Compute scroll offset based on shift of the focus point. This makes sure the view
            // pans along with the focus.
            final int scrollPanX = (int) (mStartFocusX - detector.getFocusX());
            final int scrollPanY = (int) (mStartFocusY - detector.getFocusY());

            // Apply the computed scroll components for scale and panning relative to the scroll
            // coordinates at the beginning of the gesture.
            scrollTo(mStartScrollX + scrollScaleX + scrollPanX,
                    mStartScrollY + scrollScaleY + scrollPanY);

            return true;
        }
    }
}