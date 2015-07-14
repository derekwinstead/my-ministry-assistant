package com.myMinistry.util;

import android.util.Log;

public class LogUtils {
    private static final String LOG_PREFIX = "myministryassistant_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 43;

    /**
     * Don't use this when obfuscating class names!
     */
    @SuppressWarnings("rawtypes")
    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }

    private LogUtils() {
    }
}
