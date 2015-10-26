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
    static final String CONTENT_AUTHORITY = "ro.expectations.expenses.provider";

    // Use CONTENT_AUTHORITY to create the base of all URIs used to contact the content provider.
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Inner class that defines the table contents of the `accounts` table.
     */
    public static final class Accounts implements BaseColumns {

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
        public static final String CARD_ISSUER = "card_issuer";
        public static final String IS_ACTIVE = "is_active";
        public static final String INCLUDE_INTO_TOTALS = "include_into_totals";
        public static final String SORT_ORDER = "sort_order";
        public static final String NOTE = "note";
        public static final String CREATED_AT = "created_at";
        public static final String LAST_TRANSACTION_AT = "last_transaction_at";
        public static final String LAST_ACCOUNT_ID = "last_account_id";
        public static final String LAST_CATEGORY_ID = "last_category_id";
    }

    /**
     * Inner class that defines the table contents of the `categories` table.
     */
    static final class Categories implements BaseColumns {

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
    }

    /**
     * Inner class that defines the table contents of the `running_balances` table.
     */
    public static final class RunningBalances implements BaseColumns {

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
        public static final String ORIGINAL_FROM_CURRENCY = "original_from_currency";
        public static final String ORIGINAL_FROM_AMOUNT = "original_from_amount";
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
        public static final String IS_TRANSFER = "is_transfer";
        public static final String IS_SPLIT = "is_split";
    }
}
