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

package ro.expectations.expenses.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorRes;
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

public class ColorPickerDialogFragment extends DialogFragment {

    public interface Listener {
        void onColorSelected(int targetRequestCode, @ColorRes int colorId, @ColorRes int darkColorId, @ColorRes int accentColorId);
    }

    private static final String ARG_SELECTED_COLOR = "selected_color";

    private Listener mCallback;
    private ColorPickerAdapter mAdapter;

    public static ColorPickerDialogFragment newInstance(int color) {
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTED_COLOR, color);

        ColorPickerDialogFragment colorPickerDialogFragment = new ColorPickerDialogFragment();
        colorPickerDialogFragment.setArguments(args);
        colorPickerDialogFragment.setCancelable(true);
        return colorPickerDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment target = getTargetFragment();
        if (target instanceof Listener) {
            mCallback = (Listener) getTargetFragment();
        } else {
            throw new RuntimeException(getTargetFragment().toString()
                    + " must implement ColorPickerDialogFragment.Listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_color_picker, null);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        int[] colors = getResources().getIntArray(R.array.colorPicker);
        final int[] darkColors = getResources().getIntArray(R.array.colorPickerDark);
        final int[] accentColors = getResources().getIntArray(R.array.colorPickerAccent);
        mAdapter = new ColorPickerAdapter(getActivity(), colors, new ColorPickerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    dismiss();
                } else {
                    mAdapter.setItemSelected(position, true);
                    mCallback.onColorSelected(getTargetRequestCode(),
                            (int) mAdapter.getItemId(position),
                            darkColors[position],
                            accentColors[position]);
                    dismiss();
                }
            }
        });

        int selectedColor = getArguments().getInt(ARG_SELECTED_COLOR, 0);
        for(int i = 0, n = colors.length; i < n; i++) {
            if (colors[i] == selectedColor) {
                mAdapter.setItemSelected(i, true);
                break;
            }
        }

        recyclerView.setAdapter(mAdapter);

        builder.setTitle(getString(R.string.choose_color));
        builder.setView(view);

        return builder.create();
    }
}
