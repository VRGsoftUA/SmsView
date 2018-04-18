package net.vrgsoft.smsview;

import android.content.Context;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * helper class for keyboard hide/show operations
 */

public final class KeyboardHelper {
    private KeyboardHelper() {}
    public static void hideKeyboard(Context context, IBinder view) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view, 0);
        }
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    public static void showKeyboard(Context context, IBinder view) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInputFromInputMethod(view, 0);
        }
    }

    public static void hideKeyboard(Fragment fragment) {
        View view = fragment.getView();
        if (view != null) {
            hideKeyboard(fragment.getContext(), view.getWindowToken());
        }
    }
}
