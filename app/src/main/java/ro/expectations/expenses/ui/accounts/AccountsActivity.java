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

package ro.expectations.expenses.ui.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.providers.AppBarLayoutProvider;

public class AccountsActivity extends DrawerActivity implements AppBarLayoutProvider {

    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_fragment);
        mMainContent.inflate();

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newAccountIntent = new Intent(AccountsActivity.this, ManageAccountActivity.class);
                startActivity(newAccountIntent);
            }
        });
        fab.setVisibility(View.VISIBLE);

        if (savedInstanceState == null) {
            AccountsFragment fragment = AccountsFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_accounts;
    }
}
