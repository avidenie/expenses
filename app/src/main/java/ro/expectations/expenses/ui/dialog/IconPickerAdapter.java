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

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ro.expectations.expenses.R;

public class IconPickerAdapter extends RecyclerView.Adapter<IconPickerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @DrawableRes
    private final int[] mIcons;
    private final String[] mIconTitles;
    private final OnItemClickListener mOnItemClickListener;

    public IconPickerAdapter(@DrawableRes int[] icons, String[] iconTitles, OnItemClickListener onClickListener) {
        mIcons = icons;
        mIconTitles = iconTitles;
        mOnItemClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_icons, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Set the icon
        holder.mIcon.setImageResource(mIcons[position]);

        // Set the icon title
        holder.mTitle.setText(mIconTitles[position]);

        // Set the onclick listener
        if (mOnItemClickListener != null) {
            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.mContainer, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mIcons.length;
    }

    @DrawableRes
    public int getIcon(int position) {
        return mIcons[position];
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout mContainer;
        final ImageView mIcon;
        final TextView mTitle;

        public ViewHolder(View view) {
            super(view);

            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mIcon = (ImageView) view.findViewById(R.id.icon);
            mTitle = (TextView) view.findViewById(R.id.title);
        }
    }
}
