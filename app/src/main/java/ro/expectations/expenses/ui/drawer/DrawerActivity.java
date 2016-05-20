/*
 * Copyright © 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    private DrawerLayout mDrawerLayout;
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        mDrawerLayout.closeDrawer(GravityCompat.START);

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

    public void lockNavigationDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockNavigationDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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