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

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import ro.expectations.expenses.R;

public class ColorPickerPalette extends TableLayout {

    private int mSwatchLength;
    private int mMarginSize;
    private int mNumColumns;

    private ColorPickerSwatch.OnColorSelectedListener mOnColorSelectedListener;

    private String mDescription;
    private String mDescriptionSelected;

    public ColorPickerPalette(Context context) {
        super(context);
    }

    public ColorPickerPalette(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(int size, int columns, ColorPickerSwatch.OnColorSelectedListener listener) {

        mNumColumns = columns;
        Resources resources = getResources();

        if (size == ColorPickerDialog.SIZE_LARGE) {
            mSwatchLength = resources.getDimensionPixelSize(R.dimen.color_swatch_large);
            mMarginSize = resources.getDimensionPixelSize(R.dimen.color_swatch_margins_large);
        } else {
            mSwatchLength = resources.getDimensionPixelSize(R.dimen.color_swatch_small);
            mMarginSize = resources.getDimensionPixelSize(R.dimen.color_swatch_margins_small);
        }

        mOnColorSelectedListener = listener;

        mDescription = resources.getString(R.string.color_swatch_description);
        mDescriptionSelected = resources.getString(R.string.color_swatch_description_selected);
    }

    public void drawPalette(int[] colors, int selectedColor) {
        drawPalette(colors, selectedColor, null);
    }

    public void drawPalette(int[] colors, int selectedColor, String[] colorContentDescriptions) {

        if (colors == null) {
            return;
        }

        this.removeAllViews();

        int tableElements = 0;
        int rowElements = 0;
        int rowNumber = 0;

        TableRow row = createTableRow();
        for (int color : colors) {
            View colorSwatch = createColorSwatch(color, selectedColor);
            setSwatchDescription(rowNumber, tableElements, rowElements, color == selectedColor,
                    colorSwatch, colorContentDescriptions);
            addSwatchToRow(row, colorSwatch, rowNumber);

            tableElements++;
            rowElements++;
            if (rowElements == mNumColumns) {
                addView(row);
                row = createTableRow();
                rowElements = 0;
                rowNumber++;
            }
        }

        if (rowElements > 0) {
            while (rowElements != mNumColumns) {
                addSwatchToRow(row, createBlankSpace(), rowNumber);
                rowElements++;
            }
            addView(row);
        }
    }

    private TableRow createTableRow() {
        TableRow row = new TableRow(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(params);
        return row;
    }

    private ColorPickerSwatch createColorSwatch(int color, int selectedColor) {
        ColorPickerSwatch view = new ColorPickerSwatch(getContext(), color,
                color == selectedColor, mOnColorSelectedListener);
        TableRow.LayoutParams params = new TableRow.LayoutParams(mSwatchLength, mSwatchLength);
        params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize);
        view.setLayoutParams(params);
        return view;
    }

    private void setSwatchDescription(int rowNumber, int index, int rowElements, boolean selected,
                                      View swatch, String[] contentDescriptions) {
        String description;
        if (contentDescriptions != null && contentDescriptions.length > index) {
            description = contentDescriptions[index];
        } else {
            int accessibilityIndex;
            if (rowNumber % 2 == 0) {
                // We're in a regular-ordered row
                accessibilityIndex = index + 1;
            } else {
                // We're in a backwards-ordered row.
                int rowMax = ((rowNumber + 1) * mNumColumns);
                accessibilityIndex = rowMax - rowElements;
            }

            if (selected) {
                description = String.format(mDescriptionSelected, accessibilityIndex);
            } else {
                description = String.format(mDescription, accessibilityIndex);
            }
        }
        swatch.setContentDescription(description);
    }

    private static void addSwatchToRow(TableRow row, View swatch, int rowNumber) {
        if (rowNumber % 2 == 0) {
            row.addView(swatch);
        } else {
            row.addView(swatch, 0);
        }
    }

    private ImageView createBlankSpace() {
        ImageView view = new ImageView(getContext());
        TableRow.LayoutParams params = new TableRow.LayoutParams(mSwatchLength, mSwatchLength);
        params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize);
        view.setLayoutParams(params);
        return view;
    }
}
