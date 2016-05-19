package ro.expectations.expenses.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import ro.expectations.expenses.R;

public class ConfirmationDialogFragment extends DialogFragment {

    public interface OnConfirmationListener {
        void onConfirmation(int targetRequestCode);
    }

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_BUTTON_LABEL = "positive_button_label";
    private static final String ARG_NEGATIVE_BUTTON_LABEL = "negative_button_label";

    private OnConfirmationListener callback;

    public static ConfirmationDialogFragment newInstance(String title, String message,
                                                  String positiveButtonLabel,
                                                  String negativeButtonLabel, boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POSITIVE_BUTTON_LABEL, positiveButtonLabel);
        args.putString(ARG_NEGATIVE_BUTTON_LABEL, negativeButtonLabel);

        ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
        confirmationDialogFragment.setArguments(args);
        confirmationDialogFragment.setCancelable(cancelable);
        return confirmationDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment target = getTargetFragment();
        if (target instanceof OnConfirmationListener) {
            callback = (OnConfirmationListener) getTargetFragment();
        } else {
            throw new RuntimeException(getTargetFragment().toString()
                    + " must implement ConfirmationDialogFragment.OnConfirmationListener");
        }
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
        builder.setPositiveButton(arguments.getString(ARG_POSITIVE_BUTTON_LABEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onConfirmation(getTargetRequestCode());
            }
        });
        builder.setNegativeButton(arguments.getString(ARG_NEGATIVE_BUTTON_LABEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
