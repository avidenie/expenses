package ro.expectations.expenses.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ro.expectations.expenses.provider.ExpensesContract.Accounts;
import ro.expectations.expenses.provider.ExpensesContract.Categories;
import ro.expectations.expenses.provider.ExpensesContract.Payees;
import ro.expectations.expenses.provider.ExpensesContract.Subcategories;
import ro.expectations.expenses.provider.ExpensesContract.TransactionDetails;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;

public class ExpensesProvider extends ContentProvider {

    private static final int ROUTE_ACCOUNTS = 101;
    private static final int ROUTE_ACCOUNT_ID = 102;
    private static final int ROUTE_CATEGORIES = 103;
    private static final int ROUTE_CATEGORY_ID = 104;
    private static final int ROUTE_PAYEES = 105;
    private static final int ROUTE_PAYEE_ID = 106;
    private static final int ROUTE_TRANSACTIONS = 107;
    private static final int ROUTE_TRANSACTION_ID = 108;
    private static final int ROUTE_TRANSACTION_DETAILS = 109;
    private static final int ROUTE_TRANSACTION_DETAILS_ID = 110;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "accounts", ROUTE_ACCOUNTS);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "accounts/#", ROUTE_ACCOUNT_ID);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "categories", ROUTE_CATEGORIES);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "categories/#", ROUTE_CATEGORY_ID);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "payees", ROUTE_PAYEES);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "payees/#", ROUTE_PAYEE_ID);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "transactions", ROUTE_TRANSACTIONS);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "transactions/#", ROUTE_TRANSACTION_ID);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "transaction_details", ROUTE_TRANSACTION_DETAILS);
        sUriMatcher.addURI(ExpensesContract.CONTENT_AUTHORITY, "transaction_details/#", ROUTE_TRANSACTION_DETAILS_ID);
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
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ACCOUNTS:
                return Accounts.CONTENT_TYPE;
            case ROUTE_ACCOUNT_ID:
                return Accounts.CONTENT_ITEM_TYPE;
            case ROUTE_CATEGORIES:
                return Categories.CONTENT_TYPE;
            case ROUTE_CATEGORY_ID:
                return Categories.CONTENT_ITEM_TYPE;
            case ROUTE_PAYEES:
                return Payees.CONTENT_TYPE;
            case ROUTE_PAYEE_ID:
                return Payees.CONTENT_ITEM_TYPE;
            case ROUTE_TRANSACTIONS:
                return Transactions.CONTENT_TYPE;
            case ROUTE_TRANSACTION_ID:
                return Transactions.CONTENT_ITEM_TYPE;
            case ROUTE_TRANSACTION_DETAILS:
                return Transactions.CONTENT_TYPE;
            case ROUTE_TRANSACTION_DETAILS_ID:
                return Transactions.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown content provider type URI: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String groupBy = null;

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                queryBuilder.setTables(Accounts.TABLE_NAME);
                break;
            case ROUTE_ACCOUNT_ID:
                queryBuilder.appendWhere(Accounts._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(Accounts.TABLE_NAME);
                break;
            case ROUTE_CATEGORIES:
                queryBuilder.setTables(Categories.TABLE_NAME + " LEFT OUTER JOIN "
                        + Categories.TABLE_NAME + " AS " + Subcategories.TABLE_NAME + " ON "
                        + Categories.TABLE_NAME + "." + Categories._ID + " = "
                        + Subcategories.TABLE_NAME + "." + Categories.PARENT_ID);
                Map<String, String> projectionMap = new HashMap<>();
                projectionMap.put(Categories._ID, Categories.TABLE_NAME + "." + Categories._ID);
                projectionMap.put(Categories.NAME, Categories.TABLE_NAME + "." + Categories.NAME);
                projectionMap.put(Categories.PARENT_ID, Categories.TABLE_NAME + "." + Categories.PARENT_ID);
                projectionMap.put(Categories.CHILDREN, "COUNT(" + ExpensesContract.Subcategories.TABLE_NAME + "." + Categories._ID + ")");
                queryBuilder.setProjectionMap(projectionMap);
                groupBy = Categories.TABLE_NAME + "." + Categories._ID;
                break;
            case ROUTE_CATEGORY_ID:
                queryBuilder.appendWhere(Categories._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(Categories.TABLE_NAME);
                break;
            case ROUTE_PAYEES:
                queryBuilder.setTables(Payees.TABLE_NAME);
                break;
            case ROUTE_PAYEE_ID:
                queryBuilder.appendWhere(Payees._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(Payees.TABLE_NAME);
                break;
            case ROUTE_TRANSACTIONS:
                queryBuilder.setTables(Transactions.TABLE_NAME);
                break;
            case ROUTE_TRANSACTION_ID:
                queryBuilder.appendWhere(Transactions._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(Transactions.TABLE_NAME);
                break;
            case ROUTE_TRANSACTION_DETAILS:
                queryBuilder.setTables(TransactionDetails.TABLE_NAME);
                break;
            case ROUTE_TRANSACTION_DETAILS_ID:
                queryBuilder.appendWhere(TransactionDetails._ID + "=" + ContentUris.parseId(uri));
                queryBuilder.setTables(TransactionDetails.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider query URI: " + uri);
        }

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id;
        Uri returnUri;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                id = db.insertOrThrow(Accounts.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(Accounts.CONTENT_URI, id);
                break;
            case ROUTE_CATEGORIES:
                id = db.insertOrThrow(Categories.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(Categories.CONTENT_URI, id);
                break;
            case ROUTE_PAYEES:
                id = db.insertOrThrow(Payees.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(Payees.CONTENT_URI, id);
                break;
            case ROUTE_TRANSACTIONS:
                id = db.insertOrThrow(Transactions.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(Transactions.CONTENT_URI, id);
                break;
            case ROUTE_TRANSACTION_DETAILS:
                id = db.insertOrThrow(TransactionDetails.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(TransactionDetails.CONTENT_URI, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider insert URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
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
            case ROUTE_CATEGORIES:
                rowsUpdated = db.update(Categories.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_CATEGORY_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Categories._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsUpdated = db.update(Categories.TABLE_NAME, values, newSelection.toString(), selectionArgs);
                break;
            case ROUTE_PAYEES:
                rowsUpdated = db.update(Payees.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_PAYEE_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Payees._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsUpdated = db.update(Payees.TABLE_NAME, values, newSelection.toString(), selectionArgs);
                break;
            case ROUTE_TRANSACTIONS:
                rowsUpdated = db.update(Transactions.TABLE_NAME, values, selection, selectionArgs);
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
            case ROUTE_TRANSACTION_DETAILS:
                rowsUpdated = db.update(TransactionDetails.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_TRANSACTION_DETAILS_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(TransactionDetails._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsUpdated = db.update(TransactionDetails.TABLE_NAME, values, newSelection.toString(), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider update URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int rowsDeleted;
        long id;
        StringBuilder newSelection;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ROUTE_ACCOUNTS:
                rowsDeleted = db.delete(Accounts.TABLE_NAME, selection, selectionArgs);
                if (selection == null) {
                    db.delete("sqlite_sequence", "name = ?", new String[] {Accounts.TABLE_NAME});
                }
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
            case ROUTE_CATEGORIES:
                rowsDeleted = db.delete(Categories.TABLE_NAME, selection, selectionArgs);
                if (selection == null) {
                    db.delete("sqlite_sequence", "name = ?", new String[] {Categories.TABLE_NAME});
                }
                break;
            case ROUTE_CATEGORY_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Categories._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsDeleted = db.delete(Categories.TABLE_NAME, newSelection.toString(), selectionArgs);
                break;
            case ROUTE_PAYEES:
                rowsDeleted = db.delete(Payees.TABLE_NAME, selection, selectionArgs);
                if (selection == null) {
                    db.delete("sqlite_sequence", "name = ?", new String[] {Payees.TABLE_NAME});
                }
                break;
            case ROUTE_PAYEE_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(Payees._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsDeleted = db.delete(Payees.TABLE_NAME, newSelection.toString(), selectionArgs);
                break;
            case ROUTE_TRANSACTIONS:
                rowsDeleted = db.delete(Transactions.TABLE_NAME, selection, selectionArgs);
                if (selection == null) {
                    db.delete("sqlite_sequence", "name = ?", new String[] {Transactions.TABLE_NAME});
                }
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
            case ROUTE_TRANSACTION_DETAILS:
                rowsDeleted = db.delete(TransactionDetails.TABLE_NAME, selection, selectionArgs);
                if (selection == null) {
                    db.delete("sqlite_sequence", "name = ?", new String[] {TransactionDetails.TABLE_NAME});
                }
                break;
            case ROUTE_TRANSACTION_DETAILS_ID:
                id = ContentUris.parseId(uri);
                newSelection = new StringBuilder();
                newSelection.append(TransactionDetails._ID).append("=").append(id);
                if (!TextUtils.isEmpty(selection)) {
                    newSelection.append(" AND ").append(selection);
                }
                rowsDeleted = db.delete(TransactionDetails.TABLE_NAME, newSelection.toString(), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown content provider delete URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {

        int i = 0;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentProviderResult[] results = new ContentProviderResult[operations.size()];

        db.beginTransaction();
        try {
            for (ContentProviderOperation operation: operations) {
                results[i++] = operation.apply(this, results, i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return results;
    }
}
