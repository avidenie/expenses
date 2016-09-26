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
import android.support.annotation.ColorInt;

import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.utils.ColorUtils;

public class Category implements Parcelable {

    public static final String[] PROJECTION = {
            ExpensesContract.Categories._ID,
            ExpensesContract.Categories.NAME,
            ExpensesContract.Categories.COLOR,
            ExpensesContract.Categories.STYLE,
            ExpensesContract.Categories.ICON,
            ExpensesContract.Categories.PARENT_ID,
            ExpensesContract.ParentCategories.PARENT_NAME,
            ExpensesContract.Categories.CHILDREN
    };
    public static final int COLUMN_CATEGORY_ID = 0;
    public static final int COLUMN_CATEGORY_NAME = 1;
    public static final int COLUMN_CATEGORY_COLOR = 2;
    public static final int COLUMN_CATEGORY_STYLE = 3;
    public static final int COLUMN_CATEGORY_ICON = 4;
    public static final int COLUMN_CATEGORY_PARENT_ID = 5;
    public static final int COLUMN_CATEGORY_PARENT_NAME = 6;
    public static final int COLUMN_CATEGORY_CHILDREN = 7;

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
    @ColorInt private int color;
    private String style;
    private String icon;
    private long parentId;
    private int children;

    public Category(long id, String name, @ColorInt int color, String style, String icon, long parentId, int children) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.style = style;
        this.icon = icon;
        this.parentId = parentId;
        this.children = children;
    }

    public Category(Parcel in) {
        id = in.readLong();
        name = in.readString();
        color = in.readInt();
        style = in.readString();
        icon = in.readString();
        parentId = in.readLong();
        children = in.readInt();
    }

    public Category(Category other) {
        id = other.getId();
        name = other.getName();
        color = other.getColor();
        style = other.getStyle();
        icon = other.getIcon();
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
                && style.equals(that.style)
                && icon.equals(that.icon)
                && parentId == that.parentId
                && children == that.children;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + Long.valueOf(id).hashCode();
        hashCode = hashCode * 37 + name.hashCode();
        hashCode = hashCode * 37 + Integer.valueOf(color).hashCode();
        hashCode = hashCode * 37 + style.hashCode();
        hashCode = hashCode * 37 + icon.hashCode();
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
        dest.writeString(style);
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

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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
        contentValues.put(ExpensesContract.Categories.COLOR, ColorUtils.toRGB(color));
        contentValues.put(ExpensesContract.Categories.STYLE, style);
        contentValues.put(ExpensesContract.Categories.ICON, icon);
        if (parentId > 0) {
            contentValues.put(ExpensesContract.Categories.PARENT_ID, parentId);
        } else {
            contentValues.putNull(ExpensesContract.Categories.PARENT_ID);
        }
        return contentValues;
    }
}
