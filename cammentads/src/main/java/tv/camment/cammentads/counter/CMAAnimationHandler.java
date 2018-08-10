/*
 * Created by Camment OY on 08/10/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 * Based on: https://github.com/jakob-grabner/Circle-Progress-View by Jakob Grabner
 * licensed under The MIT License (MIT)
 */

package tv.camment.cammentads.counter;

import android.animation.TimeInterpolator;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.lang.ref.WeakReference;

public class CMAAnimationHandler extends Handler {

    private final WeakReference<CMABannerProgressView> mCircleViewWeakReference;
    // Spin bar length in degree at start of animation
    private float mSpinningBarLengthStart;
    private long mAnimationStartTime;
    private long mLengthChangeAnimationStartTime;
    private TimeInterpolator mLengthChangeInterpolator = new DecelerateInterpolator();
    // The interpolator for value animations
    private TimeInterpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private double mLengthChangeAnimationDuration;
    private long mFrameStartTime = 0;

    CMAAnimationHandler(CMABannerProgressView circleView) {
        super(circleView.getContext().getMainLooper());
        mCircleViewWeakReference = new WeakReference<>(circleView);
    }

    @Override
    public void handleMessage(Message msg) {
        CMABannerProgressView circleView = mCircleViewWeakReference.get();
        if (circleView == null) {
            return;
        }

        CMAAnimationMsg msgType = CMAAnimationMsg.values()[msg.what];
        if (msgType == CMAAnimationMsg.TICK) {
            removeMessages(CMAAnimationMsg.TICK.ordinal()); // necessary to remove concurrent ticks.
        }

        mFrameStartTime = SystemClock.uptimeMillis();
        switch (circleView.mAnimationState) {
            case IDLE:
                switch (msgType) {
                    case START_SPINNING:
                        enterSpinning(circleView);
                        break;
                    case STOP_SPINNING:
                        break;
                    case SET_VALUE:
                        setValue(msg, circleView);
                        break;
                    case SET_VALUE_ANIMATED:
                        enterSetValueAnimated(msg, circleView);
                        break;
                    case TICK:
                        removeMessages(CMAAnimationMsg.TICK.ordinal());
                        break;
                }
                break;
            case SPINNING:
                switch (msgType) {
                    case START_SPINNING:
                        break;
                    case STOP_SPINNING:
                        removeCallbacksAndMessages(null);
                        enterEndSpinning(circleView);
                        break;
                    case SET_VALUE:
                        setValue(msg, circleView);
                        break;
                    case SET_VALUE_ANIMATED:
                        enterEndSpinningStartAnimating(circleView, msg);
                        break;
                    case TICK:
                        float length_delta = circleView.mSpinningBarLengthCurrent - circleView.mSpinningBarLengthOrig;
                        float t = (float) ((System.currentTimeMillis() - mLengthChangeAnimationStartTime)
                                / mLengthChangeAnimationDuration);
                        t = t > 1.0f ? 1.0f : t;
                        float interpolatedRatio = mLengthChangeInterpolator.getInterpolation(t);

                        if (Math.abs(length_delta) < 1) {
                            //spinner length is within bounds
                            circleView.mSpinningBarLengthCurrent = circleView.mSpinningBarLengthOrig;
                        } else if (circleView.mSpinningBarLengthCurrent < circleView.mSpinningBarLengthOrig) {
                            //spinner to short, --> grow
                            circleView.mSpinningBarLengthCurrent = mSpinningBarLengthStart + ((circleView.mSpinningBarLengthOrig - mSpinningBarLengthStart) * interpolatedRatio);
                        } else {
                            //spinner to long, --> shrink
                            circleView.mSpinningBarLengthCurrent = (mSpinningBarLengthStart - ((mSpinningBarLengthStart - circleView.mSpinningBarLengthOrig) * interpolatedRatio));
                        }

                        circleView.mCurrentSpinnerDegreeValue += circleView.mSpinSpeed; // spin speed value (in degree)

                        if (circleView.mCurrentSpinnerDegreeValue > 360) {
                            circleView.mCurrentSpinnerDegreeValue = 0;
                        }
                        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
                        circleView.invalidate();
                        break;
                }

                break;
            case END_SPINNING:
                switch (msgType) {
                    case START_SPINNING:
                        circleView.mAnimationState = CMAAnimationState.SPINNING;
                        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
                        break;
                    case STOP_SPINNING:
                        break;
                    case SET_VALUE:
                        setValue(msg, circleView);
                        break;
                    case SET_VALUE_ANIMATED:
                        enterEndSpinningStartAnimating(circleView, msg);
                        break;
                    case TICK:
                        float t = (float) ((System.currentTimeMillis() - mLengthChangeAnimationStartTime)
                                / mLengthChangeAnimationDuration);
                        t = t > 1.0f ? 1.0f : t;
                        float interpolatedRatio = mLengthChangeInterpolator.getInterpolation(t);
                        circleView.mSpinningBarLengthCurrent = (mSpinningBarLengthStart) * (1f - interpolatedRatio);

                        circleView.mCurrentSpinnerDegreeValue += circleView.mSpinSpeed; // spin speed value (not in percent)
                        if (circleView.mSpinningBarLengthCurrent < 0.01f) {
                            //end here, spinning finished
                            circleView.mAnimationState = CMAAnimationState.IDLE;
                        }
                        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
                        circleView.invalidate();
                        break;
                }

                break;
            case END_SPINNING_START_ANIMATING:
                switch (msgType) {
                    case START_SPINNING:
                        circleView.mDrawBarWhileSpinning = false;
                        enterSpinning(circleView);
                        break;
                    case STOP_SPINNING:
                        break;
                    case SET_VALUE:
                        circleView.mDrawBarWhileSpinning = false;
                        setValue(msg, circleView);
                        break;
                    case SET_VALUE_ANIMATED:
                        circleView.mValueFrom = 0; // start from zero after spinning
                        circleView.mValueTo = ((float[]) msg.obj)[1];
                        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
                        break;
                    case TICK:
                        //shrink spinner till it has its original length
                        if (circleView.mSpinningBarLengthCurrent > circleView.mSpinningBarLengthOrig && !circleView.mDrawBarWhileSpinning) {
                            //spinner to long, --> shrink
                            float t = (float) ((System.currentTimeMillis() - mLengthChangeAnimationStartTime)
                                    / mLengthChangeAnimationDuration);
                            t = t > 1.0f ? 1.0f : t;
                            float interpolatedRatio = mLengthChangeInterpolator.getInterpolation(t);
                            circleView.mSpinningBarLengthCurrent = (mSpinningBarLengthStart) * (1f - interpolatedRatio);
                        }

                        // move spinner for spin speed value (not in percent)
                        circleView.mCurrentSpinnerDegreeValue += circleView.mSpinSpeed;

                        //if the start of the spinner reaches zero, start animating the value
                        if (circleView.mCurrentSpinnerDegreeValue > 360 && !circleView.mDrawBarWhileSpinning) {
                            mAnimationStartTime = System.currentTimeMillis();
                            circleView.mDrawBarWhileSpinning = true;
                            initReduceAnimation(circleView);
                        }

                        //value is already animating, calc animation value and reduce spinner
                        if (circleView.mDrawBarWhileSpinning) {
                            circleView.mCurrentSpinnerDegreeValue = 360;
                            circleView.mSpinningBarLengthCurrent -= circleView.mSpinSpeed;
                            calcNextAnimationValue(circleView);

                            float t = (float) ((System.currentTimeMillis() - mLengthChangeAnimationStartTime)
                                    / mLengthChangeAnimationDuration);
                            t = t > 1.0f ? 1.0f : t;
                            float interpolatedRatio = mLengthChangeInterpolator.getInterpolation(t);
                            circleView.mSpinningBarLengthCurrent = (mSpinningBarLengthStart) * (1f - interpolatedRatio);
                        }

                        //spinner is no longer visible switch state to animating
                        if (circleView.mSpinningBarLengthCurrent < 0.1) {
                            //spinning finished, start animating the current value
                            circleView.mAnimationState = CMAAnimationState.ANIMATING;
                            circleView.invalidate();
                            circleView.mDrawBarWhileSpinning = false;
                            circleView.mSpinningBarLengthCurrent = circleView.mSpinningBarLengthOrig;

                        } else {
                            circleView.invalidate();
                        }
                        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
                        break;
                }

                break;
            case ANIMATING:
                switch (msgType) {
                    case START_SPINNING:
                        enterSpinning(circleView);
                        break;
                    case STOP_SPINNING:
                        //Ignore, not spinning
                        break;
                    case SET_VALUE:
                        setValue(msg, circleView);
                        break;
                    case SET_VALUE_ANIMATED:
                        mAnimationStartTime = System.currentTimeMillis();
                        //restart animation from current value
                        circleView.mValueFrom = circleView.mCurrentValue;
                        circleView.mValueTo = ((float[]) msg.obj)[1];

                        break;
                    case TICK:
                        if (calcNextAnimationValue(circleView)) {
                            //animation finished
                            circleView.mAnimationState = CMAAnimationState.IDLE;
                            circleView.mCurrentValue = circleView.mValueTo;
                        }
                        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
                        circleView.invalidate();
                        break;
                }
                break;
        }
    }

    private void enterSetValueAnimated(Message msg, CMABannerProgressView circleView) {
        circleView.mValueFrom = ((float[]) msg.obj)[0];
        circleView.mValueTo = ((float[]) msg.obj)[1];
        mAnimationStartTime = System.currentTimeMillis();
        circleView.mAnimationState = CMAAnimationState.ANIMATING;
        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
    }

    private void enterEndSpinningStartAnimating(CMABannerProgressView circleView, Message msg) {
        circleView.mAnimationState = CMAAnimationState.END_SPINNING_START_ANIMATING;
        circleView.mValueFrom = 0; // start from zero after spinning
        circleView.mValueTo = ((float[]) msg.obj)[1];

        mLengthChangeAnimationStartTime = System.currentTimeMillis();
        mSpinningBarLengthStart = circleView.mSpinningBarLengthCurrent;

        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
    }

    private void enterEndSpinning(CMABannerProgressView circleView) {
        circleView.mAnimationState = CMAAnimationState.END_SPINNING;
        initReduceAnimation(circleView);
        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
    }

    private void initReduceAnimation(CMABannerProgressView circleView) {
        float degreesTillFinish = circleView.mSpinningBarLengthCurrent;
        float stepsTillFinish = degreesTillFinish / circleView.mSpinSpeed;
        mLengthChangeAnimationDuration = (stepsTillFinish * circleView.mFrameDelayMillis) * 2f;

        mLengthChangeAnimationStartTime = System.currentTimeMillis();
        mSpinningBarLengthStart = circleView.mSpinningBarLengthCurrent;
    }

    private void enterSpinning(CMABannerProgressView circleView) {
        circleView.mAnimationState = CMAAnimationState.SPINNING;
        circleView.mSpinningBarLengthCurrent = (360f / circleView.mMaxValue * circleView.mCurrentValue);
        circleView.mCurrentSpinnerDegreeValue = (360f / circleView.mMaxValue * circleView.mCurrentValue);
        mLengthChangeAnimationStartTime = System.currentTimeMillis();
        mSpinningBarLengthStart = circleView.mSpinningBarLengthCurrent;

        //calc animation time
        float stepsTillFinish = circleView.mSpinningBarLengthOrig / circleView.mSpinSpeed;
        mLengthChangeAnimationDuration = ((stepsTillFinish * circleView.mFrameDelayMillis) * 2f);

        sendEmptyMessageDelayed(CMAAnimationMsg.TICK.ordinal(), circleView.mFrameDelayMillis - (SystemClock.uptimeMillis() - mFrameStartTime));
    }

    private boolean calcNextAnimationValue(CMABannerProgressView circleView) {
        float t = (float) ((System.currentTimeMillis() - mAnimationStartTime)
                / circleView.mAnimationDuration);
        t = t > 1.0f ? 1.0f : t;
        float interpolatedRatio = mInterpolator.getInterpolation(t);

        circleView.mCurrentValue = (circleView.mValueFrom + ((circleView.mValueTo - circleView.mValueFrom) * interpolatedRatio));

        return t >= 1;
    }

    private void setValue(Message msg, CMABannerProgressView circleView) {
        circleView.mValueFrom = circleView.mValueTo;
        circleView.mCurrentValue = circleView.mValueTo = ((float[]) msg.obj)[0];
        circleView.mAnimationState = CMAAnimationState.IDLE;
        circleView.invalidate();
    }
}
