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

package ro.expectations.expenses.ui.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.common.OnAppBarHeightChangeListener;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.utils.LayoutUtils;

public class AccountsActivity extends DrawerActivity implements OnAppBarHeightChangeListener {

    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_accounts);
        mMainContent.inflate();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newAccountIntent = new Intent(AccountsActivity.this, NewAccountActivity.class);
                startActivity(newAccountIntent);
            }
        });
        fab.setVisibility(View.VISIBLE);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_accounts;
    }

    @Override
    public void onAppBarHeightChange(boolean expand) {
        LayoutUtils.changeAppBarHeight(this, mAppBarLayout, expand);
    }
}
