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

import ro.expectations.expenses.provider.ExpensesContract;

public class Category {

    public static final String[] PROJECTION = {
            ExpensesContract.Categories.NAME,
            ExpensesContract.Categories.PARENT_ID,
            ExpensesContract.ParentCategories.PARENT_NAME,
            ExpensesContract.Categories.CHILDREN
    };
    public static final int COLUMN_CATEGORY_NAME = 0;
    public static final int COLUMN_CATEGORY_PARENT_ID = 1;
    public static final int COLUMN_CATEGORY_PARENT_NAME = 2;
    public static final int COLUMN_CATEGORY_CHILDREN = 3;

}
