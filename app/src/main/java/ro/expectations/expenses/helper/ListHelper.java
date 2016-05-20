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

package ro.expectations.expenses.helper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import ro.expectations.expenses.R;

public class ListHelper {

    public static void setItemBackground(Context context, View itemView, boolean selected) {
        setItemBackground(context, itemView, selected, null, null);
    }

    public static void setItemBackground(Context context, View itemView, boolean selected,
            @Nullable ViewGroup itemIcon, @Nullable ViewGroup selectedIcon) {

        TypedValue iconBackgroundTypedValue = new TypedValue();
        if (selected) {
            if (itemIcon != null) {
                itemIcon.setVisibility(View.GONE);
            }
            if (selectedIcon != null) {
                selectedIcon.setVisibility(View.VISIBLE);
            }
            context.getTheme().resolveAttribute(android.R.attr.activatedBackgroundIndicator, iconBackgroundTypedValue, true);
            itemView.setActivated(true);
        } else {
            if (itemIcon != null) {
                itemIcon.setVisibility(View.VISIBLE);
            }
            if (selectedIcon != null) {
                selectedIcon.setVisibility(View.GONE);
            }
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, iconBackgroundTypedValue, true);
            itemView.setActivated(false);
        }
        itemView.setBackgroundResource(iconBackgroundTypedValue.resourceId);
    }
}
