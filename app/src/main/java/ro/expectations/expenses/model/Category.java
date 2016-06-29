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
 * along with Expenses.  If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import ro.expectations.expenses.helper.ColorHelper;
import ro.expectations.expenses.provider.ExpensesContract;

public class Category implements Parcelable {

    public static final String[] PROJECTION = {
            ExpensesContract.Categories.NAME,
            ExpensesContract.Categories.COLOR,
            ExpensesContract.Categories.PARENT_ID,
            ExpensesContract.ParentCategories.PARENT_NAME,
            ExpensesContract.Categories.CHILDREN
    };
    public static final int COLUMN_CATEGORY_NAME = 0;
    public static final int COLUMN_CATEGORY_COLOR = 1;
    public static final int COLUMN_CATEGORY_PARENT_ID = 2;
    public static final int COLUMN_CATEGORY_PARENT_NAME = 3;
    public static final int COLUMN_CATEGORY_CHILDREN = 4;

    public static final Parcelable.Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    private long id;
    private String name;
    private int color;
    private long parentId;
    private int children;

    public Category(long id, String name, int color, long parentId, int children) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.parentId = parentId;
        this.children = children;
    }

    public Category(Parcel in) {
        id = in.readLong();
        name = in.readString();
        color = in.readInt();
        parentId = in.readLong();
        children = in.readInt();
    }

    public Category(Category other) {
        id = other.getId();
        name = other.getName();
        color = other.getColor();
        parentId = other.getParentId();
        children = other.getChildren();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Category)) {
            return false;
        }

        Category that = (Category) other;

        return id == that.id
                && name.equals(that.name)
                && color == that.color
                && parentId == that.parentId
                && children == that.children;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + Long.valueOf(id).hashCode();
        hashCode = hashCode * 37 + name.hashCode();
        hashCode = hashCode * 37 + Integer.valueOf(color).hashCode();
        hashCode = hashCode * 37 + Long.valueOf(parentId).hashCode();
        hashCode = hashCode * 37 + children;

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
        dest.writeInt(color);
        dest.writeLong(parentId);
        dest.writeInt(children);
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpensesContract.Categories.NAME, name);
        contentValues.put(ExpensesContract.Categories.COLOR, ColorHelper.toRGB(color));
        if (parentId > 0) {
            contentValues.put(ExpensesContract.Categories.PARENT_ID, parentId);
        } else {
            contentValues.putNull(ExpensesContract.Categories.PARENT_ID);
        }
        return contentValues;
    }
}
