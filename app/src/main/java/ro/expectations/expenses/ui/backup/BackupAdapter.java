package ro.expectations.expenses.ui.backup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.ListHelper;
import ro.expectations.expenses.widget.recyclerview.MultipleSelectionAdapter;

public class BackupAdapter extends MultipleSelectionAdapter<BackupAdapter.ViewHolder> {

    private File[] mFiles;
    final private Context mContext;

    public BackupAdapter(Context context, File[] files) {
        mContext = context;
        mFiles = files;
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
        ListHelper.setItemBackground(mContext, holder.itemView, isItemSelected(position));

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
