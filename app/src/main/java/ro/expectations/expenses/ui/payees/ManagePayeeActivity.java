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
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.ui.payees;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewStub;

import ro.expectations.expenses.R;

public class ManagePayeeActivity extends AppCompatActivity implements ManagePayeeFragment.Listener {

    public static final String ARG_PAYEE_ID = "payee_id";
    public static final String TAG_FRAGMENT_MANAGE_PAYEE = "fragment_manage_payee";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long payeeId = getIntent().getLongExtra(ARG_PAYEE_ID, 0);
        if (payeeId < 0) {
            Intent redirectIntent = new Intent(this, PayeesActivity.class);
            startActivity(redirectIntent);
            finish();
            return;
        } else if (payeeId == 0) {
            setTitle(getString(R.string.title_new_payee));
        } else {
            setTitle(getString(R.string.title_edit_payee));
        }

        setContentView(R.layout.app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content);
        mainContent.setLayoutResource(R.layout.content_fragment);
        mainContent.inflate();

        if (savedInstanceState == null) {
            ManagePayeeFragment fragment = ManagePayeeFragment.newInstance(payeeId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, fragment, TAG_FRAGMENT_MANAGE_PAYEE);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ManagePayeeFragment managePayeeFragment = getVisibleManagePayeeFragment();
                if (managePayeeFragment != null) {
                    if (managePayeeFragment.confirmNavigateUp()) {
                        return true;
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ManagePayeeFragment managePayeeFragment = getVisibleManagePayeeFragment();
        if (managePayeeFragment != null) {
            if (managePayeeFragment.confirmBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onBackPressedConfirmed() {
        super.onBackPressed();
    }

    @Override
    public void onNavigateUpConfirmed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private ManagePayeeFragment getVisibleManagePayeeFragment() {
        ManagePayeeFragment managePayeeFragment = (ManagePayeeFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MANAGE_PAYEE);
        if (managePayeeFragment != null && managePayeeFragment.isVisible()) {
            return managePayeeFragment;
        } else {
            return null;
        }
    }
}
