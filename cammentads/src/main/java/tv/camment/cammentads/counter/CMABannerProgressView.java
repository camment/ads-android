/*
 * Created by Camment OY on 08/10/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 * Based on: https://github.com/jakob-grabner/Circle-Progress-View by Jakob Grabner
 * licensed under The MIT License (MIT)
 */

package tv.camment.cammentads.counter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Message;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;

public class CMABannerProgressView extends View {

    //Colors (with defaults)
    private int mSpinnerColor = 0xFFFFFFFF;
    private int mTextColor = 0xFFFFFFFF;
    protected int mLayoutHeight = 0;
    protected int mLayoutWidth = 0;
    //Rectangles
    protected RectF mCircleBounds = new RectF();
    protected RectF mInnerCircleBound = new RectF();
    protected PointF mCenter;
    protected RectF mOuterTextBounds = new RectF();
    protected RectF mActualTextBounds = new RectF();
    //value animation
    float mCurrentValue = 0;
    float mValueTo = 0;
    float mValueFrom = 0;
    float mMaxValue = 100;
    float mMinValueAllowed = 0;
    float mMaxValueAllowed = -1;
    // spinner animation
    float mSpinningBarLengthCurrent = 0;
    float mSpinningBarLengthOrig = 42;
    float mCurrentSpinnerDegreeValue = 0;
    //Animation
    //The amount of degree to move the bar by on each draw
    float mSpinSpeed = 2.8f;
    //Enable spin
    boolean mSpin = false;
    double mAnimationDuration = 900;
    //The number of milliseconds to wait in between each draw
    int mFrameDelayMillis = 10;
    // helper for CMAAnimationState.END_SPINNING_START_ANIMATING
    boolean mDrawBarWhileSpinning;
    //The animation handler containing the animation state machine.
    CMAAnimationHandler mAnimationHandler = new CMAAnimationHandler(this);
    //The current state of the animation state machine.
    CMAAnimationState mAnimationState = CMAAnimationState.IDLE;
    private int mBarWidth = 20;
    private int mStartAngle = 270;
    private int mAdditionalSpaceWidth = 5;
    //Default text sizes
    private int mTextSize = 42;
    //Text scale
    private int mBackgroundCircleColor = 0x80000000;  //transparent
    //Caps
    private Paint.Cap mBarStrokeCap = Paint.Cap.BUTT;
    private Paint.Cap mSpinnerStrokeCap = Paint.Cap.BUTT;
    //Paints
    private Paint mBarPaint = new Paint();
    private Paint mShaderlessBarPaint;
    private Paint mBarSpinnerPaint = new Paint();
    private Paint mBackgroundCirclePaint = new Paint();
    private Paint mTextPaint = new Paint();
    //Other
    // The text to show
    private int mTextLength;
    //clipping
    private Bitmap mClippingBitmap;
    private Paint mMaskPaint;

    private DecimalFormat decimalFormat = new DecimalFormat("0");

    public CMABannerProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint.setFilterBitmap(false);
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setupPaints();

        if (mSpin) {
            spin();
        }
    }

    public void setMaxValue(@FloatRange(from = 0) float _maxValue) {
        mMaxValue = _maxValue;
    }

    public void setValueAnimated(float _valueFrom, float _valueTo, long _animationDuration) {
        // respect min and max values allowed
        _valueTo = Math.max(mMinValueAllowed, _valueTo);

        if (mMaxValueAllowed >= 0)
            _valueTo = Math.min(mMaxValueAllowed, _valueTo);

        mAnimationDuration = _animationDuration;
        Message msg = new Message();
        msg.what = CMAAnimationMsg.SET_VALUE_ANIMATED.ordinal();
        msg.obj = new float[]{_valueFrom, _valueTo};
        mAnimationHandler.sendMessage(msg);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Share the dimensions
        mLayoutWidth = w;
        mLayoutHeight = h;

        setupBounds();
        setupBarPaint();

        if (mClippingBitmap != null) {
            mClippingBitmap = Bitmap.createScaledBitmap(mClippingBitmap, getWidth(), getHeight(), false);
        }

        invalidate();
    }

    private RectF getInnerCircleRect(RectF _circleBounds) {

        double circleWidth = +_circleBounds.width() - mBarWidth;
        double width = ((circleWidth / 2d) * Math.sqrt(2d));
        float widthDelta = (_circleBounds.width() - (float) width) / 2f;

        float scaleX = 1;
        float scaleY = 1;
        return new RectF(_circleBounds.left + (widthDelta * scaleX), _circleBounds.top + (widthDelta * scaleY), _circleBounds.right - (widthDelta * scaleX), _circleBounds.bottom - (widthDelta * scaleY));
    }

    private void setTextSizeAndTextBoundsWithFixedTextSize(String text) {
        mTextPaint.setTextSize(mTextSize);
        mActualTextBounds = calcTextBounds(text, mTextPaint, mCircleBounds); //center text in circle
    }

    private RectF calcTextBounds(String _text, Paint _textPaint, RectF _textBounds) {
        Rect textBoundsTmp = new Rect();

        //get current text bounds
        _textPaint.getTextBounds(_text, 0, _text.length(), textBoundsTmp);
        float width = textBoundsTmp.left + textBoundsTmp.width();
        float height = textBoundsTmp.bottom + textBoundsTmp.height() * 0.93f; // the height of calcTextBounds is a bit to high, therefore  * 0.93
        //center in circle
        RectF textRect = new RectF();
        textRect.left = (_textBounds.left + ((_textBounds.width() - width) / 2));
        textRect.top = _textBounds.top + ((_textBounds.height() - height) / 2);
        textRect.right = textRect.left + width;
        textRect.bottom = textRect.top + height;

        return textRect;
    }

    private void setupBounds() {
        // Width should equal to Height, find the min value to setup the circle
        int minValue = Math.min(mLayoutWidth, mLayoutHeight);

        // Calc the Offset if needed
        int xOffset = mLayoutWidth - minValue;
        int yOffset = mLayoutHeight - minValue;

        // Add the offset
        float paddingTop = this.getPaddingTop() + (yOffset / 2);
        float paddingBottom = this.getPaddingBottom() + (yOffset / 2);
        float paddingLeft = this.getPaddingLeft() + (xOffset / 2);
        float paddingRight = this.getPaddingRight() + (xOffset / 2);

        int width = getWidth(); //this.getLayoutParams().width;
        int height = getHeight(); //this.getLayoutParams().height;

        float circleWidthHalf = mBarWidth / 2f;

        mCircleBounds = new RectF(paddingLeft + circleWidthHalf,
                paddingTop + circleWidthHalf,
                width - paddingRight - circleWidthHalf,
                height - paddingBottom - circleWidthHalf);


        mInnerCircleBound = new RectF(paddingLeft + (mBarWidth + mAdditionalSpaceWidth),
                paddingTop + (mBarWidth + mAdditionalSpaceWidth),
                width - paddingRight - (mBarWidth + mAdditionalSpaceWidth),
                height - paddingBottom - (mBarWidth + mAdditionalSpaceWidth));
        mOuterTextBounds = getInnerCircleRect(mCircleBounds);

        mCenter = new PointF(mCircleBounds.centerX(), mCircleBounds.centerY());
    }

    public void setupPaints() {
        setupBarPaint();
        setupBarSpinnerPaint();
        setupTextPaint();
        setupBackgroundCirclePaint();
    }

    private void setupBarPaint() {
        mBarPaint.setColor(mSpinnerColor);
        mBarPaint.setShader(null);
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStrokeCap(mBarStrokeCap);
        mBarPaint.setStyle(Style.STROKE);
        mBarPaint.setStrokeWidth(mBarWidth);

        if (mBarStrokeCap != Paint.Cap.BUTT) {
            mShaderlessBarPaint = new Paint(mBarPaint);
            mShaderlessBarPaint.setShader(null);
            mShaderlessBarPaint.setColor(mSpinnerColor);
        }
    }

    private void setupTextPaint() {
        mTextPaint.setSubpixelText(true);
        mTextPaint.setLinearText(true);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
    }

    private void setupBackgroundCirclePaint() {
        mBackgroundCirclePaint.setColor(mBackgroundCircleColor);
        mBackgroundCirclePaint.setAntiAlias(true);
        mBackgroundCirclePaint.setStyle(Style.FILL);
    }

    private void setupBarSpinnerPaint() {
        mBarSpinnerPaint.setAntiAlias(true);
        mBarSpinnerPaint.setStrokeCap(mSpinnerStrokeCap);
        mBarSpinnerPaint.setStyle(Style.STROKE);
        mBarSpinnerPaint.setStrokeWidth(mBarWidth);
        mBarSpinnerPaint.setColor(mSpinnerColor);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float degrees = (360f / mMaxValue * mCurrentValue);

        // Draw the background circle
        if (mBackgroundCircleColor != 0) {
            canvas.drawArc(mInnerCircleBound, 360, 360, false, mBackgroundCirclePaint);
        }

        //Draw spinner
        if (mAnimationState == CMAAnimationState.SPINNING || mAnimationState == CMAAnimationState.END_SPINNING) {
            drawSpinner(canvas);

        } else if (mAnimationState == CMAAnimationState.END_SPINNING_START_ANIMATING) {
            //draw spinning arc
            drawSpinner(canvas);

            if (mDrawBarWhileSpinning) {
                drawBar(canvas, degrees);
                drawTextWithUnit(canvas);
            }
        } else {
            drawBar(canvas, degrees);
            drawTextWithUnit(canvas);
        }

        if (mClippingBitmap != null) {
            canvas.drawBitmap(mClippingBitmap, 0, 0, mMaskPaint);
        }
    }

    private void drawSpinner(Canvas canvas) {
        if (mSpinningBarLengthCurrent < 0) {
            mSpinningBarLengthCurrent = 1;
        }

        float startAngle = mStartAngle + mCurrentSpinnerDegreeValue - mSpinningBarLengthCurrent;

        canvas.drawArc(mCircleBounds, startAngle, mSpinningBarLengthCurrent, false,
                mBarSpinnerPaint);
    }

    private void drawTextWithUnit(Canvas canvas) {
        //set text
        String text = decimalFormat.format(mCurrentValue);

        // only re-calc position and size if string length changed
        if (mTextLength != text.length()) {
            mTextLength = text.length();
            if (mTextLength == 1) {
                mOuterTextBounds = getInnerCircleRect(mCircleBounds);
                mOuterTextBounds = new RectF(mOuterTextBounds.left + (mOuterTextBounds.width() * 0.1f), mOuterTextBounds.top, mOuterTextBounds.right - (mOuterTextBounds.width() * 0.1f), mOuterTextBounds.bottom);
            } else {
                mOuterTextBounds = getInnerCircleRect(mCircleBounds);
            }
            setTextSizeAndTextBoundsWithFixedTextSize(text);
        }

        canvas.drawText(text, mActualTextBounds.left - (mTextPaint.getTextSize() * 0.02f), mActualTextBounds.bottom, mTextPaint);
    }

    private void drawBar(Canvas _canvas, float _degrees) {
        float startAngle = mStartAngle;
        _canvas.drawArc(mCircleBounds, startAngle, _degrees, false, mBarPaint);
    }

    public void stopSpinning() {
        setSpin(false);
        mAnimationHandler.sendEmptyMessage(CMAAnimationMsg.STOP_SPINNING.ordinal());
    }

    public void spin() {
        setSpin(true);
        mAnimationHandler.sendEmptyMessage(CMAAnimationMsg.START_SPINNING.ordinal());
    }

    private void setSpin(boolean spin) {
        mSpin = spin;
    }
}
