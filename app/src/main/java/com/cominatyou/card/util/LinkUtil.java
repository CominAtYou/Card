package com.cominatyou.card.util;

import android.text.Html;
import android.text.Spannable;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class LinkUtil {
    public static Spannable addHyperlinks(@NonNull JSONArray urls, String description) {
        for (int i = 0; i < urls.length(); i++) {
            final JSONObject urlObject = urls.optJSONObject(i);
            // link as html
            description = description.replace(urlObject.optString("url"), String.format(Locale.getDefault(), "<a href=\"%s\">%s</a>", urlObject.optString("url"), urlObject.optString("display_url")));
        }

        Spannable text = (Spannable) Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT);

        return TextUtil.removeUnderlinesFromLinks(text);
    }
}
