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

package ro.expectations.expenses.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the Expenses database.
 */
public final class ExpensesContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.
    public static final String CONTENT_AUTHORITY = "ro.expectations.expenses.provider";

    // Use CONTENT_AUTHORITY to create the base of all URIs used to contact the content provider.
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Inner class that defines the table contents of the `accounts` table.
     */
    public static class Accounts implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, "accounts");

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".accounts";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".account";

        // Table name.
        public static final String TABLE_NAME = "accounts";

        // Database table columns.
        public static final String TITLE = "title";
        public static final String CURRENCY = "currency";
        public static final String BALANCE = "balance";
        public static final String TYPE = "type";
        public static final String SUBTYPE = "subtype";
        public static final String IS_ACTIVE = "is_active";
        public static final String INCLUDE_INTO_TOTALS = "include_into_totals";
        public static final String SORT_ORDER = "sort_order";
        public static final String NOTE = "note";
        public static final String CREATED_AT = "created_at";
        public static final String LAST_TRANSACTION_AT = "last_transaction_at";
        public static final String LAST_ACCOUNT_ID = "last_account_id";
        public static final String LAST_CATEGORY_ID = "last_category_id";
    }

    public static final class FromAccounts extends Accounts {
        public static final String TABLE_NAME = "from_accounts";

        // Database table columns aliases.
        public static final String FROM_TITLE = "from_title";
        public static final String FROM_CURRENCY = "from_currency";
    }

    public static final class ToAccounts extends Accounts {
        public static final String TABLE_NAME = "to_accounts";

        // Database table columns aliases.
        public static final String TO_TITLE = "to_title";
        public static final String TO_CURRENCY = "to_currency";
    }

    /**
     * Inner class that defines the table contents of the `categories` table.
     */
    public static class Categories implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, "categories");

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".categories";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".category";

        // Table name.
        public static final String TABLE_NAME = "categories";

        // Database table columns.
        public static final String NAME = "name";
        public static final String PARENT_ID = "parent_id";

        // Database table columns aliases.
        public static final String CATEGORY_ID = "category_id";
        public static final String CATEGORY_NAME = "category_name";
        public static final String CHILDREN = "children";
    }

    public static final class Subcategories extends Categories {
        public static final String TABLE_NAME = "subcategories";
    }

    public static final class ParentCategories extends Categories {
        public static final String TABLE_NAME = "parent_categories";

        // Database table columns aliases.
        public static final String PARENT_NAME = "parent_name";
    }

    /**
     * Inner class that defines the table contents of the `categories` table.
     */
    public static final class Payees implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, "payees");

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".payees";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".payee";

        // Table name.
        public static final String TABLE_NAME = "payees";

        // Database table columns.
        public static final String NAME = "name";

        // Database table columns aliases.
        public static final String PAYEE_NAME = "payee_name";
    }

    /**
     * Inner class that defines the table contents of the `running_balances` table.
     */
    public static class RunningBalances implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, "running_balances");

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".running_balances";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".running_balance";

        // Table name.
        public static final String TABLE_NAME = "running_balances";

        // Database table columns.
        public static final String ACCOUNT_ID = "account_id";
        public static final String TRANSACTION_ID = "transaction_id";
        public static final String BALANCE = "balance";
    }

    public static final class FromRunningBalances extends RunningBalances {
        public static final String TABLE_NAME = "from_running_balances";

        // Database table columns aliases.
        public static final String FROM_BALANCE = "from_balance";
    }

    public static final class ToRunningBalances extends RunningBalances {
        public static final String TABLE_NAME = "to_running_balances";

        // Database table columns aliases.
        public static final String TO_BALANCE = "to_balance";
    }

    /**
     * Inner class that defines the table contents of the `transactions` table.
     */
    public static final class Transactions implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, "transactions");

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".transactions";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".transaction";

        // Table name.
        public static final String TABLE_NAME = "transactions";

        // Database table columns.
        public static final String FROM_ACCOUNT_ID = "from_account_id";
        public static final String TO_ACCOUNT_ID = "to_account_id";
        public static final String PAYEE_ID = "payee_id";
        public static final String OCCURRED_AT = "occurred_at";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String NOTE = "note";
        public static final String ORIGINAL_CURRENCY = "original_currency";
        public static final String ORIGINAL_AMOUNT = "original_amount";
    }

    /**
     * Inner class that defines the table contents of the `transaction_details` table.
     */
    public static final class TransactionDetails implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, "transaction_details");

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".transaction_details";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".transaction_detail";

        // Table name.
        public static final String TABLE_NAME = "transaction_details";

        // Database table columns.
        public static final String TRANSACTION_ID = "transaction_id";
        public static final String FROM_AMOUNT = "from_amount";
        public static final String TO_AMOUNT = "to_amount";
        public static final String CATEGORY_ID = "category_id";
        public static final String IS_SPLIT = "is_split";

        // Database table columns aliases.
        public static final String FROM_AMOUNT_SUM = "from_amount_sum";
        public static final String TO_AMOUNT_SUM = "to_amount_sum";
    }
}
