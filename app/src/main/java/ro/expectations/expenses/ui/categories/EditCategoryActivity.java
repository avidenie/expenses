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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import ro.expectations.expenses.R;

public class EditCategoryActivity extends AppCompatActivity implements EditCategoryFragment.Listener {

    public static final String ARG_CATEGORY_ID = "category_id";
    public static final String TAG_FRAGMENT_EDIT_CATEGORY = "fragment_edit_category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long categoryId = getIntent().getLongExtra(ARG_CATEGORY_ID, 0);
        if (categoryId <= 0) {
            Intent redirectIntent = new Intent(this, CategoriesActivity.class);
            startActivity(redirectIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditCategoryFragment editCategoryFragment = getVisibleEditCategoryFragment();
                    if (editCategoryFragment != null) {
                        editCategoryFragment.changeIcon();
                    }
                }
            });
        }

        ImageView categoryIcon = (ImageView) findViewById(R.id.category_icon);
        if (categoryIcon != null) {
            categoryIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditCategoryFragment editCategoryFragment = getVisibleEditCategoryFragment();
                    if (editCategoryFragment != null) {
                        editCategoryFragment.changeIcon();
                    }
                }
            });
        }

        ImageView changeColor = (ImageView) findViewById(R.id.change_color);
        if (changeColor != null) {
            changeColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditCategoryFragment editCategoryFragment = getVisibleEditCategoryFragment();
                    if (editCategoryFragment != null) {
                        editCategoryFragment.changeColor();
                    }
                }
            });
        }

        if (savedInstanceState == null) {
            EditCategoryFragment fragment = EditCategoryFragment.newInstance(categoryId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, fragment, TAG_FRAGMENT_EDIT_CATEGORY);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                EditCategoryFragment editCategoryFragment = getVisibleEditCategoryFragment();
                if (editCategoryFragment != null) {
                    if (editCategoryFragment.confirmNavigateUp()) {
                        return true;
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        EditCategoryFragment editCategoryFragment = getVisibleEditCategoryFragment();
        if (editCategoryFragment != null) {
            if (editCategoryFragment.confirmBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onNavigateUpConfirmed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onBackPressedConfirmed() {
        super.onBackPressed();
    }

    private EditCategoryFragment getVisibleEditCategoryFragment() {
        EditCategoryFragment editCategoryFragment = (EditCategoryFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_EDIT_CATEGORY);
        if (editCategoryFragment != null && editCategoryFragment.isVisible()) {
            return editCategoryFragment;
        } else {
            return null;
        }
    }
}
