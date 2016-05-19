package ro.expectations.expenses.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ro.expectations.expenses.R;

public class AlertDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    public static AlertDialogFragment newInstance(String title, String message, boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);

        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.setArguments(args);
        alertDialogFragment.setCancelable(cancelable);
        return alertDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        String title = arguments.getString(ARG_TITLE, null);
        String message = arguments.getString(ARG_MESSAGE, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        builder.setPositiveButton(getString(R.string.button_ok), null);
        return builder.create();
    }
}
