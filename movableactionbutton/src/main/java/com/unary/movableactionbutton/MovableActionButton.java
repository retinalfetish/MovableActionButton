/*
 * Copyright 2020 Christopher Zaborsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unary.movableactionbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.stateful.ExtendableSavedState;

/**
 * A simple Material ActionButton widget extended from FloatingActionButton and stylized as such.
 * The view can be repositioned by the user within the confines of the parent.
 *
 * <p><strong>XML attributes</strong></p>
 * <p>The following attributes in addition to the FloatingActionButton can modify the view:</p>
 * <pre>
 *   app:movable="boolean"       // Allow a clickable view to be moved
 *   app:movingAlpha="float"     // Multiplier used for moving. Default is "0.5"
 *
 *   android:clickable="boolean" // Default true for AppCompat themes
 * </pre>
 * <p>See {@link R.styleable#MovableActionButton MovableActionButton Attributes}, {@link R.styleable#View View Attributes}</p>
 */
public class MovableActionButton extends FloatingActionButton {

    private static final boolean MOVABLE = true;
    private static final float MOVING_ALPHA = 0.5f;
    private static final boolean CLICKABLE = true;

    private boolean mMovable;
    private float mMovingAlpha;
    private float mDefaultAlpha;
    private int mTouchSlop;
    private PointF mPortraitOrientation;
    private PointF mLandscapeOrientation;
    private float mDx;
    private float mDy;
    private float mStartX;
    private float mStartY;
    private boolean mMoving;
    private CoordinatorLayout.Behavior<FloatingActionButton> mBehavior;
    private OnMoveListener mOnMoveListener;

    /**
     * Simple constructor to use when creating the view from code.
     *
     * @param context Context given for the view. This determines the resources and theme.
     */
    public MovableActionButton(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context Context given for the view. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public MovableActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    /**
     * Constructor called when inflating from XML and applying a style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    public MovableActionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Shared method to initialize the member variables from the XML and provide default values.
     * Input values are not checked for sanity.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MovableActionButton, defStyleAttr, 0);
        boolean clickable;

        try {
            mMovable = typedArray.getBoolean(R.styleable.MovableActionButton_movable, MOVABLE);
            mMovingAlpha = typedArray.getFloat(R.styleable.MovableActionButton_movingAlpha, MOVING_ALPHA);

            clickable = typedArray.getBoolean(R.styleable.MovableActionButton_android_clickable, CLICKABLE);
        } finally {
            typedArray.recycle();
        }

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        // Create reusable point objects
        mPortraitOrientation = new PointF();
        mLandscapeOrientation = new PointF();

        // Create a default behavior
        mBehavior = new SlideBehavior<>(context, attrs);

        // Needed when using AppCompat theme
        setClickable(clickable);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    protected Parcelable onSaveInstanceState() {
        ExtendableSavedState savedState = new ExtendableSavedState(super.onSaveInstanceState());
        Bundle bundle = new Bundle();

        // Uninitialized zero is okay
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mPortraitOrientation.x = getTranslationX();
                mPortraitOrientation.y = getTranslationY();
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mLandscapeOrientation.x = getTranslationX();
                mLandscapeOrientation.y = getTranslationY();
                break;
        }

        // (Re)save both orientations
        bundle.putParcelable("portrait", mPortraitOrientation);
        bundle.putParcelable("landscape", mLandscapeOrientation);

        savedState.extendableStates.put(getClass().getSimpleName(), bundle);

        return savedState;
    }

    @SuppressLint("SwitchIntDef")
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ExtendableSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        ExtendableSavedState savedState = (ExtendableSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        Bundle bundle = savedState.extendableStates.get(getClass().getSimpleName());

        // (Re)restore both orientations
        mPortraitOrientation = bundle.getParcelable("portrait");
        mLandscapeOrientation = bundle.getParcelable("landscape");

        // Uninitialized zero is okay
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setTranslationX(mPortraitOrientation.x);
                setTranslationY(mPortraitOrientation.y);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setTranslationX(mLandscapeOrientation.x);
                setTranslationY(mLandscapeOrientation.y);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(event);
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Save the default alpha and initial touch coordinates for the view.
     *
     * @param event MotionEvent object.
     */
    private void actionDown(@NonNull MotionEvent event) {
        mDefaultAlpha = getAlpha();
        mMoving = false;

        mStartX = event.getRawX();
        mStartY = event.getRawY();

        mDx = getX() - mStartX;
        mDy = getY() - mStartY;
    }

    /**
     * Move the view to the given coordinates while staying within the bounds of the parent view.
     *
     * @param event MotionEvent object.
     */
    private void actionMove(@NonNull MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();

        if (mMoving || Math.abs(mStartX - x) > mTouchSlop || Math.abs(mStartY - y) > mTouchSlop) {
            if (mMovable && onMoveEvent(event)) {
                setAlpha(mDefaultAlpha * mMovingAlpha);

                // Update the view location
                animate().x(getInsideParentX(mDx + x)).y(getInsideParentY(mDy + y)).setDuration(0).start();

                mMoving = true;
            }
        }
    }

    /**
     * Restore the default alpha and abort the click if the view was moved.
     *
     * @param event MotionEvent object.
     */
    private void actionUp(@NonNull MotionEvent event) {
        if (mMoving && onMoveEvent(event)) {
            setAlpha(mDefaultAlpha);

            // Eat the click
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
    }

    /**
     * Update the onMove listener with the current MotionEvent. This event can fire even if the view
     * was not further displaced.
     *
     * @param event MotionEvent object.
     * @return True if event is successful.
     */
    public boolean onMoveEvent(@NonNull MotionEvent event) {
        if (mOnMoveListener != null) {
            return mOnMoveListener.onMove(this, event);
        }

        return true;
    }

    /**
     * Adjust the given X coordinate so it remains inside the bounds of the parent view.
     *
     * @param x The X Axis.
     * @return Bound X axis.
     */
    private float getInsideParentX(float x) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();

        return Math.min(Math.max(x, lp.leftMargin + ((View) getParent()).getPaddingLeft()),
                ((View) getParent()).getWidth() - lp.rightMargin - ((View) getParent()).getPaddingRight() - getWidth());
    }

    /**
     * Adjust the given Y coordinate so it remains inside the bounds of the parent view.
     *
     * @param y The Y Axis.
     * @return Bound Y axis.
     */
    private float getInsideParentY(float y) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();

        return Math.min(Math.max(y, lp.topMargin + ((View) getParent()).getPaddingTop()),
                ((View) getParent()).getHeight() - lp.bottomMargin - ((View) getParent()).getPaddingBottom() - getHeight());
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior<FloatingActionButton> getBehavior() {
        return mBehavior;
    }

    /**
     * Check the movable status. Note that views that are not clickable are also not movable.
     *
     * @return True if moving is enabled.
     */
    public boolean isMovable() {
        return mMovable;
    }

    /**
     * Set the movable status. Note that views that are not clickable are also not movable.
     *
     * @param movable True if moving is enabled.
     */
    public void setMovable(boolean movable) {
        mMovable = movable;
    }

    /**
     * Get the multiplier used for moving. This value ranges from 0 for completely transparent to 1.
     *
     * @return The alpha multiplier.
     */
    public float getMovingAlpha() {
        return mMovingAlpha;
    }

    /**
     * Set the multiplier used for moving. This value ranges from 0 for completely transparent to 1.
     *
     * @param movingAlpha The alpha multiplier.
     */
    public void setMovingAlpha(float movingAlpha) {
        mMovingAlpha = movingAlpha;
    }

    /**
     * Get the move listener for this instance. The interface is used to notify the client of any
     * touch initiated changes.
     *
     * @return Move notification listener.
     */
    @Nullable
    public OnMoveListener getOnMoveListener() {
        return mOnMoveListener;
    }

    /**
     * Set the move listener for this instance. The interface is used to notify the client of any
     * touch initiated changes.
     *
     * @param onMoveListener Move notification listener.
     */
    public void setOnMoveListener(@Nullable OnMoveListener onMoveListener) {
        mOnMoveListener = onMoveListener;
    }
}