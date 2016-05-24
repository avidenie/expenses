/*
 * Copyright © 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses.  If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.widget.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

public class ConfirmationDialogFragment extends DialogFragment {

    public interface Listener {
        void onConfirmed(int targetRequestCode);
        void onDenied(int targetRequestCode);
    }

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_BUTTON_LABEL = "positive_button_label";
    private static final String ARG_NEGATIVE_BUTTON_LABEL = "negative_button_label";

    private Listener callback;

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
        if (target instanceof Listener) {
            callback = (Listener) getTargetFragment();
        } else {
            throw new RuntimeException(getTargetFragment().toString()
                    + " must implement ConfirmationDialogFragment.Listener");
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
                callback.onConfirmed(getTargetRequestCode());
            }
        });
        builder.setNegativeButton(arguments.getString(ARG_NEGATIVE_BUTTON_LABEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onDenied(getTargetRequestCode());
            }
        });
        return builder.create();
    }
}
