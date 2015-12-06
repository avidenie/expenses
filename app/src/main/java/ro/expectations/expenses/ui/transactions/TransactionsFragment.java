package ro.expectations.expenses.ui.transactions;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.widget.DividerItemDecoration;

public class TransactionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_ACCOUNT_ID = "account_id";

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_transactions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        View emptyView = rootView.findViewById(R.id.list_transactions_empty);
        TransactionsAdapter adapter = new TransactionsAdapter(getActivity(), new TransactionsAdapter.OnClickListener() {
            @Override
            public void onClick(long accountId, TransactionsAdapter.ViewHolder vh) {
            }
        }, emptyView);
        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ExpensesContract.Transactions._ID,
                ExpensesContract.Transactions.NOTE,
        };
        String sortOrder = ExpensesContract.Transactions.CREATED_AT + " DESC";
        String selection = "";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Accounts.CONTENT_URI,
                projection,
                selection,
                null,
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
