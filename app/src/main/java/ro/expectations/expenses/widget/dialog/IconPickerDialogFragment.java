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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import ro.expectations.expenses.R;

public class IconPickerDialogFragment extends DialogFragment {

    public interface Listener {
        void onIconSelected(int targetRequestCode, String icon);
    }

    private Listener mCallback;
    private IconPickerAdapter mAdapter;

    public static IconPickerDialogFragment newInstance() {
        IconPickerDialogFragment iconPickerDialogFragment = new IconPickerDialogFragment();
        iconPickerDialogFragment.setCancelable(true);
        return iconPickerDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment target = getTargetFragment();
        if (target instanceof Listener) {
            mCallback = (Listener) getTargetFragment();
        } else {
            throw new RuntimeException(getTargetFragment().toString()
                    + " must implement IconPickerDialogFragment.Listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_icon_picker, null);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        String[] icons = getResources().getStringArray(R.array.iconPicker);
        String[] iconTitles = getResources().getStringArray(R.array.iconPickerTitle);
        mAdapter = new IconPickerAdapter(getActivity(), icons, iconTitles, new IconPickerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mCallback.onIconSelected(getTargetRequestCode(), mAdapter.getIcon(position));
                dismiss();
            }
        });

        recyclerView.setAdapter(mAdapter);

        builder.setTitle(getString(R.string.choose_icon));
        builder.setView(view);

        return builder.create();
    }
}
