package ro.expectations.expenses.ui.payees;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;

public class PayeesAdapter extends RecyclerView.Adapter<PayeesAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    final private OnClickListener mClickListener;

    public PayeesAdapter(Context context, OnClickListener clickListener, View emptyView) {
        mContext = context;
        mClickListener = clickListener;
        mEmptyView = emptyView;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the title
        String title = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Payees.NAME));
        holder.mNameView.setText(title);
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_payees, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mNameView;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameView = (TextView) itemView.findViewById(R.id.payee_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int accountId = mCursor.getColumnIndex(ExpensesContract.Accounts._ID);
            mClickListener.onClick(mCursor.getLong(accountId), this);
        }
    }

    public interface OnClickListener {
        void onClick(long payeeId, ViewHolder vh);
    }
}
