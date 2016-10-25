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

package ro.expectations.expenses.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.TypedValue;

public class ColorUtils {

    @ColorInt
    public static int fromRGB(String color, @ColorInt int defaultColor) {
        if (color == null) {
            return defaultColor;
        }
        try {
            if (color.charAt(0) != '#') {
                color = "#" + color;
            }
            return Color.parseColor(color);
        } catch(IllegalArgumentException e) {
            return defaultColor;
        }
    }

    public static String toRGB(@ColorInt int color) {
        return String.format("%06x", 0XFF000000 | color);
    }

    @ColorRes
    public static int getColorFromTheme(Context context, @AttrRes int color) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(color, typedValue, true);
        return typedValue.data;
    }
}
