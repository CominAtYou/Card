package com.cominatyou.card.activityhelpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.RecyclerView;

public class WindowUtil {
    /*
       Add padding to the recyclerview to account for the navigation bars.
       This is definitely not how you're supposed to do it, and I literally should not be parsing a string for this.
       However, there is no better API to accomplish this task (I spent forever fighting WindowInsets)
       and I honestly am just fine with this; I just have to hope Google never breaks it
    */
    public static void addPaddingForBottom(Context context, RecyclerView view) {
        @SuppressLint("InternalInsetResource") int something = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        int sbHeightInDp = Integer.parseInt(context.getString(something).substring(0, 2)); // would it kill to make a method that returns the size in pixels
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int sbHeightInPx = Math.round(sbHeightInDp * dm.xdpi / DisplayMetrics.DENSITY_DEFAULT);
        int bottomNavigationHeightPx = Math.round(80 * dm.xdpi / DisplayMetrics.DENSITY_DEFAULT);
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), sbHeightInPx + bottomNavigationHeightPx);
    }
}
