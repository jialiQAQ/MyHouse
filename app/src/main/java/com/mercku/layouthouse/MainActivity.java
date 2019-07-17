package com.mercku.layouthouse;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yanqiong.ran on 2019-07-12.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MyHouseView mMyHouseView;
    private View mLeftSideView;
    private int mCount = 0;
    private com.agsw.FabricView.FabricView  mActionEditorView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findViewById(R.id.text_tap_to_add).setOnClickListener(this);
        findViewById(R.id.text_add_room).setOnClickListener(this);
        findViewById(R.id.text_remove_room).setOnClickListener(this);
        findViewById(R.id.text_rename_room).setOnClickListener(this);
        findViewById(R.id.text_zoom_in).setOnClickListener(this);
        findViewById(R.id.text_zoom_out).setOnClickListener(this);
        findViewById(R.id.text_reset_zoom).setOnClickListener(this);
        mLeftSideView = findViewById(R.id.layout_left_side);
        mMyHouseView = findViewById(R.id.layout_scrollview);
        mActionEditorView = findViewById(R.id.layout_actionEditorCanvasView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.text_zoom_in:
                if(mActionEditorView instanceof ActionEditorCanvasView){
                    ((ActionEditorCanvasView)mActionEditorView).zoomIn();
                }

            case R.id.text_zoom_out:
                if(mActionEditorView instanceof ActionEditorCanvasView){
                    ((ActionEditorCanvasView)mActionEditorView).zoomOut();
                }

            case R.id.text_reset_zoom:
                if(mActionEditorView instanceof ActionEditorCanvasView){
                    ((ActionEditorCanvasView)mActionEditorView).resetView();
                }
            default:
                break;
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
