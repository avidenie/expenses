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
import android.os.Parcel;
import android.os.Parcelable;

import ro.expectations.expenses.provider.ExpensesContract;

public class Payee implements Parcelable {

    public static final String[] PROJECTION = {
            ExpensesContract.Payees._ID,
            ExpensesContract.Payees.NAME
    };
    public static final int COLUMN_PAYEE_ID = 0;
    public static final int COLUMN_PAYEE_NAME = 1;

    public static final Parcelable.Creator<Payee> CREATOR = new Creator<Payee>() {
        @Override
        public Payee createFromParcel(Parcel source) {
            return new Payee(source);
        }

        @Override
        public Payee[] newArray(int size) {
            return new Payee[size];
        }
    };

    private long id;
    private String name;

    public Payee(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Payee(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    public Payee(Payee other) {
        id = other.getId();
        name = other.getName();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Payee)) {
            return false;
        }

        Payee that = (Payee) other;

        return id == that.id
                && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + Long.valueOf(id).hashCode();
        hashCode = hashCode * 37 + name.hashCode();

        return hashCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpensesContract.Payees.NAME, name);
        return contentValues;
    }
}
