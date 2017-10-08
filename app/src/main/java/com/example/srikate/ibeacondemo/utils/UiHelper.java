package com.example.srikate.ibeacondemo.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.srikate.ibeacondemo.R;


/**
 * Created by srikate on 4/10/2017 AD.
 */

public class UiHelper {
    private static ProgressDialog mProgressDialog;
    private static AlertDialog.Builder builder;

    public static void showLoading(Context context) {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                dismissLoading();
            }
            mProgressDialog = ProgressDialog.show(context, "", "Loading...");
        } catch (Exception e) {

        }
    }

    public static void dismissLoading() {
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {

        }
    }

    public static void showErrorMessage(Context context, String message) {
        showErrorMessage(context, message, false);
    }

    public static void showErrorMessage(Context context, String message, boolean cancelable) {
        showErrorMessage(context, message, cancelable, null);
    }

    public static void showErrorMessage(Context context, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, listener);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void showInformationMessage(Context context, String title, String message, boolean cancelable) {
        showInformationMessage(context, title, message, cancelable, null);
    }

    public static void showInformationMessage(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, listener);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void showConfirmDialog(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        showConfirmDialog(context, title, message, false, listener);
    }

    public static void showConfirmDialog(Context context,String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, listener);
        builder.setNegativeButton(R.string.btn_cancel, listener);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void showConfirmDialogWithAlertIcon(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        showConfirmDialogWithAlertIcon(context, title, message, false, listener);
    }

    public static void showConfirmDialogWithAlertIcon(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.btn_ok, listener);
        builder.setNegativeButton(R.string.btn_cancel, listener);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void showYesNoDialog(Context context, String message, DialogInterface.OnClickListener listener) {
        showYesNoDialog(context, message, false, listener);
    }

    public static void showYesNoDialog(Context context, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_yes, listener);
        builder.setNegativeButton(R.string.btn_no, listener);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException e) {

        }
    }
}

