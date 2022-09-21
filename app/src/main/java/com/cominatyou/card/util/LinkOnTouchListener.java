package com.cominatyou.card.util;

import android.annotation.SuppressLint;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class LinkOnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    private static final View.OnTouchListener listener = (v, event) -> {
        boolean returnValue = false;
        CharSequence text = ((TextView) v).getText();
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
        TextView textView = (TextView) v;
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(textView);
                }
                returnValue = true;
            }
        }
        return returnValue;
    };

    public static View.OnTouchListener getListener() {
        return listener;
    }
}
