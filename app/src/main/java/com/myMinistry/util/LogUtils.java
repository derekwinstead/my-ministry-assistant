package com.myMinistry.util;

import android.util.Log;

public class LogUtils {
    private static final String LOG_PREFIX = "myministryassistant_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 43;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    @SuppressWarnings("rawtypes")
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }

    private LogUtils() {
    }
}
