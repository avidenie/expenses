package ro.expectations.expenses.ui.widget;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewStub;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.accounts.AccountsActivity;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;

abstract public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String LOG_TAG = DrawerActivity.class.getSimpleName();

    // Delay to launch nav drawer item, to allow close animation to play.
    private static final int NAV_DRAWER_LAUNCH_DELAY = 250;

    private DrawerLayout mDrawer;
    private Handler mHandler;

    protected ViewStub mMainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id != getSelfNavDrawerItem()) {
            if (id == R.id.nav_accounts) {
                Intent accountsIntent = new Intent(DrawerActivity.this, AccountsActivity.class);
                startActivityDelayed(accountsIntent);
            } else if (id == R.id.nav_transactions) {
                Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
                startActivityDelayed(transactionsIntent);
            } else if (id == R.id.nav_slideshow) {

            } else if (id == R.id.nav_manage) {

            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }
        }

        mDrawer.closeDrawer(GravityCompat.START);
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
