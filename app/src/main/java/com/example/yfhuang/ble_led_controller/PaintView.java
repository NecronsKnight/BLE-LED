package com.example.yfhuang.ble_led_controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by yfhuang on 11/17/2017.
 */

public class PaintView extends View {
    private Paint myPaint;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private LinkedList leftList;
    private LinkedList topList;
    private LinkedList rightList;
    private LinkedList bottomList;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        init();
        leftList = new LinkedList();
        topList = new LinkedList();
        rightList = new LinkedList();
        bottomList = new LinkedList();
        setMeasuredDimension(measureWidth(widthMeasureSpec), measuredHeight(heightMeasureSpec));
        getMeasuredWidth();
        getMeasuredHeight();
    }

    /**
     * 测量宽
     *
     * @param widthMeasureSpec
     */
    private int measureWidth(int widthMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * 测量高
     *
     * @param heightMeasureSpec
     */
    private int measuredHeight(int heightMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void init() {
        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(Color.parseColor("#000000"));
        myPaint.setStrokeWidth(1f);
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (leftList != null && topList != null && rightList != null && bottomList != null) {
            if (leftList.size() > 0 && topList.size() > 0 && rightList.size() > 0 && bottomList.size() > 0) {
                for (int a = 0; a < leftList.size(); a++) {
                    try {
                        int z = Integer.parseInt(leftList.get(a).toString());
                        int s = Integer.parseInt(topList.get(a).toString());
                        int y = Integer.parseInt(rightList.get(a).toString());
                        int x = Integer.parseInt(bottomList.get(a).toString());
                        canvas.drawRect(z, s, y, x, myPaint);
                    } catch (Exception e) {

                    }
                }
            }
        }
        if (right != 0 && bottom != 0) {
            canvas.drawRect(left, top, right, bottom, myPaint);
        }
    }

    public void setleft(int a) {
        if (leftList == null) {
            leftList = new LinkedList();
            leftList.add(a + "");
        } else {
            leftList.add(a + "");
        }
        this.left = a;

    }

    public void settop(int a) {
        if (topList == null) {
            topList = new LinkedList();
            topList.add(a + "");
        } else {
            topList.add(a + "");
        }
        this.top = a;

    }

    public void setright(int a) {
        if (rightList == null) {
            rightList = new LinkedList();
            rightList.add(a + "");
        } else {
            rightList.add(a + "");
        }
        this.right = a;

    }

    public void setbottom(int a) {
        if (bottomList == null) {
            bottomList = new LinkedList();
            bottomList.add(a + "");
        } else {
            bottomList.add(a + "");
        }
        this.bottom = a;

    }

    public void clearList() {
        leftList.clear();
        topList.clear();
        rightList.clear();
        bottomList.clear();
        right = 0;
        bottom = 0;
    }

}

