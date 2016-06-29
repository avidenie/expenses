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

package ro.expectations.expenses.ui.categories;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;

import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.ColorHelper;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.common.OnAppBarHeightChangeListener;
import ro.expectations.expenses.utils.LayoutUtils;

public class SubcategoriesActivity extends AppCompatActivity
        implements OnAppBarHeightChangeListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_PARENT_CATEGORY_ID = "parent_category_id";

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;

    private long mParentCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentCategoryId = getIntent().getLongExtra(ARG_PARENT_CATEGORY_ID, 0);
        if (mParentCategoryId == 0) {
            Intent categoryIntent = new Intent(this, CategoriesActivity.class);
            startActivity(categoryIntent);
        }

        setContentView(R.layout.app_bar);

        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content);
        mainContent.setLayoutResource(R.layout.content_categories);
        mainContent.inflate();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        getSupportLoaderManager().initLoader(0, null, this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
        });

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        if (savedInstanceState == null) {
            CategoriesFragment fragment = CategoriesFragment.newInstance(mParentCategoryId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onAppBarHeightChange(boolean expand) {
        LayoutUtils.changeAppBarHeight(this, mAppBarLayout, expand);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ExpensesContract.Categories.NAME,
                ExpensesContract.Categories.COLOR
        };

        return new CursorLoader(
                this,
                ContentUris.withAppendedId(ExpensesContract.Categories.CONTENT_URI, mParentCategoryId),
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            data.moveToPosition(0);

            ActionBar supportActionBar = getSupportActionBar();

            // set the title
            String categoryName = data.getString(data.getColumnIndex(ExpensesContract.Categories.NAME));
            setTitle(categoryName);
            if (supportActionBar != null) {
                supportActionBar.setDisplayShowTitleEnabled(true);
            }

            // retrieve the parent category color
            int color = ColorHelper.fromRGB(data.getString(data.getColumnIndex(ExpensesContract.Categories.COLOR)), ContextCompat.getColor(this, R.color.colorPrimary));

            // change the collapsing toolbar layout
            mCollapsingToolbarLayout.setBackgroundColor(color);

            // change the support action bar
            if (supportActionBar != null) {
                supportActionBar.setBackgroundDrawable(new ColorDrawable(color));
            }

            // change the status bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[] colors = getResources().getIntArray(R.array.colorPicker);
                for(int i = 0, n = colors.length; i < n; i++) {
                    if (colors[i] == color) {
                        int[] darkColors = getResources().getIntArray(R.array.colorPickerDark);
                        getWindow().setStatusBarColor(darkColors[i]);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do
    }
}
