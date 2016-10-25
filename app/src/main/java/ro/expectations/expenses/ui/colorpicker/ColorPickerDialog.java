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

package ro.expectations.expenses.ui.colorpicker;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import ro.expectations.expenses.R;

public class ColorPickerDialog extends DialogFragment
        implements ColorPickerSwatch.OnColorSelectedListener {

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    private static final String KEY_COLORS = "colors";
    private static final String KEY_COLOR_CONTENT_DESCRIPTIONS = "color_content_descriptions";
    private static final String KEY_SELECTED_COLOR = "selected_color";
    private static final String KEY_COLUMNS = "columns";
    private static final String KEY_SIZE = "size";

    private int[] mColors = null;
    private String[] mColorContentDescriptions = null;
    private int mSelectedColor;
    private int mColumns;
    private int mSize;

    protected AlertDialog mAlertDialog;

    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;

    protected ColorPickerSwatch.OnColorSelectedListener mListener;

    public ColorPickerDialog() {
        // Empty constructor required for dialog fragments.
    }

    public static ColorPickerDialog newInstance(int[] colors, int selectedColor,
                                                int columns, int size) {

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_COLUMNS, columns);
        bundle.putInt(KEY_SIZE, size);

        colorPickerDialog.setArguments(bundle);
        colorPickerDialog.setColors(colors, selectedColor);

        return colorPickerDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumns = getArguments().getInt(KEY_COLUMNS);
            mSize = getArguments().getInt(KEY_SIZE);
        }

        if (savedInstanceState != null) {
            mColors = savedInstanceState.getIntArray(KEY_COLORS);
            mSelectedColor = (Integer) savedInstanceState.getSerializable(KEY_SELECTED_COLOR);
            mColorContentDescriptions = savedInstanceState.getStringArray(
                    KEY_COLOR_CONTENT_DESCRIPTIONS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        View view = LayoutInflater.from(activity).inflate(R.layout.color_picker_dialog, null);
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, this);

        if (mColors != null) {
            showPaletteView();
        }

        mAlertDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.choose_color)
                .setView(view)
                .create();

        return mAlertDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(KEY_COLORS, mColors);
        outState.putSerializable(KEY_SELECTED_COLOR, mSelectedColor);
        outState.putStringArray(KEY_COLOR_CONTENT_DESCRIPTIONS, mColorContentDescriptions);
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        if (mListener != null) {
            mListener.onColorSelected(color);
        }

        if (getTargetFragment() instanceof ColorPickerSwatch.OnColorSelectedListener) {
            final ColorPickerSwatch.OnColorSelectedListener listener =
                    (ColorPickerSwatch.OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color);
        }

        if (color != mSelectedColor) {
            mSelectedColor = color;
            mPalette.drawPalette(mColors, mSelectedColor);
        }

        dismiss();
    }

    public void setColors(int[] colors, int selectedColor) {
        if (mColors != colors || mSelectedColor != selectedColor) {
            mColors = colors;
            mSelectedColor = selectedColor;
            refreshPalette();
        }
    }

    public void setColors(int[] colors) {
        if (mColors != colors) {
            mColors = colors;
            refreshPalette();
        }
    }

    private void showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.GONE);
            refreshPalette();
            mPalette.setVisibility(View.VISIBLE);
        }
    }

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColor, mColorContentDescriptions);
        }
    }
}
