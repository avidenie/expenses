package ro.expectations.expenses.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ro.expectations.expenses.provider.ExpensesContract.Accounts;
import ro.expectations.expenses.provider.ExpensesContract.Categories;
import ro.expectations.expenses.provider.ExpensesContract.Payees;
import ro.expectations.expenses.provider.ExpensesContract.RunningBalances;
import ro.expectations.expenses.provider.ExpensesContract.TransactionDetails;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;

public class ExpensesDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "expenses.db";

    public ExpensesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create table "categories".
        db.execSQL("CREATE TABLE " + Categories.TABLE_NAME + " (" +
                Categories._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Categories.NAME + " TEXT NOT NULL," +
                Categories.PARENT_ID + " INTEGER DEFAULT NULL," +
                "CONSTRAINT fk_parent_id FOREIGN KEY (" + Categories.PARENT_ID + ") REFERENCES " +
                Categories.TABLE_NAME + " (" + Categories._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE)");
        db.execSQL("CREATE INDEX idx_parent_id ON " + Categories.TABLE_NAME + " (" +
                Categories.PARENT_ID + " ASC)");

        // Create table "accounts".
        db.execSQL("CREATE TABLE " + Accounts.TABLE_NAME + " (" +
                Accounts._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Accounts.TITLE + " TEXT NOT NULL," +
                Accounts.CURRENCY + " TEXT NOT NULL," +
                Accounts.BALANCE + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.TYPE + " TEXT NOT NULL DEFAULT 'CASH'," +
                Accounts.SUBTYPE + " TEXT DEFAULT NULL," +
                Accounts.IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1," +
                Accounts.INCLUDE_INTO_TOTALS + " INTEGER NOT NULL DEFAULT 1," +
                Accounts.SORT_ORDER + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.NOTE + " TEXT NOT NULL," +
                Accounts.CREATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.LAST_TRANSACTION_AT + " INTEGER NOT NULL DEFAULT 0," +
                Accounts.LAST_ACCOUNT_ID + " INTEGER DEFAULT NULL," +
                Accounts.LAST_CATEGORY_ID + " INTEGER DEFAULT NULL," +
                "CONSTRAINT fk_last_account_id FOREIGN KEY (" + Accounts.LAST_ACCOUNT_ID + ") REFERENCES " +
                        Accounts.TABLE_NAME + " (" + Accounts._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE," +
                "CONSTRAINT fk_last_category_id FOREIGN KEY (" + Accounts.LAST_CATEGORY_ID + ") REFERENCES " +
                        Categories.TABLE_NAME + " (" + Categories._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE)");
        db.execSQL("CREATE INDEX idx_is_active ON " + Accounts.TABLE_NAME + " (" +
                Accounts.IS_ACTIVE + " DESC)");
        db.execSQL("CREATE INDEX idx_last_account_id ON " + Accounts.TABLE_NAME + " (" +
                Accounts.LAST_ACCOUNT_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_last_category_id ON " + Accounts.TABLE_NAME + " (" +
                Accounts.LAST_CATEGORY_ID + " ASC)");

        // Create table "payees".
        db.execSQL("CREATE TABLE " + Payees.TABLE_NAME + " (" +
                Payees._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Payees.NAME + " TEXT NOT NULL)");

        // Create table "running_balances"
        db.execSQL("CREATE TABLE " + RunningBalances.TABLE_NAME + " (" +
                RunningBalances._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                RunningBalances.ACCOUNT_ID + " INTEGER NOT NULL," +
                RunningBalances.TRANSACTION_ID + " INTEGER NOT NULL," +
                RunningBalances.BALANCE + " INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE UNIQUE INDEX uq_account_transaction ON " + RunningBalances.TABLE_NAME + " (" +
                RunningBalances.ACCOUNT_ID + " ASC, " + RunningBalances.TRANSACTION_ID + " ASC)");

        // Create table "transactions".
        db.execSQL("CREATE TABLE " + Transactions.TABLE_NAME + " (" +
                Transactions._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                Transactions.FROM_ACCOUNT_ID + " INTEGER," +
                Transactions.TO_ACCOUNT_ID + " INTEGER," +
                Transactions.PAYEE_ID + " INTEGER DEFAULT NULL," +
                Transactions.OCCURRED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Transactions.CREATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Transactions.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0," +
                Transactions.NOTE + " TEXT NOT NULL," +
                Transactions.ORIGINAL_FROM_CURRENCY + " TEXT DEFAULT NULL," +
                Transactions.ORIGINAL_FROM_AMOUNT + " INTEGER DEFAULT NULL," +
                "CONSTRAINT fk_from_account_id FOREIGN KEY (" + Transactions.FROM_ACCOUNT_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE," +
                "CONSTRAINT fk_to_account_id FOREIGN KEY (" + Transactions.TO_ACCOUNT_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE RESTRICT ON UPDATE CASCADE," +
                "CONSTRAINT fk_payee_id FOREIGN KEY (" + Transactions.PAYEE_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE)");

        db.execSQL("CREATE INDEX idx_from_account_id ON " + Transactions.TABLE_NAME +
                " (" + Transactions.FROM_ACCOUNT_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_to_account_id ON " + Transactions.TABLE_NAME +
                " (" + Transactions.TO_ACCOUNT_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_payee_id ON " + Transactions.TABLE_NAME +
                " (" + Transactions.PAYEE_ID + " ASC)");
        db.execSQL("CREATE INDEX idx_occurred_at ON " + Transactions.TABLE_NAME +
                " (" + Transactions.OCCURRED_AT + " ASC)");

        // Create table "transaction_details".
        db.execSQL("CREATE TABLE " + TransactionDetails.TABLE_NAME + " (" +
                TransactionDetails._ID + " INTEGER NOT NULL," +
                TransactionDetails.TRANSACTION_ID + " INTEGER NOT NULL," +
                TransactionDetails.FROM_AMOUNT + " INTEGER," +
                TransactionDetails.TO_AMOUNT + " INTEGER," +
                TransactionDetails.CATEGORY_ID + " INTEGER," +
                TransactionDetails.IS_TRANSFER + " INTEGER NOT NULL DEFAULT 0," +
                TransactionDetails.IS_SPLIT + " INTEGER NOT NULL DEFAULT 0," +
                "PRIMARY KEY(" + TransactionDetails._ID + ")," +
                "CONSTRAINT fk_transaction_id FOREIGN KEY (" + TransactionDetails.TRANSACTION_ID + ") REFERENCES " +
                        Transactions.TABLE_NAME + " (" + Transactions._ID +
                ") ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT fk_category_id FOREIGN KEY (" + TransactionDetails.CATEGORY_ID + ") REFERENCES " +
                        Categories.TABLE_NAME + " (" + Categories._ID +
                ") ON DELETE SET NULL ON UPDATE CASCADE)");

        db.execSQL("CREATE INDEX idx_transaction_id ON " + TransactionDetails.TABLE_NAME +
                " (" + TransactionDetails.TRANSACTION_ID + " ASC)");

        db.execSQL("CREATE INDEX idx_type ON " + TransactionDetails.TABLE_NAME + " (" +
                TransactionDetails.IS_TRANSFER + " ASC, " +
                TransactionDetails.IS_SPLIT + " ASC, " +
                TransactionDetails.CATEGORY_ID + " ASC)");

        db.execSQL("CREATE INDEX idx_split ON " + TransactionDetails.TABLE_NAME +
                " (" + TransactionDetails.IS_SPLIT + " ASC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
