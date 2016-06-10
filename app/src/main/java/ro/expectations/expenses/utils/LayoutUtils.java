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

package ro.expectations.expenses.utils;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import ro.expectations.expenses.R;

public class LayoutUtils {

    public static int getThemeAttributeValue(Context context, @AttrRes int attrId) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrId, value, true);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        return (int) value.getDimension(metrics);
    }

    public static void changeAppBarHeight(Context context, AppBarLayout appBarLayout, boolean expand) {

        int height;
        if (expand) {
            height = (int) context.getResources().getDimension(R.dimen.app_bar_extended_height);
        } else {
            height = LayoutUtils.getThemeAttributeValue(context, R.attr.actionBarSize);
        }

        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        p.height = height;
        appBarLayout.setLayoutParams(p);

        if (expand) {
            appBarLayout.setExpanded(false, false);
        }
    }
}
