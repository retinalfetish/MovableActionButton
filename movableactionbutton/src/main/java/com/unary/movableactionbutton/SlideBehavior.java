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

import android.content.Context;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

/**
 * A behavior used by CoordinatorLayout to slide an obstructing view out of the way of a Snackbar.
 *
 * @param <V> View type parameter.
 */
public class SlideBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private boolean mAnimate;
    private float mTranslationY;
    private int mPointerId;
    private float mOffsetX;
    private float mOffsetY;

    /**
     * Simple constructor to use when creating the behavior from code.
     */
    public SlideBehavior() {
        super();
    }

    /**
     * Constructor that is called when inflating the behavior from XML.
     *
     * @param context Context given for the behavior. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public SlideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull V child) {
        if (mAnimate) {
            child.setTranslationY(mTranslationY);
        }

        return super.onSaveInstanceState(parent, child);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                actionDown(parent, child, event);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                actionUp(parent, child, event);
                break;
        }

        return false;
    }

    /**
     * Determine if the child might be associated with the event and save that for later.
     *
     * @param parent Parent receiving the event.
     * @param child  Child view for this behavior.
     * @param event  MotionEvent object.
     */
    private void actionDown(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        mPointerId = MotionEvent.INVALID_POINTER_ID;

        // Could be multiple events
        if (parent.isPointInChildBounds(child, (int) event.getX(pointerIndex), (int) event.getY(pointerIndex))) {
            mPointerId = event.getPointerId(pointerIndex);

            mOffsetX = child.getTranslationX();
            mOffsetY = child.getTranslationY();
        }
    }

    /**
     * Detach from the Snackbar if the child has been moved since beginning the animation.
     *
     * @param parent Parent receiving the event.
     * @param child  Child view for this behavior.
     * @param event  MotionEvent object.
     */
    private void actionUp(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        boolean touched = (mPointerId == event.getPointerId(event.getActionIndex()));

        // Abort out of the animation
        if (touched && (mOffsetX != child.getTranslationX() || mOffsetY != child.getTranslationY())) {
            mAnimate = false;
        }
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();

        // Only animate if blocking
        if (!mAnimate && getViewRectF(child).intersect(getViewRectF(dependency))) {
            mTranslationY = child.getTranslationY();
            mAnimate = true;

            return false;
        }

        // Piggyback the animator
        if (mAnimate) {
            child.setTranslationY(1 + mTranslationY + dependency.getTranslationY() - dependency.getHeight() - lp.bottomMargin);
        }

        return mAnimate;
    }

    @Override
    public void onDependentViewRemoved(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        // Snackbar might get dismissed
        if (mAnimate) {
            child.animate().translationY(mTranslationY).setDuration(0).start();
        }

        mAnimate = false;
    }

    /**
     * Utility method to find a view's rectangular coordinates relative to the parent view group.
     *
     * @param view Child view.
     * @return Location relative to parent.
     */
    @NonNull
    private static RectF getViewRectF(@NonNull View view) {
        return new RectF(view.getX(), view.getY(), view.getX() + view.getWidth(), view.getY() + view.getHeight());
    }
}