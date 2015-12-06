package ro.expectations.expenses.ui.transactions;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    final private OnClickListener mClickListener;

    public TransactionsAdapter(Context context, OnClickListener clickListener, View emptyView) {
        mContext = context;
        mClickListener = clickListener;
        mEmptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_transactions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the title
        String note = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Transactions.NOTE));
        holder.mTitleView.setText(note);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTitleView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.account_title);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int transactionId = mCursor.getColumnIndex(ExpensesContract.Transactions._ID);
            mClickListener.onClick(mCursor.getLong(transactionId), this);
        }
    }

    public interface OnClickListener {
        void onClick(long transactionId, ViewHolder vh);
    }
}
