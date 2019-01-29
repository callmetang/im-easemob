package com.im.test.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 *
 * @author a
 * @date 2019/1/28
 */

public class DialogUtils {
    public static void showDialog(Context mContext,
                                  String title, String message,
                                  String negativeText, String positiveText, DialogInterface.OnClickListener negativeListener
            , DialogInterface.OnClickListener positiveListener) {
        if (mContext == null) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setNegativeButton(negativeText, negativeListener);
        builder.setPositiveButton(positiveText, positiveListener);
        builder.show();

    }
}
