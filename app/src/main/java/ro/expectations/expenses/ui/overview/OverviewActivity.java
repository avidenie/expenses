package ro.expectations.expenses.ui.overview;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.accounts.AccountsActivity;
import ro.expectations.expenses.ui.backup.BackupActivity;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;

public class OverviewActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    // Delay to launch nav drawer item, to allow close animation to play.
    private static final int NAV_DRAWER_LAUNCH_DELAY = 250;

    private static final int LOADER_ACCOUNTS = 0;

    private DrawerLayout mDrawer;
    private Handler mHandler;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_overview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup drawer and drawer toggle.
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation view.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_overview);

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

        // Handler used to delay the launch of activities from the nav drawer.
        mHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);

        int id = item.getItemId();
        if (id != R.id.nav_overview) {
            if (id == R.id.nav_accounts) {
                Intent accountsIntent = new Intent(this, AccountsActivity.class);
                startActivityDelayed(accountsIntent);
            } else if (id == R.id.nav_transactions) {
                Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
                startActivityDelayed(transactionsIntent);
            } else if (id == R.id.nav_backup) {
                Intent backupIntent = new Intent(this, BackupActivity.class);
                startActivityDelayed(backupIntent);
            }
        }

        return true;
    }

    /**
     * Start the activity after a short delay, to allow the nav drawer close animation to play.
     */
    private void startActivityDelayed(final Intent intent) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, NAV_DRAWER_LAUNCH_DELAY);
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
        mSectionsPagerAdapter.swapCursor(data);
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
            return AccountDetailsFragment.newInstance(mCursor.getLong(idIndex));
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
