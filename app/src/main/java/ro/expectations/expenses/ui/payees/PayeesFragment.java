package ro.expectations.expenses.ui.payees;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import ro.expectations.expenses.widget.recyclerview.DividerItemDecoration;

public class PayeesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    public PayeesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_payees, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_payees);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        mEmptyView = (TextView) rootView.findViewById(R.id.list_payees_empty);

        PayeesAdapter adapter = new PayeesAdapter(getActivity(), new PayeesAdapter.OnClickListener() {
            @Override
            public void onClick(long accountId, PayeesAdapter.ViewHolder vh) {
                Snackbar.make(vh.itemView, "Not yet implemented", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
        });
        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ExpensesContract.Payees._ID,
                ExpensesContract.Payees.NAME
        };
        String sortOrder = ExpensesContract.Payees.NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Payees.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((PayeesAdapter) mRecyclerView.getAdapter()).swapCursor(data);
        mEmptyView.setVisibility(data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((PayeesAdapter) mRecyclerView.getAdapter()).swapCursor(null);
    }
}
