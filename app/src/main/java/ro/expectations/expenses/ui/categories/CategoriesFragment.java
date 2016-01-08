package ro.expectations.expenses.ui.categories;

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
import ro.expectations.expenses.ui.widget.DividerItemDecoration;

public class CategoriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] PROJECTION = {
            ExpensesContract.Categories._ID,
            ExpensesContract.Categories.NAME,
            ExpensesContract.Categories.PARENT_ID,
            ExpensesContract.Categories.CHILDREN
    };
    static final int COLUMN_CATEGORY_ID = 0;
    static final int COLUMN_CATEGORY_NAME = 1;
    static final int COLUMN_CATEGORY_PARENT_ID = 2;
    static final int COLUMN_CATEGORY_CHILDREN = 3;

    private static final String ARG_PARENT_CATEGORY_ID = "parent_category_id";

    private long mParentCategoryId;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance(long parentCategoryId) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARENT_CATEGORY_ID, parentCategoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParentCategoryId = getArguments().getLong(ARG_PARENT_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_categories);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        mEmptyView = (TextView) rootView.findViewById(R.id.list_categories_empty);
        if (mParentCategoryId > 0) {
            mEmptyView.setText(getString(R.string.no_subcategories_defined));
        }
        CategoriesAdapter adapter = new CategoriesAdapter(getActivity(), new CategoriesAdapter.OnClickListener() {
                @Override
                public void onClick(long categoryId, CategoriesAdapter.ViewHolder vh) {
                    if (mParentCategoryId > 0) {
                        return;
                    }
                    Intent subcategoryIntent = new Intent(getActivity(), SubcategoriesActivity.class);
                    subcategoryIntent.putExtra(SubcategoriesActivity.ARG_PARENT_CATEGORY_ID, categoryId);
                    getActivity().startActivity(subcategoryIntent);
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
        String selection;
        String[] selectionArgs;
        if (mParentCategoryId > 0) {
            selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID  + " = ?";
            selectionArgs = new String[] { String.valueOf(mParentCategoryId) };
        } else{
            selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories._ID  + " > 0 AND "
                    + ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID  + " IS NULL";
            selectionArgs = null;
        }

        String sortOrder = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Categories.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((CategoriesAdapter) mRecyclerView.getAdapter()).swapCursor(data);
        mEmptyView.setVisibility(data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CategoriesAdapter) mRecyclerView.getAdapter()).swapCursor(null);
    }
}
