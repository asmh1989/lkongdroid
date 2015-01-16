package org.cryse.lkong.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;

public class ToastProxy {
    public static void showToast(Context context, String text, int toastType) {
        int textColor;
        int backgroundColor;
        int actionColor;
        switch (toastType) {
            case ToastSupport.TOAST_ALERT:
                textColor = Color.RED;
                break;
            case ToastSupport.TOAST_CONFIRM:
                textColor = Color.BLUE;
                break;
            case ToastSupport.TOAST_INFO:
                textColor = Color.WHITE;
                break;
            default:
                textColor = Color.WHITE;
        }

        if(context instanceof Activity)
            Snackbar.with(context) // context
                    .text(text) // text to be displayed
                    .textColor(textColor) // change the text color
                    // .color(Color.BLUE) // change the background color
                    .show((Activity)context); // activity where it is displayed
        else
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

    }
}