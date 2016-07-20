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