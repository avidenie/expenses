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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Map;

import ro.expectations.expenses.R;
import ro.expectations.expenses.utils.ColorStyleUtils;

public class ColorPickerDialogFragment extends DialogFragment {

    public interface Listener {
        void onColorSelected(int targetRequestCode, @ColorRes int colorId, @StyleRes int style);
    }

    private static final String ARG_SELECTED_STYLE = "selected_style";

    private Listener mCallback;
    private ColorPickerAdapter mAdapter;

    public static ColorPickerDialogFragment newInstance(String style) {
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_STYLE, style);

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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_fragment_color_picker, null);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        String[] styleNames = getResources().getStringArray(R.array.colorPickerStyles);
        int[] colorPickerStyles = new int[styleNames.length];
        for (int i = 0; i < styleNames.length; i++) {
            colorPickerStyles[i] = ColorStyleUtils.getIdentifier(getActivity(), styleNames[i]);
        }

        mAdapter = new ColorPickerAdapter(getActivity(), colorPickerStyles, new ColorPickerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    dismiss();
                } else {
                    mAdapter.setItemSelected(position, true);
                    @StyleRes int selectedStyle = mAdapter.getItem(position);
                    Map<String, Integer> colors = ColorStyleUtils.getColorsFromStyle(getActivity(), selectedStyle);
                    mCallback.onColorSelected(getTargetRequestCode(),
                            colors.get(ColorStyleUtils.COLOR_PRIMARY),
                            selectedStyle);
                    dismiss();
                }
            }
        });

        String selectedStyle = getArguments().getString(ARG_SELECTED_STYLE);
        for(int i = 0, n = styleNames.length; i < n; i++) {
            if (styleNames[i].equals(selectedStyle)) {
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
