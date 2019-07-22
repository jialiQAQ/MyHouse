package com.mercku.layouthouse;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Created by yanqiong.ran on 2019-07-12.
 * refer to:https://blog.csdn.net/zhongwn/article/details/51984476
 */
public class MyHouseView extends View {
    private Context mContext;
    // Scale and zoom in/out factor.
    private static final int INIT_ZOOM_SCALES_INDEX = 0;
    private int mCurrentZoomScaleIndex = INIT_ZOOM_SCALES_INDEX;
    private static final float[] ZOOM_SCALES = new float[]{0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f};
    private float mViewScale = ZOOM_SCALES[INIT_ZOOM_SCALES_INDEX];

    private static final String TAG = "ryq-MyHouseView";
    private Scroller mScroller;
    private int mMaxWidth;
    private int mMaxHeight;
    private ArrayList<House> mHouseList;
    private ArrayList<String> mNameArrays = new ArrayList<String>();
    //每次从mNameArrays中从第0个按顺序选取名字。
    private int nameIndex = 0;
    private Paint mHousePaint;
    private float mCurrentY;
    private float mCurrentX;
    private int mCurrentNEAR;
    private static final float NEAR = 30f;
    private static final float MIN_MOVE_DIS = 6f;
    private static final float WALL_WIDTH = 18f;
    private static final int NONE_TOUCH = -1;
    private static final int NONE_POINT = 99;
    private static final int LEFT_TOP = 1;
    private static final int RIGHT_TOP = 1 + 1;
    private static final int RIGHT_BOTTOM = 1 + 1 + 1;
    private static final int LEFT_BOTTOM = 1 + 1 + 1 + 1;
    private int mSelectedViewIndex;
    private float mLastDownX;
    private float mLastDownY;
    private Paint mTextPaint;
    private Paint mFocusedHousePaint;
    private int mLastSeletedViewIndex;
    private Paint mGridPaint;
    private ScaleGestureDetector mScaleGestureDetector;
    private boolean mIsScale;
    private static final float DEFAULT_WALL_WIDTH = 200;
    private static final float DEFAULT_WALL_HEIGHT = 200;
    private static final float DEFAULT_DOT_RADIUS = 30;
    public static final int HOUSE_MODE = 100;
    public static final int DOTS_MODE = 101;
    private int mCurrentMode;
    private AbstractList<Node> mDotList;
    private int mSelectedDotIndex = NONE_TOUCH;
    private int mLastSeletedDotIndex = NONE_TOUCH;

    private class House {
        private String name;
        private RectF rect;
        private String id;
    }

    private class Node {
        private String sn;
        private float cx;
        private float cy;
        private String id;
    }

    public MyHouseView(Context context) {
        super(context);
        init(context);
    }

    public MyHouseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
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

        mHousePaint = new Paint();
        mHousePaint.setColor(Color.GRAY);
        mHousePaint.setStrokeWidth(WALL_WIDTH);
        mHousePaint.setStyle(Paint.Style.STROKE);
        mHousePaint.setAntiAlias(true);//用于防止边缘的锯齿
        mHousePaint.setAlpha(1000);//设置透明度

        mFocusedHousePaint = new Paint();
        mFocusedHousePaint.setColor(Color.DKGRAY);
        mFocusedHousePaint.setStrokeWidth(WALL_WIDTH);
        mFocusedHousePaint.setStyle(Paint.Style.STROKE);
        mFocusedHousePaint.setAntiAlias(true);//用于防止边缘的锯齿
        mFocusedHousePaint.setAlpha(1000);//设置透明度

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);//用于防止边缘的锯齿
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.text_size_h14));
        mTextPaint.setAlpha(1000);//设置透明度

        mGridPaint = new Paint();
        mGridPaint.setColor(getResources().getColor(R.color.blue_green));
        mGridPaint.setStrokeJoin(Paint.Join.ROUND);
        mGridPaint.setStrokeCap(Paint.Cap.ROUND);
        mGridPaint.setStrokeWidth(1f);

        mHouseList = new ArrayList<>();
        mDotList = new ArrayList<>();
    }

    public void setMode(int mode) {
        mCurrentMode = mode;
        mSelectedDotIndex = NONE_TOUCH;
        mLastSeletedDotIndex = NONE_TOUCH;
        mSelectedViewIndex = NONE_TOUCH;
        mLastSeletedViewIndex = NONE_TOUCH;
        postInvalidate();
    }

    private static final int fixedWidthMeasureSpec
            = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY);

    private static final int fixedHeightMeasureSpec
            = View.MeasureSpec.makeMeasureSpec(3840, View.MeasureSpec.EXACTLY);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "onMeasure widthMeasureSpec " + widthMeasureSpec + "heightMeasureSpec " + heightMeasureSpec);
        /***自身宽*/
        int measureSelfHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "onMeasure widthMeasure " + MeasureSpec.getSize(heightMeasureSpec) + " widthMode " + MeasureSpec.getMode(widthMeasureSpec));
        Log.d(TAG, "onMeasure heightMeasure " + measureSelfHeight + "heightMode " + MeasureSpec.getMode(heightMeasureSpec));


        //设置viewGroup的宽高，也可以在onlayout中通过layoutParams设置
        //totalHeight = getScreenSize(mContext).heightPixels * childCount;
        // h=resolveSizeAndState(mContext.getResources().getDisplayMetrics().heightPixels,heightMeasureSpec,1);

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + mContext.getResources().getDisplayMetrics().widthPixels * 2;
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = getPaddingBottom() + getPaddingTop() + mContext.getResources().getDisplayMetrics().heightPixels * 2;
        int h = resolveSizeAndState(minh, heightMeasureSpec, 1);
        Log.d(TAG, "onMeasure w =" + w + " h= " + h + " minw=" + minw + " minh=" + minh);
        // setMeasuredDimension(minw, minh);
       // super.onMeasure(fixedWidthMeasureSpec, fixedHeightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onMeasure w =" + w + " h= " + h + " oldw=" + oldw + " oldh=" + oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout left " + l + "  top " + t + "  right " + r + "  bottom " + b);
        Log.d(TAG, "onLayout heightPixels " + getScreenSize(mContext).heightPixels);
//        LayoutParams lp = getLayoutParams();
//        totalHeight = getScreenSize(mContext).heightPixels * childCount;
//        lp.height = totalHeight;//设置viewgroup总高度
//        setLayoutParams(lp);
        super.onLayout(changed, 0, 0, 2160, 3840);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // drawBackground(canvas, mBackgroundMode);
        Log.d(TAG, "draw canvas getLeft=" + getLeft() + "getX()=" + getX() + " getTranslationX()=" + getTranslationX() +
                " getWidth()=" + getWidth() + " getHeight()=" + getHeight());
         drawBackground(canvas);
        //draw all houses
        for (int index = 0; index < mHouseList.size(); index++) {
            House house = mHouseList.get(index);
            RectF rect = house.rect;
            if (mSelectedViewIndex == index) {
                canvas.drawCircle(rect.left, rect.top, DEFAULT_DOT_RADIUS / 2, mFocusedHousePaint);
                canvas.drawCircle(rect.left, rect.bottom, DEFAULT_DOT_RADIUS / 2, mFocusedHousePaint);
                canvas.drawCircle(rect.right, rect.top, DEFAULT_DOT_RADIUS / 2, mFocusedHousePaint);
                canvas.drawCircle(rect.right, rect.bottom, DEFAULT_DOT_RADIUS / 2, mFocusedHousePaint);
                canvas.drawRect(rect, mFocusedHousePaint);
            } else {
                canvas.drawRect(rect, mHousePaint);
            }
            drawText(canvas, house.name, rect);

        }
        for (int index = 0; index < mDotList.size(); index++) {
            Node dot = mDotList.get(index);
            if (mSelectedDotIndex == index) {
                canvas.drawCircle(dot.cx, dot.cy, DEFAULT_DOT_RADIUS, mFocusedHousePaint);
            } else {
                canvas.drawCircle(dot.cx, dot.cy, DEFAULT_DOT_RADIUS, mHousePaint);
            }
        }
    }
    private Bitmap mBitmap;
    private Canvas drawBackground(Canvas canvas ) {
       /* final int width = getWidth();  //hdpi 480x800
        final int height = getHeight();
        mBackgroundColor = mContext.getResources().getColor(R.color.half_trans);
        canvas.drawColor(mBackgroundColor);
        final int edgeWidth = 10;
        final int space = 60;   //长宽间隔
        int vertz = 0;
        int hortz = 0;
        //TODO!! 未铺满
        for (int i = 0; i < 100; i++) {
            canvas.drawLine(0, vertz, width, vertz, mGridPaint);
            canvas.drawLine(hortz, 0, hortz, height, mGridPaint);
            vertz += space;
            hortz += space;

        }*/
        /**
         * 注意多个createBiamap重载函数，必须是可变位图对应的重载才能绘制
         * bitmap: 原图像
         * pixInterval: 网格线的横竖间隔，单位:像素
         */
        int pixInterval = 60;
        Bitmap bitmap = Bitmap.createBitmap(3240, 5760, Bitmap.Config.ARGB_8888);  //很重要
        bitmap.eraseColor(Color.WHITE);//填充颜色
        android.util.Log.d("ryq", " bitmap1 getWidth()=" + bitmap.getWidth() + " bitmap getHeight()=" + bitmap.getHeight());

        Canvas bimapCanvas = new Canvas(bitmap);  //创建画布
        Paint paint = new Paint();  //画笔
        paint.setStrokeWidth(1);  //设置线宽。单位为像素
        paint.setAntiAlias(true); //抗锯齿
        paint.setColor(Color.RED);  //画笔颜色
        bimapCanvas.drawBitmap(bitmap, new Matrix(), paint);  //在画布上画一个和bitmap一模一样的图
        //根据Bitmap大小，画网格线
        //画横线
        for (int i = 0; i < bitmap.getHeight() / pixInterval; i++) {
            bimapCanvas.drawLine(0, i * pixInterval, bitmap.getWidth(), i * pixInterval, paint);
        }
        //画竖线
        for (int i = 0; i < bitmap.getWidth() / pixInterval; i++) {
            bimapCanvas.drawLine(i * pixInterval, 0, i * pixInterval, bitmap.getHeight(), paint);
        }
        canvas.drawBitmap(bitmap, new Matrix(), new Paint());
        return canvas;
    }
    private static Bitmap drawBackground(Bitmap bitmap, int pixInterval) {
        /**
         * 注意多个createBiamap重载函数，必须是可变位图对应的重载才能绘制
         * bitmap: 原图像
         * pixInterval: 网格线的横竖间隔，单位:像素
         */
        Bitmap copy = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);  //很重要
        Canvas canvas = new Canvas(copy);  //创建画布
        Paint paint = new Paint();  //画笔
        paint.setStrokeWidth(1);  //设置线宽。单位为像素
        paint.setAntiAlias(true); //抗锯齿
        paint.setColor(Color.RED);  //画笔颜色
        canvas.drawBitmap(bitmap, new Matrix(), paint);  //在画布上画一个和bitmap一模一样的图
        //根据Bitmap大小，画网格线
        for (int i = 0; i < bitmap.getHeight() / pixInterval; i++) {
            canvas.drawLine(0, i * pixInterval, bitmap.getWidth(), i * pixInterval, paint);
        }
        for (int i = 0; i < bitmap.getWidth() / pixInterval; i++) {
            canvas.drawLine(i * pixInterval, 0, i * pixInterval, bitmap.getHeight(), paint);
        }
        return copy;
    }
    private void drawText(Canvas canvas, String name, RectF rect) {
        float textPaintWidth = mTextPaint.measureText(name);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textStartX = (rect.right - rect.left) / 2 + rect.left - textPaintWidth / 2;
        float textStartY = (rect.bottom - rect.top) / 2 + rect.top;
        canvas.drawText(name, textStartX, textStartY, mTextPaint);
    }
    //TODO!!  画布左上角不在可视范围，无法正确显示

    public void addView() {
        House house = new House();
        float startLeft = 200;
        float startTop = 200;
        float startRight = startLeft + DEFAULT_WALL_WIDTH + mHousePaint.getStrokeWidth() * 2;
        float startBottom = startTop + DEFAULT_WALL_HEIGHT + mHousePaint.getStrokeWidth() * 2;
        house.rect = new RectF(startLeft, startTop, startRight, startBottom);
        house.name = mNameArrays.get(nameIndex++ % mNameArrays.size());
        house.id = String.valueOf(System.currentTimeMillis());
        mHouseList.add(house);
        postInvalidate();
    }

    public void deleteView() {
        if (mSelectedViewIndex == NONE_TOUCH) {
            Toast.makeText(mContext, "请先选择一个房间", Toast.LENGTH_LONG).show();
        } else {
            mHouseList.remove(mSelectedViewIndex);
            mSelectedViewIndex = NONE_TOUCH;
            mLastSeletedViewIndex = NONE_TOUCH;
            postInvalidate();
        }

    }

    public void addDot() {
        Node dot = new Node();
        float startCenterX = 200 + DEFAULT_DOT_RADIUS;
        float startCenterY = 200 + DEFAULT_DOT_RADIUS;
        dot.id = String.valueOf(System.currentTimeMillis());
        dot.cx = startCenterX;
        dot.cy = startCenterY;
        mDotList.add(dot);
        postInvalidate();
    }

    public void removeDot() {
        if (mSelectedDotIndex == NONE_TOUCH) {
            Toast.makeText(mContext, "请先选择一个节点", Toast.LENGTH_LONG).show();
        } else {
            mDotList.remove(mSelectedDotIndex);
            mSelectedDotIndex = NONE_TOUCH;
            mLastSeletedDotIndex = NONE_TOUCH;
            postInvalidate();
        }
    }

    public void renameView() {
        if (mSelectedViewIndex == NONE_TOUCH) {
            Toast.makeText(mContext, "请先选择一个房间", Toast.LENGTH_LONG).show();
        } else {
            showRenameDialog();
        }
    }

    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final EditText editText = new EditText(mContext);
        String name = mHouseList.get(mSelectedViewIndex).name;
        editText.setText(name);
        editText.setSelection(name.length());
        builder.setView(editText);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHouseList.get(mSelectedViewIndex).name = editText.getText().toString().trim();
                        postInvalidate();
                    }
                }
        );
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
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

        mCurrentX = event.getX();
        mCurrentY = event.getY();
        boolean result = true;
        mIsScale = false;
        Log.d(TAG, "onTouchEvent event.getAction() = " + event.getAction() + " mIsScale=" + mIsScale
                + " event.getPointerCount()=" + event.getPointerCount() + " event.getActionMasked()=" + event.getActionMasked());
        if (event.getPointerCount() > 1) {
            mScaleGestureDetector.onTouchEvent(event);
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownX = event.getX();
                mLastDownY = event.getY();
                if (mCurrentMode == HOUSE_MODE) {
                    checkTouchDownViewRange();
                } else {
                    checkTouchDownDotRange();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mSelectedViewIndex == NONE_TOUCH && mSelectedDotIndex == NONE_TOUCH) {
                    scrollTheCanvas();
                } else {
                    switch (mCurrentMode) {
                        case HOUSE_MODE:
                            moveOrScaleHouse();
                            break;
                        case DOTS_MODE:
                            moveDot();
                            break;
                        default:
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mLastSeletedViewIndex == NONE_TOUCH && mSelectedViewIndex != NONE_TOUCH
                        || mLastSeletedDotIndex == NONE_TOUCH && mSelectedDotIndex != NONE_TOUCH) {
                    postInvalidate();
                } else {
                    mSelectedViewIndex = NONE_TOUCH;
                    mLastSeletedViewIndex = NONE_TOUCH;
                    mSelectedDotIndex = NONE_TOUCH;
                    mLastSeletedDotIndex = NONE_TOUCH;
                    postInvalidate();
                }
                break;
        }
        return result;//需要返回true否则down后无法执行move和up操作
    }

    private void scrollTheCanvas() {
        float dy, dx;
        dy = mLastDownY - mCurrentY;
        dx = mLastDownX - mCurrentX;
        Log.d(TAG, "ACTION_MOVE mLastDownX=" + mLastDownX + " mLastDownY=" + mLastDownY
                + " mCurrentX=" + mCurrentX + " mCurrentY=" + mCurrentY
                + " dy = " + dy + " dx=" + dx);
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > MIN_MOVE_DIS) {
            //移动view中的内容
            // scrollBy((int) dx, 0);
            // invalidate();
            //当前的view平移,平移后无法选中房间
            // smoothScrollTo((int) dx, 0);
            //加上后滑动很卡
            //mLastDownX = mCurrentX;
            //TODO!!! 滑动不顺畅
            ObjectAnimator mObjectAnimator = ObjectAnimator.ofFloat(this, "translationX", -dx);
            mObjectAnimator.setDuration(500);
            mObjectAnimator.start();
        } else if (Math.abs(dx) <= Math.abs(dy) && Math.abs(dy) > MIN_MOVE_DIS) {
            // scrollBy(0, (int) dy);
            // invalidate();
            //当前的view平移,平移后无法选中房间
            //smoothScrollTo(0, (int) dy);
            //加上后滑动很卡
            //mLastDownY = mCurrentY;
            //TODO!!! 滑动不顺畅
            ObjectAnimator mObjectAnimator = ObjectAnimator.ofFloat(this, "translationY", -dy);
            mObjectAnimator.setDuration(500);
            mObjectAnimator.start();
        }
    }

    private void moveDot() {
        if (mLastSeletedDotIndex != NONE_TOUCH) {
            if (mSelectedDotIndex != NONE_TOUCH) {
                //move
                Node node = mDotList.get(mSelectedDotIndex);
                node.cx = node.cx + mCurrentX - mLastDownX;
                node.cy = node.cy + mCurrentY - mLastDownY;
                mLastDownX = mCurrentX;
                mLastDownY = mCurrentY;
                postInvalidate();
            }
        }
    }

    private void moveOrScaleHouse() {
        Log.d(TAG, "ACTION_MOVE mCurrentNEAR = " + mCurrentNEAR + " mLastSeletedViewIndex=" + mLastSeletedViewIndex
                + " mSelectedViewIndex=" + mSelectedViewIndex + " mCurrentNEAR=" + mCurrentNEAR);
        if (mLastSeletedViewIndex != NONE_TOUCH) {
            if (mSelectedViewIndex != NONE_TOUCH) {
                if (mCurrentNEAR == NONE_POINT) {
                    //move
                    RectF rect = mHouseList.get(mSelectedViewIndex).rect;
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
                    RectF rect = mHouseList.get(mSelectedViewIndex).rect;
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
        }
    }

    private void checkTouchDownDotRange() {
        if (mSelectedDotIndex == NONE_TOUCH) {
            mSelectedDotIndex = checkSelectedDot();
        } else {
            mLastSeletedDotIndex = mSelectedDotIndex;
            mSelectedDotIndex = checkSelectedDot();
        }
        //这次选择的跟上次不同,则清除上次的选择
        if (mSelectedDotIndex != NONE_TOUCH && mLastSeletedDotIndex != mSelectedDotIndex) {
            mLastSeletedDotIndex = NONE_TOUCH;
        }

        Log.d(TAG, "ACTION_DOWN mCurrentX = " + mCurrentX);
        Log.d(TAG, "ACTION_DOWN mCurrentY = " + mCurrentY);
        Log.d(TAG, "ACTION_DOWN mSelectedViewIndex = " + mSelectedDotIndex);

    }

    private int checkSelectedDot() {
        int selectedViewIndex = NONE_TOUCH;
        for (int index = 0; index < mDotList.size(); index++) {
            Node node = mDotList.get(index);
            Log.d(TAG, "checkSelectedView node.cx = " + node.cx
                    + " node.cy=" + node.cy
            );
            if (mCurrentX >= (node.cx - DEFAULT_DOT_RADIUS - NEAR) && mCurrentX <= (node.cx + DEFAULT_DOT_RADIUS + NEAR)
                    && mCurrentY >= (node.cy - DEFAULT_DOT_RADIUS - NEAR) && mCurrentY <= (node.cy + DEFAULT_DOT_RADIUS + NEAR)) {
                selectedViewIndex = index;
                break;
            }
        }
        return selectedViewIndex;
    }

    private void checkTouchDownViewRange() {
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

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void smoothScrollTo(int dx, int dy) {
        Log.d(TAG, "smoothScrollTo dx = " + dx + " dy=" + dy + " getScrollX()=" + getScrollX() + " getScrollY()=" + getScrollY());
        mScroller.startScroll(getScrollX(), getScrollY(), dx, dy, 2000);
        postInvalidate();
    }

    private int checkSelectedView() {
        int selectedViewIndex = NONE_TOUCH;
        for (int index = 0; index < mHouseList.size(); index++) {
            RectF rect = mHouseList.get(index).rect;
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

        RectF rect = mHouseList.get(selectedViewIndex).rect;
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

    /*******************************************
     * Drawing Events
     ******************************************/
    /**
     * Draw the background on the canvas
     *
     * @param canvas         the canvas to draw on
     * @param backgroundMode one of BACKGROUND_STYLE_GRAPH_PAPER, BACKGROUND_STYLE_NOTEBOOK_PAPER, BACKGROUND_STYLE_BLANK
     */
    private int mBackgroundColor = Color.WHITE;
    public static final int BACKGROUND_STYLE_BLANK = 0;

    public static final int BACKGROUND_STYLE_NOTEBOOK_PAPER = 1;

    public static final int BACKGROUND_STYLE_GRAPH_PAPER = 2;

    private int mBackgroundMode = BACKGROUND_STYLE_GRAPH_PAPER;

    private Paint.Style mStyle = Paint.Style.STROKE;

    private float mSize = 5f;

    public void drawBackground(Canvas canvas, int backgroundMode) {
        if (mBackgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(mBackgroundColor);
        }
        if (backgroundMode != BACKGROUND_STYLE_BLANK) {
            Paint linePaint = new Paint();
            // linePaint.setColor(Color.argb(50, 0, 0, 0));
            linePaint.setColor(getResources().getColor(R.color.blue_green));
            linePaint.setStyle(mStyle);
            linePaint.setStrokeJoin(Paint.Join.ROUND);
            linePaint.setStrokeWidth(mSize - 2f);
            switch (backgroundMode) {
                case BACKGROUND_STYLE_GRAPH_PAPER:
                    drawGraphPaperBackground(canvas, linePaint);
                    break;

                default:
                    break;
            }
        }
        //mRedrawBackground = false;
    }

    /**
     * Draws a graph paper background on the view
     *
     * @param canvas the canvas to draw on
     * @param paint  the paint to use
     */
    private void drawGraphPaperBackground(Canvas canvas, Paint paint) {
        int i = 0;
        boolean doneH = false, doneV = false;

        // while we still need to draw either H or V
        while (!(doneH && doneV)) {

            // check if there is more H lines to draw
            if (i < canvas.getHeight()) {
                canvas.drawLine(0, i, canvas.getWidth(), i, paint);
            } else {
                doneH = true;
            }
            // check if there is more V lines to draw
            if (i < canvas.getWidth()) {
                canvas.drawLine(i, 0, i, canvas.getHeight(), paint);
            } else {
                doneV = true;
            }
            // declare as done
            i += 75;
        }
    }

    /**
     * The method onFinishInflate() will be called after all children have been added.
     * 这个方法是所有的子view被添加之后调用
     */
    protected boolean mScrollable = true;

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
        mBackgroundMode = BACKGROUND_STYLE_GRAPH_PAPER;
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());         //设置手势缩放的监听

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
            Log.d(TAG, "onScaleBegin mViewScale = " + mViewScale);
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

            MyHouseView.this.setScaleX(mViewScale);
            MyHouseView.this.setScaleY(mViewScale);

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
            // scrollTo(mStartScrollX + scrollScaleX + scrollPanX,
            //         mStartScrollY + scrollScaleY + scrollPanY);
            Log.d(TAG, "onScale mViewScale = " + mViewScale +
                    " mStartScale=" + mStartScale + " mStartFocusX=" + mStartFocusX
                    + " mStartFocusY=" + mStartFocusY +
                    " detector.getFocusX()=" + detector.getFocusX() + "  detector.getFocusY()=" + detector.getFocusY());
            mIsScale = true;
            return true;
        }
    }


}
