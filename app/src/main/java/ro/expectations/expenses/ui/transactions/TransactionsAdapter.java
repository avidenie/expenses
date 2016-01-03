package ro.expectations.expenses.ui.transactions;

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
import ro.expectations.expenses.utils.NumberUtils;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private long mSelectedAccountId;
    final private View mEmptyView;
    final private OnClickListener mClickListener;

    public TransactionsAdapter(Context context, long selectedAccountId, OnClickListener clickListener, View emptyView) {
        mContext = context;
        mSelectedAccountId = selectedAccountId;
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

        long fromAccountId = mCursor.getLong(TransactionsFragment.COLUMN_FROM_ACCOUNT_ID);
        long toAccountId = mCursor.getLong(TransactionsFragment.COLUMN_TO_ACCOUNT_ID);

        if (fromAccountId > 0 && toAccountId > 0) {
            processTransfer(holder, position);
        } else {
            if (fromAccountId > 0) {
                processDebit(holder, position);
            } else {
                processCredit(holder, position);
            }
        }

        // Set the description
        StringBuilder description = new StringBuilder();
        long categoryId = mCursor.getLong(TransactionsFragment.COLUMN_CATEGORY_ID);
        if (categoryId == -1) {
            description.append(mContext.getString(R.string.multiple_categories));
        } else {
            String category = mCursor.getString(TransactionsFragment.COLUMN_CATEGORY_NAME);
            if (category != null && !category.isEmpty()) {
                description.append(category);
            }
        }
        String parentCategory = mCursor.getString(TransactionsFragment.COLUMN_PARENT_CATEGORY_NAME);
        if (parentCategory != null && !parentCategory.isEmpty()) {
            description.insert(0, " » ");
            description.insert(0, parentCategory);
        }

        StringBuilder additionalDescription = new StringBuilder();
        String payeeName = mCursor.getString(TransactionsFragment.COLUMN_PAYEE_NAME);
        if (payeeName != null && !payeeName.isEmpty()) {
            additionalDescription.append(payeeName);
        }
        String note = mCursor.getString(TransactionsFragment.COLUMN_NOTE);
        if (note != null && !note.isEmpty()) {
            if (additionalDescription.length() > 0) {
                additionalDescription.append(": ");
            }
            additionalDescription.append(note);
        }
        if (description.length() > 0 && additionalDescription.length() > 0) {
            additionalDescription.insert(0, " (");
            additionalDescription.append(")");
        }
        if (additionalDescription.length() > 0) {
            description.append(additionalDescription.toString());
        }
        if (description.length() == 0) {
            if (fromAccountId > 0 && toAccountId > 0) {
                description.append(mContext.getString(R.string.default_transfer_description));
            } else {
                long fromAmount = mCursor.getLong(TransactionsFragment.COLUMN_FROM_AMOUNT);
                if (fromAmount > 0) {
                    description.append(mContext.getString(R.string.default_debit_description));
                } else {
                    description.append(mContext.getString(R.string.default_credit_description));
                }
            }
        }
        holder.mDescription.setText(description.toString());

        // Set the transaction date
        long transactionDate = mCursor.getLong(TransactionsFragment.COLUMN_OCCURED_AT);
        if (transactionDate == 0) {
            transactionDate = mCursor.getLong(TransactionsFragment.COLUMN_CREATED_AT);
        }
        holder.mDate.setText(DateUtils.getRelativeTimeSpanString(transactionDate, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS));

        // Set the icon
        if (fromAccountId > 0 && toAccountId > 0) {
            holder.mIcon.setImageResource(R.drawable.ic_transfer_white_24dp);
        } else {
            holder.mIcon.setImageResource(R.drawable.ic_question_mark_white_24dp);
        }
        GradientDrawable bgShape = (GradientDrawable) holder.mIconBackgroundView.getBackground();
        bgShape.setColor(0xFF000000 | ContextCompat.getColor(mContext, R.color.primary));
    }

    private void processTransfer(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the account
        String fromAccount = mCursor.getString(TransactionsFragment.COLUMN_FROM_ACCOUNT_TITLE);
        String toAccount = mCursor.getString(TransactionsFragment.COLUMN_TO_ACCOUNT_TITLE);
        holder.mAccount.setText(mContext.getResources().getString(R.string.breadcrumbs, fromAccount, toAccount));

        // Set the amount
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String fromCurrencyCode = mCursor.getString(TransactionsFragment.COLUMN_FROM_CURRENCY);
        String toCurrencyCode = mCursor.getString(TransactionsFragment.COLUMN_TO_CURRENCY);
        if (fromCurrencyCode.equals(toCurrencyCode)) {
            double amount = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_FROM_AMOUNT) / 100.0);
            Currency currency = Currency.getInstance(fromCurrencyCode);
            format.setCurrency(currency);
            format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
            holder.mAmount.setText(format.format(amount));

            double fromBalance = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_FROM_BALANCE) / 100.0);
            String fromBalanceFormatted = format.format(fromBalance);
            double toBalance = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_TO_BALANCE) / 100.0);
            holder.mRunningBalance.setText(mContext.getResources().getString(R.string.breadcrumbs, fromBalanceFormatted, format.format(toBalance)));
        } else {
            Currency fromCurrency = Currency.getInstance(fromCurrencyCode);
            format.setCurrency(fromCurrency);
            format.setMaximumFractionDigits(fromCurrency.getDefaultFractionDigits());
            double fromAmount = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_FROM_AMOUNT) / 100.0);
            String fromAmountFormatted = format.format(fromAmount);
            double fromBalance = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_FROM_BALANCE) / 100.0);
            String fromBalanceFormatted = format.format(fromBalance);

            Currency toCurrency = Currency.getInstance(toCurrencyCode);
            format.setCurrency(toCurrency);
            format.setMaximumFractionDigits(toCurrency.getDefaultFractionDigits());
            double toAmount = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_TO_AMOUNT) / 100.0);
            double toBalance = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_TO_BALANCE) / 100.0);

            holder.mAmount.setText(mContext.getResources().getString(R.string.breadcrumbs, fromAmountFormatted, format.format(toAmount)));
            holder.mRunningBalance.setText(mContext.getResources().getString(R.string.breadcrumbs, fromBalanceFormatted, format.format(toBalance)));
        }

        // Set the color for the amount and the transaction type icon
        if (mSelectedAccountId == 0) {
            holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.orange_700));
            holder.mTypeIcon.setImageResource(R.drawable.ic_swap_horiz_orange_24dp);
        } else {
            long fromAccountId = mCursor.getLong(TransactionsFragment.COLUMN_FROM_ACCOUNT_ID);
            long toAccountId = mCursor.getLong(TransactionsFragment.COLUMN_TO_ACCOUNT_ID);
            if (mSelectedAccountId == fromAccountId) {
                holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.red_700));
                holder.mTypeIcon.setImageResource(R.drawable.ic_call_made_red_24dp);
            } else if (mSelectedAccountId == toAccountId) {
                holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.green_700));
                holder.mTypeIcon.setImageResource(R.drawable.ic_call_received_green_24dp);
            }
        }

        // Set the transaction type icon

    }

    private void processDebit(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the account
        String fromAccount = mCursor.getString(TransactionsFragment.COLUMN_FROM_ACCOUNT_TITLE);
        holder.mAccount.setText(fromAccount);

        // Set the amount
        double fromAmount = NumberUtils.roundToTwoPlaces(0 - mCursor.getLong(TransactionsFragment.COLUMN_FROM_AMOUNT) / 100.0);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String fromCurrencyCode = mCursor.getString(TransactionsFragment.COLUMN_FROM_CURRENCY);
        Currency fromCurrency = Currency.getInstance(fromCurrencyCode);
        format.setCurrency(fromCurrency);
        format.setMaximumFractionDigits(fromCurrency.getDefaultFractionDigits());
        holder.mAmount.setText(format.format(fromAmount));
        holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.red_700));

        double fromBalance = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_FROM_BALANCE) / 100.0);
        holder.mRunningBalance.setText(format.format(fromBalance));

        // Set the transaction type icon
        holder.mTypeIcon.setImageResource(R.drawable.ic_call_made_red_24dp);
    }

    private void processCredit(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set the account
        String toAccount = mCursor.getString(TransactionsFragment.COLUMN_TO_ACCOUNT_TITLE);
        holder.mAccount.setText(toAccount);

        // Set the amount
        double toAmount = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_TO_AMOUNT) / 100.0);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String toCurrencyCode = mCursor.getString(TransactionsFragment.COLUMN_TO_CURRENCY);
        Currency toCurrency = Currency.getInstance(toCurrencyCode);
        format.setCurrency(toCurrency);
        format.setMaximumFractionDigits(toCurrency.getDefaultFractionDigits());
        holder.mAmount.setText(format.format(toAmount));
        holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.green_700));

        double toBalance = NumberUtils.roundToTwoPlaces(mCursor.getLong(TransactionsFragment.COLUMN_TO_BALANCE) / 100.0);
        holder.mRunningBalance.setText(format.format(toBalance));

        // Set the transaction type icon
        holder.mTypeIcon.setImageResource(R.drawable.ic_call_received_green_24dp);
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
        mEmptyView.setVisibility(mCursor != null && getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final RelativeLayout mIconBackgroundView;
        public final ImageView mIcon;
        public final TextView mAccount;
        public final TextView mDescription;
        public final TextView mDate;
        public final ImageView mTypeIcon;
        public final TextView mAmount;
        public final TextView mRunningBalance;

        public ViewHolder(View itemView) {
            super(itemView);
            mIconBackgroundView = (RelativeLayout) itemView.findViewById(R.id.transaction_icon_background);
            mIcon = (ImageView) itemView.findViewById(R.id.transaction_icon);
            mAccount = (TextView) itemView.findViewById(R.id.transaction_account);
            mDescription = (TextView) itemView.findViewById(R.id.transaction_description);
            mDate = (TextView) itemView.findViewById(R.id.transaction_date);
            mTypeIcon = (ImageView) itemView.findViewById(R.id.transaction_type_icon);
            mAmount = (TextView) itemView.findViewById(R.id.transaction_amount);
            mRunningBalance = (TextView) itemView.findViewById(R.id.account_running_balance);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickListener.onClick(mCursor.getLong(TransactionsFragment.COLUMN_TRANSACTION_ID), this);
        }
    }

    public interface OnClickListener {
        void onClick(long transactionId, ViewHolder vh);
    }
}
