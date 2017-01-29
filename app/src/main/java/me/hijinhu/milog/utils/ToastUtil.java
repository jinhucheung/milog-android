package me.hijinhu.milog.utils;

import android.app.Application;
import android.widget.Toast;

/**
 * ToastUtil: Toast Factory
 *
 * Created by kumho on 17-1-15.
 */
public class ToastUtil {
    private static Application context;
    public static boolean isShow = true;

    private ToastUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void initialize(Application context) {
        ToastUtil.context = context;
    }

    public static void showShort(CharSequence message) {
        if (context != null && isShow) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showLong(CharSequence message) {
        if (context != null && isShow) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void show(CharSequence message, int duration) {
        if (context != null && isShow) {
            Toast.makeText(context, message, duration).show();
        }
    }
}
