package ro.expectations.expenses.utils;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.provider.ExpensesContract.Accounts;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;
import ro.expectations.expenses.provider.ExpensesContract.TransactionDetails;
import ro.expectations.expenses.provider.ExpensesContract.RunningBalances;

public class IntegrityFix {

    private static final String TAG = IntegrityFix.class.getSimpleName();

    private Context mContext;

    public IntegrityFix(Context context) {
        mContext = context;
    }

    public void fix() throws RemoteException, OperationApplicationException {
        recalculateAccountsBalances();
        rebuildRunningBalances();
    }

    public void recalculateAccountsBalances() throws RemoteException, OperationApplicationException {
        long t0 = System.currentTimeMillis();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        Cursor accounts = mContext.getContentResolver().query(Accounts.CONTENT_URI,
                new String[] {Accounts._ID},
                null, null, null);
        try {
            while (accounts.moveToNext()) {
                long accountId = accounts.getLong(0);
                recalculateAccountBalance(operations, accountId);
            }
        } finally {
            accounts.close();
        }

        if (operations.size() > 0) {
            mContext.getContentResolver().applyBatch(ExpensesContract.CONTENT_AUTHORITY, operations);
        }

        long t1 = System.currentTimeMillis();
        Log.i(TAG, "Recalculating balances took " + TimeUnit.MILLISECONDS.toSeconds(t1 - t0) + "s");
    }

    private void recalculateAccountBalance(ArrayList<ContentProviderOperation> operations, long accountId) {
        long balance = fetchAccountBalance(accountId);
        operations.add(ContentProviderOperation.newUpdate(Accounts.CONTENT_URI)
                .withValue(Accounts.BALANCE, balance)
                .withSelection(Accounts._ID + " = ?",
                        new String[]{String.valueOf(accountId)})
                .build());
    }

    private long fetchAccountBalance(long accountId) {
        long balance = 0;

        Cursor fromCursor = mContext.getContentResolver().query(ExpensesContract.Transactions.CONTENT_URI,
                new String[] {TransactionDetails.FROM_AMOUNT_SUM},
                Transactions.FROM_ACCOUNT_ID + " = ? AND " + TransactionDetails.IS_SPLIT + " = 0",
                new String[] {String.valueOf(accountId)},
                null);
        try {
            if (fromCursor.moveToFirst()) {
                balance -= fromCursor.getLong(0);
            }
        } finally {
            fromCursor.close();
        }

        Cursor toCursor = mContext.getContentResolver().query(ExpensesContract.Transactions.CONTENT_URI,
                new String[]{TransactionDetails.TO_AMOUNT_SUM},
                Transactions.TO_ACCOUNT_ID + " = ? AND " + TransactionDetails.IS_SPLIT + " = 0",
                new String[]{String.valueOf(accountId)},
                null);
        try {
            if (toCursor.moveToFirst()) {
                balance += toCursor.getLong(0);
            }
        } finally {
            toCursor.close();
        }

        return balance;
    }

    public void rebuildRunningBalances() throws RemoteException, OperationApplicationException {
        long t0 = System.currentTimeMillis();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        Cursor accounts = mContext.getContentResolver().query(Accounts.CONTENT_URI,
                new String[] {Accounts._ID},
                null, null, null);
        try {
            while (accounts.moveToNext()) {
                long accountId = accounts.getLong(0);
                rebuildRunningBalancesForAccount(operations, accountId);
            }
        } finally {
            accounts.close();
        }

        if (operations.size() > 0) {
            mContext.getContentResolver().applyBatch(ExpensesContract.CONTENT_AUTHORITY, operations);
        }

        long t1 = System.currentTimeMillis();
        Log.i(TAG, "Updating running balances took " + TimeUnit.MILLISECONDS.toSeconds(t1 - t0) + "s");
    }

    private void rebuildRunningBalancesForAccount(ArrayList<ContentProviderOperation> operations, long accountId) {
        // Delete all running balances for this account.
        operations.add(ContentProviderOperation.newDelete(RunningBalances.CONTENT_URI)
                .withSelection(RunningBalances.ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)})
                .build());

        // Retrieve all transactions for this account.
        String[] projection = {
                Transactions._ID,
                Transactions.FROM_ACCOUNT_ID,
                TransactionDetails.FROM_AMOUNT,
                Transactions.TO_ACCOUNT_ID,
                TransactionDetails.TO_AMOUNT
        };
        String selection = TransactionDetails.TABLE_NAME + "." + TransactionDetails.IS_SPLIT + " = 0"
                + " AND (" + Transactions.TABLE_NAME + "." + Transactions.FROM_ACCOUNT_ID
                + " = ? OR " + Transactions.TABLE_NAME + "." + Transactions.TO_ACCOUNT_ID
                + " = ?)";
        String[] selectionArgs = new String[] {
                String.valueOf(accountId),
                String.valueOf(accountId)
        };
        String sortOrder = Transactions.TABLE_NAME + "." + Transactions.CREATED_AT + " ASC, "
                + Transactions.TABLE_NAME + "." + Transactions._ID + " ASC";
        Cursor transactions = mContext.getContentResolver().query(Transactions.CONTENT_URI,
                projection, selection, selectionArgs, sortOrder);
        try {
            long balance = 0;
            while (transactions.moveToNext()) {
                long transactionId = transactions.getLong(0);
                long fromAccountId = transactions.getLong(1);
                long toAccountId = transactions.getLong(3);
                if (fromAccountId == accountId) {
                    long fromAmount = transactions.getLong(2);
                    balance -= fromAmount;
                    operations.add(ContentProviderOperation.newInsert(RunningBalances.CONTENT_URI)
                            .withValue(RunningBalances.ACCOUNT_ID, accountId)
                            .withValue(RunningBalances.TRANSACTION_ID, transactionId)
                            .withValue(RunningBalances.BALANCE, balance)
                            .build());
                } else if(toAccountId == accountId) {
                    long toAmount = transactions.getLong(4);
                    balance += toAmount;
                    operations.add(ContentProviderOperation.newInsert(RunningBalances.CONTENT_URI)
                            .withValue(RunningBalances.ACCOUNT_ID, accountId)
                            .withValue(RunningBalances.TRANSACTION_ID, transactionId)
                            .withValue(RunningBalances.BALANCE, balance)
                            .build());
                }
            }
        } finally {
            transactions.close();
        }

    }
}
