package ro.expectations.expenses.ui.accounts;

import android.content.Intent;
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
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;
import ro.expectations.expenses.widget.recyclerview.DividerItemDecoration;

public class AccountsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accounts, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_accounts);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        mEmptyView = (TextView) rootView.findViewById(R.id.list_accounts_empty);
        AccountsAdapter adapter = new AccountsAdapter(getActivity(), new AccountsAdapter.OnClickListener() {
                @Override
                public void onClick(long accountId, AccountsAdapter.ViewHolder vh) {
                    Intent transactionsListingIntent = new Intent(getActivity(), TransactionsActivity.class);
                    transactionsListingIntent.putExtra(TransactionsActivity.ARG_ACCOUNT_ID, accountId);
                    startActivity(transactionsListingIntent);
                }
            });
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
        String[] projection = {
                ExpensesContract.Accounts._ID,
                ExpensesContract.Accounts.TITLE,
                ExpensesContract.Accounts.CURRENCY,
                ExpensesContract.Accounts.BALANCE,
                ExpensesContract.Accounts.LAST_TRANSACTION_AT,
                ExpensesContract.Accounts.CREATED_AT,
                ExpensesContract.Accounts.TYPE,
                ExpensesContract.Accounts.SUBTYPE
        };
        String sortOrder = ExpensesContract.Accounts.SORT_ORDER + " ASC";
        String selection = ExpensesContract.Accounts.IS_ACTIVE + " = 1";

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
        ((AccountsAdapter) mRecyclerView.getAdapter()).swapCursor(data);
        mEmptyView.setVisibility(data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((AccountsAdapter) mRecyclerView.getAdapter()).swapCursor(null);
    }
}
