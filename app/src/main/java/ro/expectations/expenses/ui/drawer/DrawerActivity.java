package ro.expectations.expenses.ui.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.Toast;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.accounts.AccountsActivity;
import ro.expectations.expenses.ui.backup.BackupActivity;
import ro.expectations.expenses.ui.categories.CategoriesActivity;
import ro.expectations.expenses.ui.overview.OverviewActivity;
import ro.expectations.expenses.ui.payees.PayeesActivity;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;

abstract public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Delay to launch nav drawer item, to allow close animation to play.
    private static final int NAV_DRAWER_LAUNCH_DELAY = 250;

    private DrawerLayout mDrawer;
    private Handler mHandler;

    protected ViewStub mMainContent;

    protected void setMainContentView() {
        setContentView(R.layout.activity_drawer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMainContentView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(getSelfNavDrawerItem());

        mMainContent = (ViewStub) findViewById(R.id.main_content);

        // Handler used to delay the launch of activities from the nav drawer.
        mHandler = new Handler();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);

        int id = item.getItemId();

        if (id == R.id.nav_overview) {
            Intent overviewIntent = new Intent(this, OverviewActivity.class);
            startActivityDelayed(overviewIntent);
        } else if (id == R.id.nav_accounts) {
            Intent accountsIntent = new Intent(this, AccountsActivity.class);
            startActivityDelayed(accountsIntent);
        } else if (id == R.id.nav_transactions) {
            Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
            startActivityDelayed(transactionsIntent);
        } else if (id == R.id.nav_backup) {
            Intent backupIntent = new Intent(this, BackupActivity.class);
            startActivityDelayed(backupIntent);
        } else if (id == R.id.nav_categories) {
            Intent categoriesIntent = new Intent(this, CategoriesActivity.class);
            startActivityDelayed(categoriesIntent);
        } else if (id == R.id.nav_payees) {
            Intent payeesIntent = new Intent(this, PayeesActivity.class);
            startActivityDelayed(payeesIntent);
        } else if (id == R.id.nav_exchange_rates) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity.
     *
     * Subclasses of DrawerActivity override this to indicate what nav drawer item corresponds to
     * them.
     */
    abstract protected int getSelfNavDrawerItem();

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
}