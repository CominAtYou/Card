package com.cominatyou.card.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import com.cominatyou.card.util.Config;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class Auth {
    private static final OAuthConsumer consumer = new DefaultOAuthConsumer(Config.CONSUMER_KEY, Config.CONSUMER_SECRET);
    private static final OAuthProvider provider = new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token", "https://api.twitter.com/oauth/authorize");

    public static void authenticate(Activity activity) {
        new Thread(() -> {
            final String authUrl;
            try {
                authUrl = provider.retrieveRequestToken(consumer, "com.cominatyou.card.auth://callback");

                activity.runOnUiThread(() -> {
                    CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                    intent.launchUrl(activity, Uri.parse(authUrl));
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected static OAuthConsumer getConsumer() {
        return consumer;
    }

    protected static OAuthProvider getProvider() {
        return provider;
    }
}