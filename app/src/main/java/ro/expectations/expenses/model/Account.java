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

package ro.expectations.expenses.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import ro.expectations.expenses.provider.ExpensesContract;

public class Account implements Parcelable {

    public static final String[] PROJECTION = {
            ExpensesContract.Accounts._ID,
            ExpensesContract.Accounts.TITLE,
            ExpensesContract.Accounts.CURRENCY,
            ExpensesContract.Accounts.TYPE,
            ExpensesContract.Accounts.SUBTYPE,
            ExpensesContract.Accounts.IS_ACTIVE,
            ExpensesContract.Accounts.INCLUDE_INTO_TOTALS,
            ExpensesContract.Accounts.SORT_ORDER,
            ExpensesContract.Accounts.NOTE,
            ExpensesContract.Accounts.CREATED_AT
    };
    public static final int COLUMN_ACCOUNT_ID = 0;
    public static final int COLUMN_ACCOUNT_TITLE = 1;
    public static final int COLUMN_ACCOUNT_CURRENCY = 2;
    public static final int COLUMN_ACCOUNT_TYPE = 3;
    public static final int COLUMN_ACCOUNT_SUBTYPE = 4;
    public static final int COLUMN_ACCOUNT_IS_ACTIVE = 5;
    public static final int COLUMN_ACCOUNT_INCLUDE_INTO_TOTALS = 6;
    public static final int COLUMN_ACCOUNT_SORT_ORDER = 7;
    public static final int COLUMN_ACCOUNT_NOTE = 8;
    public static final int COLUMN_ACCOUNT_CREATED_AT = 9;

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    private long id;
    private String title;
    private String currency;
    private AccountType type;
    private CardType cardType;
    private OnlinePaymentType onlinePaymentType;
    private boolean isActive;
    private boolean includeIntoTotals;
    private int sortOrder;
    private String note;
    private long createdAt;

    public Account(long id, String title, String currency, AccountType type, CardType cardType,
                   OnlinePaymentType onlinePaymentType, boolean isActive,
                   boolean includeIntoTotals, int sortOrder, String note, long createdAt) {

        this.id = id;
        this.title = title;
        this.currency = currency;
        this.type = type;
        this.cardType = cardType;
        this.onlinePaymentType = onlinePaymentType;
        this.isActive = isActive;
        this.includeIntoTotals = includeIntoTotals;
        this.sortOrder = sortOrder;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Account(Cursor cursor) {

        id = cursor.getLong(Account.COLUMN_ACCOUNT_ID);
        title = cursor.getString(Account.COLUMN_ACCOUNT_TITLE);
        currency = cursor.getString(Account.COLUMN_ACCOUNT_CURRENCY);

        try {
            type = AccountType.valueOf(cursor.getString(Account.COLUMN_ACCOUNT_TYPE));
        } catch (final IllegalArgumentException ex) {
            type = AccountType.OTHER;
        }

        if (type == AccountType.CREDIT_CARD || type == AccountType.DEBIT_CARD) {
            try {
                cardType = CardType.valueOf(cursor.getString(Account.COLUMN_ACCOUNT_SUBTYPE));
            } catch (final IllegalArgumentException ex) {
                cardType = CardType.OTHER;
            }
        } else if (type == AccountType.ONLINE) {
            try {
                onlinePaymentType = OnlinePaymentType.valueOf(cursor.getString(Account.COLUMN_ACCOUNT_SUBTYPE));
            } catch (final IllegalArgumentException ex) {
                onlinePaymentType = OnlinePaymentType.OTHER;
            }
        }

        isActive = cursor.getInt(Account.COLUMN_ACCOUNT_IS_ACTIVE) != 0;
        includeIntoTotals = cursor.getInt(Account.COLUMN_ACCOUNT_INCLUDE_INTO_TOTALS) != 0;
        sortOrder = cursor.getInt(Account.COLUMN_ACCOUNT_SORT_ORDER);
        note = cursor.getString(Account.COLUMN_ACCOUNT_NOTE);
        createdAt = cursor.getLong(Account.COLUMN_ACCOUNT_CREATED_AT);
    }

    public Account(Parcel in) {
        id = in.readLong();
        title = in.readString();
        currency = in.readString();
        type = (AccountType) in.readSerializable();
        cardType = (CardType) in.readSerializable();
        onlinePaymentType = (OnlinePaymentType) in.readSerializable();
        isActive = in.readInt() != 0;
        includeIntoTotals = in.readInt() != 0;
        sortOrder = in.readInt();
        note = in.readString();
        createdAt = in.readLong();
    }

    public Account(Account other) {
        id = other.getId();
        title = other.getTitle();
        currency = other.getCurrency();
        type = other.getType();
        cardType = other.getCardType();
        onlinePaymentType = other.getOnlinePaymentType();
        isActive = other.isActive();
        includeIntoTotals = other.isIncludeIntoTotals();
        sortOrder = other.getSortOrder();
        note = other.getNote();
        createdAt = other.getCreatedAt();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Account)) {
            return false;
        }

        Account that = (Account) other;

        return id == that.id
                && title.equals(that.title)
                && currency.equals(that.currency)
                && type.equals(that.type)
                && (cardType == null ? that.cardType == null : cardType.equals(that.cardType))
                && (onlinePaymentType == null ? that.onlinePaymentType == null : onlinePaymentType.equals(that.onlinePaymentType))
                && isActive == that.isActive
                && includeIntoTotals == that.includeIntoTotals
                && sortOrder == that.sortOrder
                && (note == null ? that.note == null : note.equals(that.note))
                && createdAt == that.createdAt;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + Long.valueOf(id).hashCode();
        hashCode = hashCode * 37 + title.hashCode();
        hashCode = hashCode * 37 + currency.hashCode();
        hashCode = hashCode * 37 + type.hashCode();
        hashCode = hashCode * 37 + cardType.hashCode();
        hashCode = hashCode * 37 + onlinePaymentType.hashCode();
        hashCode = hashCode * 37 + Boolean.valueOf(isActive).hashCode();
        hashCode = hashCode * 37 + Boolean.valueOf(includeIntoTotals).hashCode();
        hashCode = hashCode * 37 + Long.valueOf(sortOrder).hashCode();
        hashCode = hashCode * 37 + note.hashCode();
        hashCode = hashCode * 37 + Long.valueOf(createdAt).hashCode();

        return hashCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(currency);
        dest.writeSerializable(type);
        dest.writeSerializable(cardType);
        dest.writeSerializable(onlinePaymentType);
        dest.writeInt(isActive ? 1 : 0);
        dest.writeInt(includeIntoTotals ? 1 : 0);
        dest.writeInt(sortOrder);
        dest.writeString(note);
        dest.writeLong(createdAt);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public OnlinePaymentType getOnlinePaymentType() {
        return onlinePaymentType;
    }

    public void setOnlinePaymentType(OnlinePaymentType onlinePaymentType) {
        this.onlinePaymentType = onlinePaymentType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isIncludeIntoTotals() {
        return includeIntoTotals;
    }

    public void setIncludeIntoTotals(boolean includeIntoTotals) {
        this.includeIntoTotals = includeIntoTotals;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public ContentValues toContentValues() {

        ContentValues contentValues = new ContentValues();

        contentValues.put(ExpensesContract.Accounts.TITLE, title);
        contentValues.put(ExpensesContract.Accounts.CURRENCY, currency);
        contentValues.put(ExpensesContract.Accounts.TYPE, type.toString());
        if (type == AccountType.CREDIT_CARD || type == AccountType.DEBIT_CARD) {
            if (cardType != null) {
                contentValues.put(ExpensesContract.Accounts.SUBTYPE, cardType.toString());
            } else {
                contentValues.put(ExpensesContract.Accounts.SUBTYPE, CardType.OTHER.toString());
            }
        } else if (type == AccountType.ONLINE) {
            if (onlinePaymentType != null) {
                contentValues.put(ExpensesContract.Accounts.SUBTYPE, onlinePaymentType.toString());
            } else {
                contentValues.put(ExpensesContract.Accounts.SUBTYPE, OnlinePaymentType.OTHER.toString());
            }
        }
        contentValues.put(ExpensesContract.Accounts.IS_ACTIVE, isActive);
        contentValues.put(ExpensesContract.Accounts.INCLUDE_INTO_TOTALS, includeIntoTotals);
        contentValues.put(ExpensesContract.Accounts.SORT_ORDER, sortOrder);
        contentValues.put(ExpensesContract.Accounts.NOTE, note);
        contentValues.put(ExpensesContract.Accounts.CREATED_AT, createdAt);

        return contentValues;
    }
}
