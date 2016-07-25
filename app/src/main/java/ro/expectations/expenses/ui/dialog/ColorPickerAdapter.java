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
import android.graphics.drawable.Drawable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Map;

import ro.expectations.expenses.R;
import ro.expectations.expenses.utils.DrawableUtils;
import ro.expectations.expenses.ui.recyclerview.SingleSelectionAdapter;
import ro.expectations.expenses.utils.ColorStyleUtils;

public class ColorPickerAdapter extends SingleSelectionAdapter<ColorPickerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private final Context mContext;
    @StyleRes private final int[] mStyles;
    private final OnItemClickListener mOnItemClickListener;

    public ColorPickerAdapter(Context context, @StyleRes int[] styles, OnItemClickListener onClickListener) {
        mContext = context;
        mStyles = styles;
        mOnItemClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_colors, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        @StyleRes int styleResourceId = mStyles[position];
        Map<String, Integer> colors = ColorStyleUtils.getColorsFromStyle(mContext, styleResourceId);

        // Set the icon background color
        Drawable background = DrawableUtils.tintWithColor(holder.mColorBackground.getBackground().mutate(), colors.get(ColorStyleUtils.COLOR_PRIMARY));
        holder.mColorBackground.setBackground(background);

        // Set the checked icon
        holder.mColorSelected.setVisibility(isItemSelected(position) ? View.VISIBLE : View.GONE);

        // Set the onclick listener
        if (mOnItemClickListener != null) {
            holder.mColorBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.mColorBackground, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mStyles.length;
    }

    public @StyleRes int getItem(int position) {
        return mStyles[position];
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final FrameLayout mContainer;
        public final FrameLayout mColorBackground;
        public final ImageView mColorSelected;

        public ViewHolder(View view) {
            super(view);

            mContainer = (FrameLayout) view.findViewById(R.id.container);
            mColorBackground = (FrameLayout) view.findViewById(R.id.color_background);
            mColorSelected = (ImageView) view.findViewById(R.id.color_selected);
        }
    }
}
