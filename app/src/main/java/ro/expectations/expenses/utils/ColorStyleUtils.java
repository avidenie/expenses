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
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ro.expectations.expenses.R;

public class ColorStyleUtils {

    public static final String COLOR_PRIMARY = "colorPrimary";
    public static final String COLOR_PRIMARY_DARK = "colorPrimaryDark";
    public static final String COLOR_ACCENT = "colorAccent";

    public static @StyleRes int getIdentifier(Context context, String styleName, @StyleRes int defaultResourceId) {
        int styleResourceId = context.getResources().getIdentifier(styleName, "style", context.getPackageName());
        if (styleResourceId == 0) {
            styleResourceId = defaultResourceId;
        }

        return styleResourceId;
    }

    public static @StyleRes int getIdentifier(Context context, String styleName) {
        return getIdentifier(context, styleName, R.style.AppTheme);
    }

    public static Map<String, Integer> getColorsFromStyle(Context context, @StyleRes int styleResId) {

        int[] attrs = {
                R.attr.colorPrimary,
                R.attr.colorPrimaryDark,
                R.attr.colorAccent
        };
        Arrays.sort(attrs);

        TypedArray ta = context.obtainStyledAttributes(styleResId, attrs);

        Map<String, Integer> colors = new HashMap<>(3);
        colors.put(COLOR_PRIMARY, ta.getColor(Arrays.binarySearch(attrs, R.attr.colorPrimary), ContextCompat.getColor(context, R.color.colorPrimary)));
        colors.put(COLOR_PRIMARY_DARK, ta.getColor(Arrays.binarySearch(attrs, R.attr.colorPrimaryDark), ContextCompat.getColor(context, R.color.colorPrimaryDark)));
        colors.put(COLOR_ACCENT, ta.getColor(Arrays.binarySearch(attrs, R.attr.colorAccent), ContextCompat.getColor(context, R.color.colorAccent)));

        ta.recycle();

        return colors;
    }

    public static Map<String, Integer> getColorsFromStyle(Context context, String styleName) {
        return getColorsFromStyle(context, getIdentifier(context, styleName));
    }

    @ColorRes public static int getColorFromTheme(Context context, @AttrRes int color) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(color, typedValue, true);
        return typedValue.data;
    }
}
