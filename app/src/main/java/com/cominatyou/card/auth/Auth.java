package com.cominatyou.card.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import com.cominatyou.card.util.Config;

import java.security.SecureRandom;
import java.util.Base64;

public class Auth {
    public static void authenticate(Activity activity) {
        // generate PKCE code verifier
        SecureRandom secureRandom = new SecureRandom();
        byte[] code = new byte[64];
        char[] pkceValidChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~".toCharArray();
        secureRandom.nextBytes(code);
        for (int i = 0; i < code.length; i++) {
            code[i] = (byte) pkceValidChars[code[i] & 63];
        }

        final String codeVerifier = new String(code);

        // generate PKCE code challenge
        byte[] bytes = codeVerifier.getBytes();
        byte[] digest = null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            digest = md.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        activity.getSharedPreferences("auth", Activity.MODE_PRIVATE).edit().putString("code_verifier", codeVerifier).apply();

        String url = "https://twitter.com/i/oauth2/authorize?client_id=" + Config.CLIENT_ID + "&response_type=code&redirect_uri=com.cominatyou.card-auth%3A%2F%2Fcallback&code_challenge_method=S256&code_challenge=" + codeChallenge + "&scope=tweet.read%20tweet.write%20users.read%20follows.read%20follows.write%20mute.read%20mute.write%20like.read%20offline.access%20like.write%20block.read%20block.write%20bookmark.write%20bookmark.read&state=state";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
        activity.finish();
    }
}