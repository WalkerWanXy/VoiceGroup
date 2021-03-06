package com.view.dansesshou.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by dxs on 2018/5/17.
 */

public class BatteryRound extends View {
    private RectF batteryRecf;
    private RectF batterCenterRecf;
    private Paint mPaint;
    private Paint mTextPaint;
    private float ratio = 0.4f;
    private int lineW = 1;
    private float lineRatio = 0.5f;
    private int textSize = 10;
    private int[][] col = new int[][]{{20, 100}, {Color.parseColor("#ff0000"), Color.parseColor("#89c646")}};
    private int currentValue = 100;
    PorterDuffXfermode modeTx = new PorterDuffXfermode(PorterDuff.Mode.XOR);
    private boolean isShowCurrentValue;//是否展示文字
    private Paint mChargePaint;
    private boolean isShowCharge;//是否展示充电图标

    public BatteryRound(Context context) {
        this(context, null);
    }

    public BatteryRound(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        lineW = dip2px(context, lineW);
        batteryRecf = new RectF();
        batterCenterRecf = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#4ec200"));
        mPaint.setStrokeWidth(lineW);
//        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(dip2px(context, textSize));
//        mTextPaint.setXfermode(modeTx);
        mTextPaint.setColor(Color.parseColor("#4ec200"));

        mChargePaint = new Paint();
        mChargePaint.setAntiAlias(true);
        mChargePaint.setColor(Color.parseColor("#FFFFFF"));
        mChargePaint.setStyle(Paint.Style.FILL);

        //关闭硬件加速（神坑）
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heighMode = MeasureSpec.getMode(heightMeasureSpec);
        int heighSize = MeasureSpec.getSize(heightMeasureSpec);
        int mesW, mesH;
        if (widthMode == MeasureSpec.EXACTLY) {
            mesW = widthSize;
        } else {
            mesW = dip2px(getContext(), 100);
        }

        if (heighMode == MeasureSpec.EXACTLY) {
            mesH = heighSize;
        } else {
            mesH = (int) (mesW * ratio);
        }
        setMeasuredDimension(mesW, mesH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float batterW = w - 2 * lineW;
        float batterH = batterW * ratio;
        batteryRecf.set(lineW / 2, (h - batterH) / 2, w - lineW / 2, (h + batterH) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBattery(canvas);
        resetTextPaint();
        drawBatteryCennter(canvas);
        if (isShowCurrentValue) {
            drawText(canvas);
            drawSendText(canvas);
        }
        if (isShowCharge) {
            drawCharge(canvas);
        }
    }

    private void drawText(Canvas canvas) {
        if(currentValue==100){
            mTextPaint.setColor(Color.parseColor("#ff7f00"));
        }
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        int baseLineY = (int) (batteryRecf.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式
        String text = getFormatText(currentValue);
        canvas.drawText(text, batteryRecf.centerX(), baseLineY, mTextPaint);
    }

    private void drawSendText(Canvas canvas){
        mTextPaint.setColor(Color.WHITE);
        canvas.save();
        canvas.clipRect(batteryRecf.left,batteryRecf.top,batteryRecf.right*currentValue/100,batteryRecf.bottom);
        drawText(canvas);
        canvas.restore();
    }

    private void resetTextPaint() {
        for (int i = 0; i < col[0].length; i++) {
            if (currentValue >= 0 && currentValue <= col[0][i]) {
                if (isShowCharge) {
                    mPaint.setColor(Color.parseColor("#89c646"));
                    mTextPaint.setColor(Color.parseColor("#FFFFFF"));
                }else {
                    mTextPaint.setColor(col[1][i]);
                    mPaint.setColor(col[1][i]);
                }
                break;
            }
        }
    }

    public void setCol(int[][] col) {
        this.col = col;
    }

    private void drawBattery(Canvas canvas) {
        if (isShowCharge) {
            mPaint.setColor(Color.WHITE);
        }else {
            if(currentValue==0){
                mPaint.setColor(col[1][0]);
            }else {
                mPaint.setColor(Color.WHITE);
            }
        }
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(batteryRecf, batteryRecf.height() / 2, batteryRecf.height() / 2, mPaint);
    }

    private void drawBatteryCennter(Canvas canvas) {
        //应产品经理需求 屏蔽百分百展示为白色的需求
//        if(currentValue==100){
//            mTextPaint.setColor(Color.WHITE);
//        }
        canvas.save();
        mPaint.setStyle(Paint.Style.FILL);
        batterCenterRecf.set(batteryRecf.left + lineW, batteryRecf.top + lineW, batteryRecf.right - lineW, batteryRecf.bottom - lineW);
        canvas.clipRect(batterCenterRecf.left,batterCenterRecf.top,batterCenterRecf.right*currentValue/100,batterCenterRecf.bottom);
        canvas.drawRoundRect(batterCenterRecf,batterCenterRecf.height()/2,batterCenterRecf.height()/2, mPaint);
        canvas.restore();
    }

    /**
     * 画电池充电的闪电图标
     * @param canvas
     */
    private void drawCharge(Canvas canvas){
        canvas.save();
        canvas.translate(getMeasuredWidth()/2,getMeasuredHeight()/2);
        //一共为6个点
        Path path = new Path();
        path.moveTo(7,(0-batterCenterRecf.height()/2+6));
        path.lineTo(4,-2);
        path.lineTo(8,2);
        path.lineTo(-7, batterCenterRecf.height()/2-6);
        path.lineTo(-4,2);
        path.lineTo(-8,-2);
        path.close();
        canvas.drawPath(path,mChargePaint);
        canvas.restore();
    }

    public void setCurrentValue(int value) {
        this.currentValue = value;
        invalidate();
    }

    public void setCurrentValue(String text) {
        if(TextUtils.isEmpty(text)){
            return;
        }
        try {
            this.currentValue=Integer.parseInt(text);
            invalidate();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static int dip2px(Context context, int dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private String getFormatText(int value) {
        return "" + value + "%";
    }

    public boolean isShowCurrentValue() {
        return isShowCurrentValue;
    }

    public void setShowCurrentValue(boolean showCurrentValue) {
        isShowCurrentValue = showCurrentValue;
        invalidate();
    }

    public boolean isShowCharge() {
        return isShowCharge;
    }

    public void setShowCharge(boolean showCharge) {
        isShowCharge = showCharge;
        if (showCharge) {
            startAnimation();
        }else {
            stopAnimation();
        }
        invalidate();
    }

    public void startAnimation(){
        chargingAnimation.setRepeatCount(-1);
        chargingAnimation.setRepeatMode(Animation.RESTART);
        chargingAnimation.setInterpolator(new LinearInterpolator());
        chargingAnimation.setDuration(1000);
        this.startAnimation(chargingAnimation);
    }

    public void stopAnimation(){
        chargingAnimation.cancel();
        this.clearAnimation(); 
    }

    private Animation chargingAnimation = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            currentValue= (int) (interpolatedTime*100);
            invalidate();
        }
    };
}
