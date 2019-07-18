package com.mercku.layouthouse;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by yanqiong.ran on 2019-07-12.
 */
public class MainActivity0 extends AppCompatActivity implements View.OnClickListener {
    private ViewGroup mScrollviewLayout;
    private ViewGroup mHouseLayoutView;
    private View mLeftSideView;
private int mCount=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main0);
        findViewById(R.id.text_tap_to_add).setOnClickListener(this);
        findViewById(R.id.text_rect).setOnClickListener(this);
        mLeftSideView = findViewById(R.id.layout_left_side);
        mScrollviewLayout = findViewById(R.id.layout_house);
        mHouseLayoutView = findViewById(R.id.layout_house);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_tap_to_add:
                onClickAdd();
                break;
            case R.id.text_rect:
                onClickAddRectangle();
               /* if(mCount%2==0){
                    onClickAddRectangle();
                    mCount++;
                }else {
                    onClickAddText();
                    mCount++;
                }*/

                break;
            default:
                break;
        }

    }

    private void onClickAddText() {
        TextView textView = new TextView(this);
        textView.setText("LIVING ROOM");
        textView.setTextColor(Color.BLACK);
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //mScrollviewLayout.addView(textView,mCount,layoutParams);
        android.util.Log.d("ryq","onClickAddText mCount="+mCount);
        mScrollviewLayout.addView(textView,0,layoutParams);
    }

    private void onClickAddRectangle() {
        RectView rect = new RectView(this);
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        android.util.Log.d("ryq","onClickAddRectangle mCount="+mCount);
        mScrollviewLayout.addView(rect,0,layoutParams);
    }

    private void onClickAdd() {
        if (mLeftSideView.getVisibility() == View.VISIBLE) {
            mLeftSideView.setVisibility(View.GONE);
        } else {
            mLeftSideView.setVisibility(View.VISIBLE);
        }
    }
}
