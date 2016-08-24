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
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import ro.expectations.expenses.R;

public class AlertDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private DialogInterface.OnClickListener mOnClickListener = null;
    private DialogInterface.OnDismissListener mOnDismissListener = null;

    public static AlertDialogFragment newInstance(String title, String message, boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);

        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.setArguments(args);
        alertDialogFragment.setCancelable(cancelable);
        return alertDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment target = getTargetFragment();

        if (target instanceof DialogInterface.OnClickListener) {
            mOnClickListener = (DialogInterface.OnClickListener) target;
        }

        if (target instanceof DialogInterface.OnDismissListener) {
            mOnDismissListener = (DialogInterface.OnDismissListener) target;
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

        builder.setPositiveButton(getString(R.string.button_ok), mOnClickListener);

        if (mOnDismissListener != null) {
            builder.setOnDismissListener(mOnDismissListener);
        }

        return builder.create();
    }
}
