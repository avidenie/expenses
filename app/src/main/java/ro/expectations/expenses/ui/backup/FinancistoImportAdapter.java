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

package ro.expectations.expenses.ui.backup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.recyclerview.SingleSelectionAdapter;
import ro.expectations.expenses.utils.ListUtils;

public class FinancistoImportAdapter extends SingleSelectionAdapter<FinancistoImportAdapter.ViewHolder> {

    private File[] mFiles;
    final private Context mContext;

    public FinancistoImportAdapter(Context context, File[] files) {
        mContext = context;
        mFiles = files;
    }

    public void setFiles(File[] files) {
        mFiles = files;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_backup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File currentFile = mFiles[position];

        // Set the row background
        ListUtils.setItemBackground(mContext, holder.itemView, isItemSelected(position));

        // Set the backup file details
        holder.mBackupFilename.setText(currentFile.getName());
        holder.mBackupFilesize.setText(Formatter.formatFileSize(mContext, currentFile.length()));
    }

    @Override
    public int getItemCount() {
        return mFiles.length;
    }

    public File getItem(int position) {
        return mFiles[position];
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mBackupFilename;
        private final TextView mBackupFilesize;

        public ViewHolder(View view) {
            super(view);
            mBackupFilename = (TextView) view.findViewById(R.id.backup_filename);
            mBackupFilesize = (TextView) view.findViewById(R.id.backup_filesize);
        }
    }
}
