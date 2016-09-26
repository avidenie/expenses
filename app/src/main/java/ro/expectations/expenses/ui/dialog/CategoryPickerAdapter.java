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

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.utils.ColorUtils;
import ro.expectations.expenses.utils.DrawableUtils;

public class CategoryPickerAdapter extends RecyclerView.Adapter<CategoryPickerAdapter.ViewHolder> {

    static final String[] PROJECTION = {
            ExpensesContract.Categories._ID,
            ExpensesContract.Categories.NAME,
            ExpensesContract.Categories.COLOR
    };
    static final int COLUMN_CATEGORY_ID = 0;
    static final int COLUMN_CATEGORY_NAME = 1;
    static final int COLUMN_CATEGORY_COLOR = 2;

    private Cursor mCursor;
    final private Context mContext;

    public CategoryPickerAdapter(Context context) {
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

        // Set the icon background color
        int color = ColorUtils.fromRGB(mCursor.getString(COLUMN_CATEGORY_COLOR), ContextCompat.getColor(mContext, R.color.colorPrimary));
        Drawable background = DrawableUtils.tintWithColor(holder.mCategoryIconBackground.getBackground().mutate(), color);
        holder.mCategoryIconBackground.setBackground(background);

        // Set the icon
        int iconId;
        if (getItemId(position) == 0) {
            iconId = R.drawable.ic_clear_black_24dp;
        } else {
            iconId = R.drawable.ic_question_mark_black_24dp;
        }
        holder.mCategoryIcon.setImageDrawable(DrawableUtils.tint(mContext, iconId, R.color.colorWhite));

        // Set the category name
        String name = mCursor.getString(COLUMN_CATEGORY_NAME);
        holder.mCategoryName.setText(name);

        // Set the proper padding for a dialog.
        TypedValue value = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.dialogPreferredPadding, value, true);
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        int padding = (int) value.getDimension(metrics);
        holder.mCategoryItem.setPadding(
                padding,
                holder.mCategoryItem.getPaddingTop(),
                padding,
                holder.mCategoryItem.getPaddingBottom()
        );
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

        public final LinearLayout mCategoryItem;
        public final RelativeLayout mCategoryIconBackground;
        public final ImageView mCategoryIcon;
        public final TextView mCategoryName;

        public ViewHolder(View view) {
            super(view);

            mCategoryItem = (LinearLayout) view.findViewById(R.id.category_item);
            mCategoryIconBackground = (RelativeLayout) view.findViewById(R.id.category_icon_background);
            mCategoryIcon = (ImageView) view.findViewById(R.id.category_icon);
            mCategoryName = (TextView) view.findViewById(R.id.category_name);
        }
    }
}

