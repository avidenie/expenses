package ro.expectations.expenses.ui.accounts;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Currency;

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

        // Set the icon.
        String type = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.AccountTypes.TYPE));
        ExpensesContract.AccountTypeData accountType = ExpensesContract.AccountTypeData.valueOf(type);
        if (accountType == ExpensesContract.AccountTypeData.CASH) {
            holder.mIconView.setImageResource(R.drawable.ic_cash_round);
        } else if (accountType == ExpensesContract.AccountTypeData.DEBIT_CARD) {
            holder.mIconView.setImageResource(R.drawable.ic_credit_card_round);
        } else if (accountType == ExpensesContract.AccountTypeData.CREDIT_CARD) {
            holder.mIconView.setImageResource(R.drawable.ic_credit_card_round);
        } else {
            holder.mIconView.setImageResource(R.drawable.ic_cash_round);
        }

        // Set the title
        String title = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.TITLE));
        holder.mTitleView.setText(title);

        // Set the date.
        long now = System.currentTimeMillis();
        long createdAt = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.CREATED_AT)) * 1000L;
        holder.mCreatedAt.setText(DateUtils.getRelativeTimeSpanString(createdAt, now, DateUtils.DAY_IN_MILLIS));

        // Set the balance
        long balance = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.BALANCE)) / 100;
        String currencyCode = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.CURRENCY));
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        holder.mBalanceView.setText(format.format(balance));
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
        public final TextView mCreatedAt;
        public final TextView mBalanceView;

        public ViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.account_icon);
            mTitleView = (TextView) view.findViewById(R.id.account_title);
            mCreatedAt = (TextView) view.findViewById(R.id.account_created_at);
            mBalanceView = (TextView) view.findViewById(R.id.account_balance);
        }
    }
}
