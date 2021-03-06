# MovableActionButton
A simple Material ActionButton widget extended from [FloatingActionButton](https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton) and stylized as such. The view can be repositioned by the user within the confines of the parent.
## Screenshots
<img src="/art/screenshot-animation.gif" alt="Screenshot" height=600>

## Usage
The library is part of [JCenter](https://bintray.com/rogue/maven/com.unary:movableactionbutton) (a default repository) and can be included in your project by adding `implementation 'com.unary:movableactionbutton:1.0.2'` as a module dependency. The latest build can also be found at [JitPack](https://jitpack.io/#com.unary/movableactionbutton).
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
This widget has a number of options that can be configured in both the XML and code. An example app is provided in the project repository to illustrate its use and the `OnMoveListener` interface.
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.unary.movableactionbutton.MovableActionButton
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:src="@drawable/ic_baseline_add_24"
        app:tint="@android:color/white" />
        
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```
Changing behaviors in XML:
```
app:layout_behavior="@string/movableactionbutton_slide_behavior"
app:layout_behavior="@string/movableactionbutton_shrink_behavior"
```
The listener interface:
```
fab.setOnMoveListener(this);

@Override
public boolean onMove(View view, MotionEvent event) { ... }
```

### XML attributes
The following attributes in addition to the FloatingActionButton can modify the view:
```
app:movable="boolean"       // Allow a clickable view to be moved
app:movingAlpha="float"     // Multiplier used for moving. Default is "0.5"
app:useMargins="boolean"    // Observe the layout margins when moving

android:clickable="boolean" // Default true for AppCompat themes
```
