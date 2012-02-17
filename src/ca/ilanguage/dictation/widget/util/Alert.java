package ca.ilanguage.dictation.widget.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.R;

public class Alert {

	public static void showAlert(Context con, String title, String message) {
		Dialog dlg = new AlertDialog.Builder(con).setIcon(
				R.drawable.ic_dialog_alert).setTitle(title).setPositiveButton(
				"OK", null).setMessage(message).create();
		dlg.show();
	}

	public static void showAlert(Context con, String title, String message,
			CharSequence positiveButtontxt,
			DialogInterface.OnClickListener positiveListener,
			CharSequence negativeButtontxt,
			DialogInterface.OnClickListener negativeListener) {
		Dialog dlg = new AlertDialog.Builder(con).setIcon(
				R.drawable.ic_dialog_alert).setTitle(title).setNegativeButton(
				negativeButtontxt, negativeListener).setPositiveButton(
				positiveButtontxt, positiveListener).setMessage(message)
				.create();
		dlg.show();
	}
}