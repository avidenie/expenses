package ro.expectations.expenses.ui.accounts;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;

    public AccountsAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_accounts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.mTitleView.setText(mCursor.getString(mCursor.getColumnIndex(
                ExpensesContract.Accounts.TITLE
        )));
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

    public Cursor getCursor() {
        return mCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mIconView;
        public final TextView mTitleView;
        public final TextView mLastTransactionView;
        public final TextView mBalanceView;

        public ViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.account_icon);
            mTitleView = (TextView) view.findViewById(R.id.account_title);
            mLastTransactionView = (TextView) view.findViewById(R.id.account_last_transaction_at);
            mBalanceView = (TextView) view.findViewById(R.id.account_balance);
        }
    }
}
