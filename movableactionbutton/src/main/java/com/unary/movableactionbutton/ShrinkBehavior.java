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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

/**
 * A behavior used by CoordinatorLayout to shrink an obstructing view out of the way of a Snackbar.
 *
 * @param <V> View type parameter.
 */
public class ShrinkBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private boolean mAnimate;
    private float mScaleX;
    private float mScaleY;

    /**
     * Simple constructor to use when creating the behavior from code.
     */
    public ShrinkBehavior() {
        super();
    }

    /**
     * Constructor that is called when inflating the behavior from XML.
     *
     * @param context Context given for the behavior. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public ShrinkBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull V child) {
        if (mAnimate) {
            child.setScaleX(mScaleX);
            child.setScaleY(mScaleY);
        }

        return super.onSaveInstanceState(parent, child);
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
            mScaleX = child.getScaleX();
            mScaleY = child.getScaleY();
            mAnimate = true;

            return false;
        }

        // Piggyback the animator
        if (mAnimate) {
            child.setScaleX(mScaleX * (1 + dependency.getTranslationY()) / (dependency.getHeight() + lp.bottomMargin));
            child.setScaleY(mScaleY * (1 + dependency.getTranslationY()) / (dependency.getHeight() + lp.bottomMargin));
        }

        return mAnimate;
    }

    @Override
    public void onDependentViewRemoved(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        // Snackbar might get dismissed
        if (mAnimate) {
            child.animate().scaleX(mScaleX).scaleY(mScaleY).setDuration(0).start();
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