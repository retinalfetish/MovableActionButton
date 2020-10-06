package com.unary.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.unary.movableactionbutton.ExtendedMovableActionButton;
import com.unary.movableactionbutton.MovableActionButton;
import com.unary.movableactionbutton.OnMoveListener;

public class MainActivity extends AppCompatActivity implements OnMoveListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MovableActionButton fab = findViewById(R.id.fab);
        ExtendedMovableActionButton extendedFab = findViewById(R.id.extended_fab);

        // Set the move listeners
        fab.setOnMoveListener(this);
        extendedFab.setOnMoveListener(this);

        // Disallow widget positioning
        //fab.setMovable(false);

        // Change the moving opacity
        //fab.setMovingAlpha(0.25f);

        // Change the view behavior
        //((CoordinatorLayout.LayoutParams) fab.getLayoutParams()).setBehavior(new ShrinkBehavior<>());
        //((CoordinatorLayout.LayoutParams) extendedFab.getLayoutParams()).setBehavior(new ShrinkBehavior<>());
    }

    @Override
    public boolean onMove(@NonNull View view, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Snackbar.make(view, "Moved to " + view.getX() + " " + view.getY(), Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .show();
        }

        return true;
    }
}