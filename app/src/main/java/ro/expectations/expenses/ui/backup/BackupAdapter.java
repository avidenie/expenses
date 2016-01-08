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

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.ViewHolder> {

    private File[] mFiles;
    final private Context mContext;
    final private OnClickListener mClickListener;

    public BackupAdapter(Context context, OnClickListener clickListener) {
        mContext = context;
        mClickListener = clickListener;
        mFiles = new File[0];
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
        holder.mBackupFilename.setText(currentFile.getName());
        holder.mBackupFilesize.setText(Formatter.formatFileSize(mContext, currentFile.length()));
    }

    @Override
    public int getItemCount() {
        return mFiles.length;
    }

    public void setFiles(File[] newFiles) {
        mFiles = newFiles;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mBackupFilename;
        private final TextView mBackupFilesize;

        public ViewHolder(View view) {
            super(view);
            mBackupFilename = (TextView) view.findViewById(R.id.backup_filename);
            mBackupFilesize = (TextView) view.findViewById(R.id.backup_filesize);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                File file = mFiles[getAdapterPosition()];
                mClickListener.onClick(file, this);
            }
        }
    }

    public interface OnClickListener {
        void onClick(File file, ViewHolder vh);
    }
}
