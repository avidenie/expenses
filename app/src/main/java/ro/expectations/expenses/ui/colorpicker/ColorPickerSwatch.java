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
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import ro.expectations.expenses.R;

public class ColorPickerSwatch extends FrameLayout implements View.OnClickListener {

    public interface OnColorSelectedListener {
        void onColorSelected(@ColorInt int color);
    }

    @ColorInt
    private int mColor;
    private ImageView mSwatchImage;
    private ImageView mCheckmarkImage;
    private OnColorSelectedListener mOnColorSelectedListener;

    public ColorPickerSwatch(Context context, @ColorInt int color, boolean checked,
                             OnColorSelectedListener listener) {

        super(context);
        mColor = color;
        mOnColorSelectedListener = listener;

        LayoutInflater.from(context).inflate(R.layout.color_picker_swatch, this);
        mSwatchImage = (ImageView) findViewById(R.id.color_picker_swatch);
        mCheckmarkImage = (ImageView) findViewById(R.id.color_picker_checkmark);

        setColor(color);
        setChecked(checked);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mOnColorSelectedListener != null) {
            mOnColorSelectedListener.onColorSelected(mColor);
        }
    }

    private void setColor(@ColorInt int color) {
        Drawable[] colorDrawable = new Drawable[]
                {ContextCompat.getDrawable(getContext(), R.drawable.color_picker_swatch)};
        mSwatchImage.setImageDrawable(new ColorStateDrawable(colorDrawable, color));
    }

    private void setChecked(boolean checked) {
        if (checked) {
            mCheckmarkImage.setVisibility(View.VISIBLE);
        } else {
            mCheckmarkImage.setVisibility(View.GONE);
        }
    }
}
