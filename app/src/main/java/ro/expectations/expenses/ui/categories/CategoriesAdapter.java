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

package ro.expectations.expenses.ui.categories;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.utils.ColorUtils;
import ro.expectations.expenses.utils.DrawableUtils;
import ro.expectations.expenses.utils.ListUtils;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.recyclerview.MultipleSelectionAdapter;

public class CategoriesAdapter extends MultipleSelectionAdapter<CategoriesAdapter.ViewHolder> {

    static final String[] PROJECTION = {
            ExpensesContract.Categories._ID,
            ExpensesContract.Categories.NAME,
            ExpensesContract.Categories.COLOR,
            ExpensesContract.Categories.ICON,
            ExpensesContract.Categories.PARENT_ID,
            ExpensesContract.Categories.CHILDREN
    };
    static final int COLUMN_CATEGORY_ID = 0;
    static final int COLUMN_CATEGORY_NAME = 1;
    static final int COLUMN_CATEGORY_COLOR = 2;
    static final int COLUMN_CATEGORY_ICON = 3;
    static final int COLUMN_CATEGORY_PARENT_ID = 4;
    static final int COLUMN_CATEGORY_CHILDREN = 5;

    private Cursor mCursor;
    final private Context mContext;

    public CategoriesAdapter(Context context) {
        mContext = context;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_categories, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the row background
        ListUtils.setItemBackground(mContext, holder.itemView, isItemSelected(position),
                holder.mCategoryIconBackground, holder.mSelectedIconBackground);

        // Set the icon background color
        int color = ColorUtils.fromRGB(mCursor.getString(COLUMN_CATEGORY_COLOR), ContextCompat.getColor(mContext, R.color.colorPrimary));
        Drawable background = DrawableUtils.tintWithColor(holder.mCategoryIconBackground.getBackground().mutate(), color);
        holder.mCategoryIconBackground.setBackground(background);

        // Set the icon
        int iconId = DrawableUtils.getIdentifier(mContext, mCursor.getString(COLUMN_CATEGORY_ICON));
        holder.mCategoryIcon.setImageResource(iconId);

        // Set the category name
        String name = mCursor.getString(COLUMN_CATEGORY_NAME);
        holder.mNameView.setText(name);

        // Show or hide the subcategories icon
        long parentId = mCursor.getLong(COLUMN_CATEGORY_PARENT_ID);
        if (parentId > 0 ) {
            holder.mSubcategoriesIcon.setVisibility(View.GONE);
        } else {
            long children = mCursor.getLong(COLUMN_CATEGORY_CHILDREN);
            if (children > 0) {
                holder.mSubcategoriesIcon.setVisibility(View.VISIBLE);
            } else {
                holder.mSubcategoriesIcon.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(COLUMN_CATEGORY_ID);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final RelativeLayout mCategoryIconBackground;
        public final ImageView mCategoryIcon;
        public final RelativeLayout mSelectedIconBackground;
        public final ImageView mSelectedIcon;
        public final TextView mNameView;
        public final ImageView mSubcategoriesIcon;

        public ViewHolder(View view) {
            super(view);
            mCategoryIconBackground = (RelativeLayout) view.findViewById(R.id.category_icon_background);
            mCategoryIcon = (ImageView) view.findViewById(R.id.category_icon);
            mSelectedIconBackground = (RelativeLayout) view.findViewById(R.id.selected_icon_background);
            mSelectedIcon = (ImageView) view.findViewById(R.id.selected_icon);
            mNameView = (TextView) view.findViewById(R.id.category_name);
            mSubcategoriesIcon = (ImageView) view.findViewById(R.id.subcategories_icon);
        }
    }
}
