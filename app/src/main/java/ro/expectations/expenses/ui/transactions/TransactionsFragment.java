package ro.expectations.expenses.ui.transactions;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract.Categories;
import ro.expectations.expenses.provider.ExpensesContract.FromAccounts;
import ro.expectations.expenses.provider.ExpensesContract.FromRunningBalances;
import ro.expectations.expenses.provider.ExpensesContract.ParentCategories;
import ro.expectations.expenses.provider.ExpensesContract.Payees;
import ro.expectations.expenses.provider.ExpensesContract.ToAccounts;
import ro.expectations.expenses.provider.ExpensesContract.ToRunningBalances;
import ro.expectations.expenses.provider.ExpensesContract.TransactionDetails;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;
import ro.expectations.expenses.ui.widget.DividerItemDecoration;

public class TransactionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String ARG_ACCOUNT_ID = "account_id";

    private static final String[] PROJECTION = {
            Transactions._ID,
            Transactions.FROM_ACCOUNT_ID,
            FromAccounts.FROM_TITLE,
            TransactionDetails.FROM_AMOUNT,
            FromAccounts.FROM_CURRENCY,
            FromRunningBalances.FROM_BALANCE,
            Transactions.TO_ACCOUNT_ID,
            ToAccounts.TO_TITLE,
            TransactionDetails.TO_AMOUNT,
            ToAccounts.TO_CURRENCY,
            ToRunningBalances.TO_BALANCE,
            Categories.CATEGORY_ID,
            Categories.CATEGORY_NAME,
            Categories.PARENT_ID,
            ParentCategories.PARENT_NAME,
            Transactions.PAYEE_ID,
            Payees.PAYEE_NAME,
            Transactions.NOTE,
            TransactionDetails.IS_SPLIT,
            Transactions.OCCURRED_AT,
            Transactions.CREATED_AT,
            Transactions.ORIGINAL_AMOUNT,
            Transactions.ORIGINAL_CURRENCY
    };
    static final int COLUMN_TRANSACTION_ID = 0;
    static final int COLUMN_FROM_ACCOUNT_ID = 1;
    static final int COLUMN_FROM_ACCOUNT_TITLE = 2;
    static final int COLUMN_FROM_AMOUNT = 3;
    static final int COLUMN_FROM_CURRENCY = 4;
    static final int COLUMN_FROM_BALANCE = 5;
    static final int COLUMN_TO_ACCOUNT_ID = 6;
    static final int COLUMN_TO_ACCOUNT_TITLE = 7;
    static final int COLUMN_TO_AMOUNT = 8;
    static final int COLUMN_TO_CURRENCY = 9;
    static final int COLUMN_TO_BALANCE = 10;
    static final int COLUMN_CATEGORY_ID = 11;
    static final int COLUMN_CATEGORY_NAME = 12;
    static final int COLUMN_PARENT_CATEGORY_ID = 13;
    static final int COLUMN_PARENT_CATEGORY_NAME = 14;
    static final int COLUMN_PAYEE_ID = 15;
    static final int COLUMN_PAYEE_NAME = 16;
    static final int COLUMN_NOTE = 17;
    static final int COLUMN_IS_SPLIT = 18;
    static final int COLUMN_OCCURED_AT = 19;
    static final int COLUMN_CREATED_AT = 20;
    static final int COLUMN_ORIGINAL_AMOUNT = 21;
    static final int COLUMN_ORIGINAL_CURRENCY = 22;

    private RecyclerView mRecyclerView;
    private long mSelectedAccountId;

    public static TransactionsFragment newInstance(long accountId) {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedAccountId = getArguments().getLong(ARG_ACCOUNT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_transactions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        View emptyView = rootView.findViewById(R.id.list_transactions_empty);
        TransactionsAdapter adapter = new TransactionsAdapter(getActivity(), mSelectedAccountId, new TransactionsAdapter.OnClickListener() {
            @Override
            public void onClick(long accountId, TransactionsAdapter.ViewHolder vh) {
            }
        }, emptyView);
        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = Transactions.TABLE_NAME + "." + Transactions.CREATED_AT + " DESC";
        String selection = TransactionDetails.TABLE_NAME + "." + TransactionDetails.IS_SPLIT + " = 0";
        String[] selectionArgs = null;
        if (mSelectedAccountId > 0) {
            selection += " AND (" + Transactions.TABLE_NAME + "." + Transactions.FROM_ACCOUNT_ID
                    + " = ? OR " + Transactions.TABLE_NAME + "." + Transactions.TO_ACCOUNT_ID
                    + " = ?)";
            selectionArgs = new String[] {String.valueOf(mSelectedAccountId), String.valueOf(mSelectedAccountId)};
        }

        return new CursorLoader(
                getActivity(),
                Transactions.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((TransactionsAdapter) mRecyclerView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((TransactionsAdapter) mRecyclerView.getAdapter()).swapCursor(null);
    }
}
