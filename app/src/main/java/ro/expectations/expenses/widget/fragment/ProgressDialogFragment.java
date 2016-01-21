package ro.expectations.expenses.widget.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ProgressDialogFragment newInstance(String message) {
        return newInstance(message, true);
    }

    public static ProgressDialogFragment newInstance(String message, boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);

        ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setArguments(args);
        progressDialogFragment.setCancelable(cancelable);
        return progressDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString(ARG_MESSAGE, null);
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        if(message != null) {
            progressDialog.setMessage(message);
        }
        progressDialog.setIndeterminate(true);
        return progressDialog;
    }
}