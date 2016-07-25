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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.expectations.expenses.R;
import ro.expectations.expenses.utils.DrawableUtils;

public class IconPickerAdapter extends RecyclerView.Adapter<IconPickerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private final Context mContext;
    private final List<Map<String, String>> mIcons;
    private final OnItemClickListener mOnItemClickListener;

    public IconPickerAdapter(Context context, String[] icons, String[] iconTitles, OnItemClickListener onClickListener) {
        mContext = context;

        mIcons = new ArrayList<>(icons.length);
        for (int i = 0; i < icons.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("icon", icons[i]);
            item.put("title", iconTitles[i]);
            mIcons.add(i, item);
        }
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
        holder.mIcon.setImageResource(DrawableUtils.getIdentifier(mContext, mIcons.get(position).get("icon")));

        // Set the icon title
        holder.mTitle.setText(mIcons.get(position).get("title"));

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
        return mIcons.size();
    }

    public String getIcon(int position) {
        return mIcons.get(position).get("icon");
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final RelativeLayout mContainer;
        public final ImageView mIcon;
        public final TextView mTitle;

        public ViewHolder(View view) {
            super(view);

            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mIcon = (ImageView) view.findViewById(R.id.icon);
            mTitle = (TextView) view.findViewById(R.id.title);
        }
    }
}
