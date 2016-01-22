package ro.expectations.expenses.ui.overview;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.transactions.TransactionsFragment;

public class OverviewActivity extends DrawerActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ACCOUNTS = 0;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    protected void setMainContentView() {
        setContentView(R.layout.activity_drawer_tabs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_overview);
        mMainContent.inflate();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
        });

        // Create the adapter that will return a fragment for each primary section of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.swapCursor(null);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Get a reference to the tab layout to populate when the loader finishes loading.
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        // Initialise the account data loader.
        getSupportLoaderManager().initLoader(LOADER_ACCOUNTS, null, this);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_overview;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ExpensesContract.Accounts._ID,
                ExpensesContract.Accounts.TITLE
        };
        String sortOrder = ExpensesContract.Accounts.SORT_ORDER + " ASC";
        String selection = ExpensesContract.Accounts.IS_ACTIVE + " = 1";

        return new CursorLoader(
                this,
                ExpensesContract.Accounts.CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                ExpensesContract.Accounts._ID,
                ExpensesContract.Accounts.TITLE
        });
        matrixCursor.addRow(new Object[] { "0", getString(R.string.all_accounts) });
        MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, data });

        mSectionsPagerAdapter.swapCursor(mergeCursor);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mSectionsPagerAdapter.swapCursor(null);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private Cursor mCursor;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (mCursor == null) {
                return null;
            }

            mCursor.moveToPosition(position);
            int idIndex = mCursor.getColumnIndex(ExpensesContract.Accounts._ID);
            return TransactionsFragment.newInstance(mCursor.getLong(idIndex), false);
        }

        @Override
        public int getCount() {
            if (mCursor == null) {
                return 0;
            } else {
                return mCursor.getCount();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mCursor == null) {
                return null;
            }

            mCursor.moveToPosition(position);
            int titleIndex = mCursor.getColumnIndex(ExpensesContract.Accounts.TITLE);
            return mCursor.getString(titleIndex);
        }

        public void swapCursor(Cursor newCursor) {
            if (mCursor == newCursor) {
                return;
            }
            mCursor = newCursor;
            notifyDataSetChanged();
        }
    }
}
