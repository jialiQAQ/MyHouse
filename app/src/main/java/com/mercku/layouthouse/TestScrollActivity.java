package com.mercku.layouthouse;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by yanqiong.ran on 2019-07-18.
 */
public class TestScrollActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageEditingView mMyHouseView;
    private View mLeftSideView;
    private int mCount = 0;
    private int mCurrentMode = MyHouseView.HOUSE_MODE;
    private Button mChangeModeBtn;
    private View mHouseMenuLayout;
    private View mDotMenuLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scroll);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findViewById(R.id.text_tap_to_add).setOnClickListener(this);
        findViewById(R.id.text_add_room).setOnClickListener(this);
        findViewById(R.id.text_remove_room).setOnClickListener(this);
        findViewById(R.id.text_rename_room).setOnClickListener(this);
        findViewById(R.id.current_mode).setOnClickListener(this);
        findViewById(R.id.text_add_dot).setOnClickListener(this);
        findViewById(R.id.text_remove_dot).setOnClickListener(this);
        mLeftSideView = findViewById(R.id.layout_left_side);
        mMyHouseView = findViewById(R.id.layout_custom_view);

        mChangeModeBtn = findViewById(R.id.current_mode);
        mHouseMenuLayout = findViewById(R.id.layout_house_menus);
        mDotMenuLayout = findViewById(R.id.layout_dots_menu);
        mCurrentMode = MyHouseView.HOUSE_MODE;
        mMyHouseView.setMode(mCurrentMode);

        setCurrentModeLayout();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_mode:
                onClickChangeMode();
                break;
            case R.id.text_tap_to_add:
                onClickAdd();
                break;
            case R.id.text_add_room:
                onClickAddRectangle();
                break;
            case R.id.text_remove_room:
                onClickDeleteRectangle();
                break;
            case R.id.text_rename_room:
                onClickRename();
                break;
            case R.id.text_add_dot:
                onClickAddDot();
                break;
            case R.id.text_remove_dot:
                onClickRemoveDot();
                break;
            default:
                break;
        }
    }

    private void onClickAddDot() {
        mMyHouseView.addDot();
    }

    private void onClickRemoveDot() {
        mMyHouseView.removeDot();
    }

    private void onClickChangeMode() {
        android.util.Log.d("ryq", "onClickChangeMode mCurrentMode=" + mCurrentMode);
        if (mCurrentMode == MyHouseView.HOUSE_MODE) {
            mCurrentMode = MyHouseView.DOTS_MODE;
            mMyHouseView.setMode(mCurrentMode);
        } else {
            mCurrentMode = MyHouseView.HOUSE_MODE;
            mMyHouseView.setMode(mCurrentMode);
        }
        setCurrentModeLayout();

    }

    private void setCurrentModeLayout() {
        if (mCurrentMode == MyHouseView.HOUSE_MODE) {
            mChangeModeBtn.setText("当前房间模式\n点击切换");
            mHouseMenuLayout.setVisibility(View.VISIBLE);
            mDotMenuLayout.setVisibility(View.GONE);
        } else {
            mChangeModeBtn.setText("当前打点模式\n点击切换");
            mHouseMenuLayout.setVisibility(View.GONE);
            mDotMenuLayout.setVisibility(View.VISIBLE);
        }
    }

    private void onClickRename() {
        mMyHouseView.renameView();
    }

    private void onClickDeleteRectangle() {
        mMyHouseView.deleteView();
    }

    private void onClickAddRectangle() {
        android.util.Log.d("ryq", "onClickAddRectangle mCount=" + mCount);
        mMyHouseView.addView();
    }

    private void onClickAdd() {
        if (mLeftSideView.getVisibility() == View.VISIBLE) {
            mLeftSideView.setVisibility(View.GONE);
        } else {
            mLeftSideView.setVisibility(View.VISIBLE);
        }
    }

}
