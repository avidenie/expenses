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

package ro.expectations.expenses.ui.backup;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.providers.AppBarLayoutProvider;
import ro.expectations.expenses.ui.providers.FloatingActionButtonProvider;

public class BackupActivity extends DrawerActivity implements FloatingActionButtonProvider, AppBarLayoutProvider {

    private FloatingActionButton mFloatingActionButton;
    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_fragment);
        mMainContent.inflate();

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        Drawable icon = DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_cloud_upload_black_24dp));
        DrawableCompat.setTint(icon, ContextCompat.getColor(this, R.color.colorWhite));
        mFloatingActionButton.setImageDrawable(icon);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackupFragment backupFragment = (BackupFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                if (backupFragment != null && backupFragment.isVisible()) {
                    backupFragment.fabAction();
                }
            }
        });

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        if (savedInstanceState == null) {
            BackupFragment fragment = BackupFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_backup;
    }

    @Override
    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }
}
