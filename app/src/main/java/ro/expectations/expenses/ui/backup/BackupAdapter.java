package ro.expectations.expenses.ui.backup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.ViewHolder> {

    final private Context mContext;
    final private View mEmptyView;
    final private OnClickListener mClickListener;

    public BackupAdapter(Context context, OnClickListener clickListener, View emptyView) {
        mContext = context;
        mClickListener = clickListener;
        mEmptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }

    public interface OnClickListener {
        void onClick(String filename, ViewHolder vh);
    }
}
