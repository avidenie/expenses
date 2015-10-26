package ro.expectations.expenses.ui.accounts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Currency;

import ro.expectations.expenses.R;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardIssuer;
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
        String type = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.TYPE));
        AccountType accountType = AccountType.valueOf(type);
        if (accountType == AccountType.CREDIT_CARD || accountType == AccountType.DEBIT_CARD) {
            String issuer = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.CARD_ISSUER));
            CardIssuer cardIssuer;
            if (issuer == null) {
                cardIssuer = CardIssuer.OTHER;
            } else {
                try {
                    cardIssuer = CardIssuer.valueOf(issuer);
                } catch (final IllegalArgumentException ex) {
                    cardIssuer = CardIssuer.OTHER;
                }
            }
            holder.mIconView.setImageResource(cardIssuer.iconId);
        } else {
            holder.mIconView.setImageResource(accountType.iconId);
        }

        // Set the icon background color.
        GradientDrawable bgShape = (GradientDrawable) holder.mIconBackgroundView.getBackground();
        bgShape.setColor(0xFF000000 | ContextCompat.getColor(mContext, accountType.colorId));

        // Set the description.
        holder.mDescriptionView.setText(accountType.titleId);

        // Set the title
        String title = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.TITLE));
        holder.mTitleView.setText(title);

        // Set the date.
        long now = System.currentTimeMillis();
        long lastTransactionAt = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.LAST_TRANSACTION_AT)) * 1000L;
        if (lastTransactionAt == 0) {
            lastTransactionAt = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.CREATED_AT)) * 1000L;
        }
        holder.mLastTransactionAt.setText(DateUtils.getRelativeTimeSpanString(lastTransactionAt, now, DateUtils.DAY_IN_MILLIS));

        // Set the balance
        long balance = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.BALANCE)) / 100;
        String currencyCode = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.CURRENCY));
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        holder.mBalanceView.setText(format.format(balance));
        if (balance > 0) {
            holder.mBalanceView.setTextColor(ContextCompat.getColor(mContext, R.color.green_700));
        } else if (balance < 0) {
            holder.mBalanceView.setTextColor(ContextCompat.getColor(mContext, R.color.red_700));
        }
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
        public final RelativeLayout mIconBackgroundView;
        public final ImageView mIconView;
        public final TextView mTitleView;
        public final TextView mDescriptionView;
        public final TextView mLastTransactionAt;
        public final TextView mBalanceView;

        public ViewHolder(View view) {
            super(view);
            mIconBackgroundView = (RelativeLayout) view.findViewById(R.id.account_icon_background);
            mIconView = (ImageView) view.findViewById(R.id.account_icon);
            mTitleView = (TextView) view.findViewById(R.id.account_title);
            mDescriptionView = (TextView) view.findViewById(R.id.account_description);
            mLastTransactionAt = (TextView) view.findViewById(R.id.account_last_transaction_at);
            mBalanceView = (TextView) view.findViewById(R.id.account_balance);
        }
    }
}
