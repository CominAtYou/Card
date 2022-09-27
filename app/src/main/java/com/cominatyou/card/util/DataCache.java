package com.cominatyou.card.util;

import android.content.Context;

import java.nio.file.Files;
import java.nio.file.Path;

public class DataCache {
    public static String get(Context context, String name) {
        final Path path = context.getCacheDir().toPath().resolve(name);

        if (!Files.exists(path)) {
            return null;
        }

        try {
            if (Files.getLastModifiedTime(path).toMillis() < System.currentTimeMillis() - 1000 * 60 * 5) {
                Files.delete(path);
                return null;
            }

            return new String(Files.readAllBytes(path));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean set(Context context, String name, String content) {
        final Path path = context.getCacheDir().toPath().resolve(name);

        try {
            Files.write(path, content.getBytes());
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
