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

package ro.expectations.expenses.ui.accounts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
import ro.expectations.expenses.utils.DrawableUtils;
import ro.expectations.expenses.utils.ListUtils;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardIssuer;
import ro.expectations.expenses.model.ElectronicPaymentType;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.utils.NumberUtils;
import ro.expectations.expenses.ui.recyclerview.MultipleSelectionAdapter;

public class AccountsAdapter extends MultipleSelectionAdapter<AccountsAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    public AccountsAdapter(Context context) {
        mContext = context;
        setHasStableIds(true);
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

        // Set the row background
        ListUtils.setItemBackground(mContext, holder.itemView, isItemSelected(position),
                holder.mAccountIconBackground, holder.mSelectedIconBackground);

        // Set the icon
        String type = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.TYPE));
        AccountType accountType = AccountType.valueOf(type);
        if (accountType == AccountType.CREDIT_CARD || accountType == AccountType.DEBIT_CARD) {
            String issuer = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.SUBTYPE));
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
            holder.mAccountIcon.setImageDrawable(DrawableUtils.tint(mContext, cardIssuer.iconId, R.color.colorWhite));
        } else if (accountType == AccountType.ELECTRONIC) {
            String paymentType = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.SUBTYPE));
            ElectronicPaymentType electronicPaymentType;
            if (paymentType == null) {
                electronicPaymentType = ElectronicPaymentType.OTHER;
            } else {
                try {
                    electronicPaymentType = ElectronicPaymentType.valueOf(paymentType);
                } catch(IllegalArgumentException ex) {
                    electronicPaymentType = ElectronicPaymentType.OTHER;
                }
            }
            holder.mAccountIcon.setImageDrawable(DrawableUtils.tint(mContext, electronicPaymentType.iconId, R.color.colorWhite));
        } else {
            holder.mAccountIcon.setImageDrawable(DrawableUtils.tint(mContext, accountType.iconId, R.color.colorWhite));
        }

        // Set the icon background color
        Drawable background = DrawableUtils.tintWithColor(holder.mAccountIconBackground.getBackground().mutate(), ContextCompat.getColor(mContext, accountType.colorId));
        holder.mAccountIconBackground.setBackground(background);

        // Set the description
        holder.mAccountDescription.setText(accountType.titleId);

        // Set the title
        String title = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.TITLE));
        holder.mAccountTitle.setText(title);

        // Set the date
        long now = System.currentTimeMillis();
        long lastTransactionAt = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.LAST_TRANSACTION_AT));
        if (lastTransactionAt == 0) {
            lastTransactionAt = mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.CREATED_AT));
        }
        holder.mAccountLastTransactionAt.setText(DateUtils.getRelativeTimeSpanString(lastTransactionAt, now, DateUtils.DAY_IN_MILLIS));

        // Set the account balance
        double balance = NumberUtils.roundToTwoPlaces(mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts.BALANCE)) / 100.0);
        String currencyCode = mCursor.getString(mCursor.getColumnIndex(ExpensesContract.Accounts.CURRENCY));
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        holder.mAccountBalance.setText(format.format(balance));
        if (balance > 0) {
            holder.mAccountBalance.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen700));
        } else if (balance < 0) {
            holder.mAccountBalance.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed700));
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(ExpensesContract.Accounts._ID));
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final RelativeLayout mAccountIconBackground;
        public final ImageView mAccountIcon;
        public final RelativeLayout mSelectedIconBackground;
        public final TextView mAccountTitle;
        public final TextView mAccountDescription;
        public final TextView mAccountLastTransactionAt;
        public final TextView mAccountBalance;

        public ViewHolder(View view) {
            super(view);
            mAccountIconBackground = (RelativeLayout) view.findViewById(R.id.account_icon_background);
            mSelectedIconBackground = (RelativeLayout) view.findViewById(R.id.selected_icon_background);
            mAccountIcon = (ImageView) view.findViewById(R.id.account_icon);
            mAccountTitle = (TextView) view.findViewById(R.id.account_title);
            mAccountDescription = (TextView) view.findViewById(R.id.account_description);
            mAccountLastTransactionAt = (TextView) view.findViewById(R.id.account_last_transaction_at);
            mAccountBalance = (TextView) view.findViewById(R.id.account_balance);
        }
    }
}
