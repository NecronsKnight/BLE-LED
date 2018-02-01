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

public class MyView extends View {
    private Paint mPaint;
    private Paint myPaint;
    private int x;
    private int y;
    private int maxx;
    private int maxy;
    private int poindx;
    private int poindy;
    private LinkedList xxxList;
    private LinkedList yyyList;
    private LinkedList leftList;
    private LinkedList topList;
    private LinkedList rightList;
    private LinkedList bottomList;
    int lineX = 64;
    int lineY = 32;

    public MyView(Context context, AttributeSet attrs) {
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
        maxx = getMeasuredWidth();
        maxy = getMeasuredHeight();
    }

    /**
     * 测量宽
     *
     * @param widthMeasureSpec
     */
    private int measureWidth(int widthMeasureSpec) {
        xxxList = new LinkedList();
        yyyList = new LinkedList();
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
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#000000"));
        mPaint.setStrokeWidth(1f);
        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(Color.parseColor("#000000"));
        myPaint.setStrokeWidth(1f);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        x = maxx / lineX;
        y = maxy / lineY;
        poindx = maxx - x * lineX;
        poindy = maxy - y * lineY;
        for (int a = 0; a < lineX; a++) {
            xxxList.add((poindx / 2) + (x * a));
            canvas.drawLine((poindx / 2) + (x * a), (poindy / 2), (poindx / 2) + (x * a), maxy - (poindy / 2), mPaint);
        }
        xxxList.add((poindx / 2) + (x * lineX));
        canvas.drawLine((poindx / 2) + (x * lineX), (poindy / 2), (poindx / 2) + (x * lineX), maxy - (poindy / 2), mPaint);
        for (int a = 0; a < lineY; a++) {
            yyyList.add((poindy / 2) + (y * a));
            canvas.drawLine((poindx / 2), (poindy / 2) + (y * a), maxx - (poindx / 2), (poindy / 2) + (y * a), mPaint);
        }
        yyyList.add((poindy / 2) + (y * lineY));
        canvas.drawLine((poindx / 2), (poindy / 2) + (y * lineY), maxx - (poindx / 2), (poindy / 2) + (y * lineY), mPaint);
    }

    public LinkedList getxList() {
        return xxxList;
    }

    public LinkedList getyList() {
        return yyyList;
    }

    public void setLineX(int a) {
        this.lineX=a;
    }

    public void setLineY(int a) {
        this.lineY=a;
    }
}
