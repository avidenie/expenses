package ro.expectations.expenses.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import ro.expectations.expenses.provider.ExpensesContract.Accounts;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;

public class ExpensesProvider extends ContentProvider {

    private static final int ROUTE_ACCOUNTS = 1;
    private static final int ROUTE_ACCOUNT_ID = 2;
    private static final int ROUTE_TRANSACTIONS = 3;
    private static final int ROUTE_TRANSACTION_ID = 4;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "accounts", ROUTE_ACCOUNTS);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "accounts/#", ROUTE_ACCOUNT_ID);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "transactions", ROUTE_TRANSACTIONS);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "transactions/#", ROUTE_TRANSACTION_ID);
    }

    private ExpensesDatabase mDatabaseHelper;

    public ExpensesProvider() {
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ExpensesDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ACCOUNTS:
                return Accounts.CONTENT_TYPE;
            case ROUTE_ACCOUNT_ID:
                return Accounts.CONTENT_ITEM_TYPE;
            case ROUTE_TRANSACTIONS:
                return Transactions.CONTENT_TYPE;
            case ROUTE_TRANSACTION_ID:
                return Transactions.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown content provider type URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                queryBuilder.setTables(Accounts.TABLE_NAME);
                break;
            case ROUTE_ACCOUNT_ID:
                queryBuilder.appendWhere(Accounts._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(Accounts.TABLE_NAME);
                break;
            case ROUTE_TRANSACTIONS:
                queryBuilder.setTables(Transactions.TABLE_NAME);
                break;
            case ROUTE_TRANSACTION_ID:
                queryBuilder.appendWhere(Transactions._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(Transactions.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider query URI: " + uri);
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        Uri returnUri;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                id = db.insertOrThrow(Accounts.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(Accounts.CONTENT_URI, id);
                break;
            case ROUTE_TRANSACTIONS:
                id = db.insertOrThrow(Transactions.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(Transactions.CONTENT_URI, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider insert URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int rowsUpdated;
        long id;
        StringBuilder newSelection;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                rowsUpdated = db.update(Accounts.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_ACCOUNT_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Accounts._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsUpdated = db.update(Accounts.TABLE_NAME, values, newSelection.toString(), selectionArgs);
                break;
            case ROUTE_TRANSACTIONS:
                rowsUpdated = db.update(Accounts.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_TRANSACTION_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Transactions._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsUpdated = db.update(Transactions.TABLE_NAME, values, newSelection.toString(), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider update URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int rowsDeleted;
        long id;
        StringBuilder newSelection;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                rowsDeleted = db.delete(Accounts.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_ACCOUNT_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Accounts._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsDeleted = db.delete(Accounts.TABLE_NAME, newSelection.toString(), selectionArgs);
                break;
            case ROUTE_TRANSACTIONS:
                rowsDeleted = db.delete(Accounts.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_TRANSACTION_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Transactions._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsDeleted = db.delete(Transactions.TABLE_NAME, newSelection.toString(), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider delete URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }
}
