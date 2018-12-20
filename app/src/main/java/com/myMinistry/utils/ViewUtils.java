package com.myMinistry.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.R;

import androidx.core.content.ContextCompat;

public class ViewUtils {
    public static float pxToDp(float px) {
        float densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        return px / (densityDpi / 160f);
    }

    public static int dpToPx(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static void changeIconDrawableToGray(Context context, Drawable drawable) {
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(ContextCompat
                    .getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void Toast(Context mContext, CharSequence message, int length) {
        Toast toast = Toast.makeText(mContext, message, length);
        View view = toast.getView();
        //Gets the actual oval background of the Toast then sets the colour filter
        view.getBackground().setColorFilter(mContext.getResources().getColor(R.color.alert_bg), PorterDuff.Mode.SRC_IN);
        //Gets the TextView from the Toast so it can be edited
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(mContext.getResources().getColor(R.color.bpWhite));
        toast.show();
    }

    public static void Toast(Context mContext, int redId, int length) {
        Toast toast = Toast.makeText(mContext, redId, length);
        View view = toast.getView();
        //Gets the actual oval background of the Toast then sets the colour filter
        view.getBackground().setColorFilter(mContext.getResources().getColor(R.color.alert_bg), PorterDuff.Mode.SRC_IN);
        //Gets the TextView from the Toast so it can be edited
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(mContext.getResources().getColor(R.color.bpWhite));
        toast.show();
    }

    private ViewUtils() {
        // This utility class is not publicly instantiable
    }
}
