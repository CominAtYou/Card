package com.cominatyou.card.util;

import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;

public class TextUtil {
    public static Spannable removeUnderlinesFromLinks(Spannable spannable) {
        final URLSpan[] urlSpans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan span : urlSpans) {
            int startIndex = spannable.getSpanStart(span);
            int endIndex = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            spannable.setSpan(new URLSpanNoUnderline(span.getURL()), startIndex, endIndex, 0);
        }
        return spannable;
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint drawState) {
            super.updateDrawState(drawState);
            drawState.setUnderlineText(false);
        }
    }
}
