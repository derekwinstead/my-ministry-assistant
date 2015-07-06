package tester.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class UIUtils {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }
}