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
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.DrawableHelper;

public class ManageCategoryActivity extends AppCompatActivity implements ManageCategoryFragment.Listener {

    public static final String ARG_CATEGORY_ID = "category_id";
    public static final String ARG_PARENT_CATEGORY_ID = "parent_category_id";
    public static final String TAG_FRAGMENT_MANAGE_CATEGORY = "fragment_manage_category";

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FloatingActionButton mFloatingActionButton;
    private FrameLayout mChangeColor;
    private FrameLayout mChangeColorBackground;
    private ImageView mCategoryIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long categoryId = getIntent().getLongExtra(ARG_CATEGORY_ID, 0);
        if (categoryId < 0) {
            Intent redirectIntent = new Intent(this, CategoriesActivity.class);
            startActivity(redirectIntent);
            finish();
            return;
        } else if (categoryId == 0) {
            setTitle(getString(R.string.title_new_category));
        } else {
            setTitle(getString(R.string.title_edit_category));
        }

        setContentView(R.layout.activity_manage_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ManageCategoryFragment manageCategoryFragment = getVisibleManageCategoryFragment();
                    if (manageCategoryFragment != null) {
                        manageCategoryFragment.changeIcon();
                    }
                }
            });
        }

        mCategoryIcon = (ImageView) findViewById(R.id.category_icon);
        if (mCategoryIcon != null) {
            mCategoryIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManageCategoryFragment manageCategoryFragment = getVisibleManageCategoryFragment();
                    if (manageCategoryFragment != null) {
                        manageCategoryFragment.changeIcon();
                    }
                }
            });
        }

        mChangeColor = (FrameLayout) findViewById(R.id.change_color);
        if (mChangeColor != null) {
            mChangeColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManageCategoryFragment manageCategoryFragment = getVisibleManageCategoryFragment();
                    if (manageCategoryFragment != null) {
                        manageCategoryFragment.changeColor();
                    }
                }
            });
        }

        mChangeColorBackground = (FrameLayout) findViewById(R.id.change_color_background);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        if (savedInstanceState == null) {
            ManageCategoryFragment fragment;
            if (categoryId == 0) {
                long parentCategoryId = getIntent().getLongExtra(ARG_PARENT_CATEGORY_ID, 0);
                fragment = ManageCategoryFragment.newInstanceWithParent(parentCategoryId);
            } else {
                fragment = ManageCategoryFragment.newInstance(categoryId);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, fragment, TAG_FRAGMENT_MANAGE_CATEGORY);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ManageCategoryFragment manageCategoryFragment = getVisibleManageCategoryFragment();
                if (manageCategoryFragment != null) {
                    if (manageCategoryFragment.confirmNavigateUp()) {
                        return true;
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ManageCategoryFragment manageCategoryFragment = getVisibleManageCategoryFragment();
        if (manageCategoryFragment != null) {
            if (manageCategoryFragment.confirmBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        ManageCategoryFragment manageCategoryFragment = getVisibleManageCategoryFragment();
        if (manageCategoryFragment != null) {
            Intent parentActivityIntent = manageCategoryFragment.getParentActivityIntent();
            if (parentActivityIntent != null) {
                return parentActivityIntent;
            }
        }

        return super.getSupportParentActivityIntent();
    }

    @Override
    public void onBackPressedConfirmed() {
        super.onBackPressed();
    }

    @Override
    public void onNavigateUpConfirmed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onColorSelected(@ColorInt int color, @ColorInt int darkColor, @ColorInt int accentColor) {

        // change the collapsing toolbar layout
        mCollapsingToolbarLayout.setBackgroundColor(color);

        // change the support action bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setBackgroundDrawable(new ColorDrawable(color));
        }

        // change the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(darkColor);
        }

        // change the floating action button
        mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(accentColor));

        // change the change icon color
        Drawable background = DrawableHelper.tintWithColor(ContextCompat.getDrawable(this, R.drawable.circle_background_grey), accentColor);
        mChangeColorBackground.setBackground(background);
    }

    @Override
    public void onIconSelected(@DrawableRes int icon) {
        mCategoryIcon.setImageResource(icon);
    }

    @Override
    public void showChangeColor() {
        mChangeColor.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideChangeColor() {
        mChangeColor.setVisibility(View.GONE);
    }

    private ManageCategoryFragment getVisibleManageCategoryFragment() {
        ManageCategoryFragment manageCategoryFragment = (ManageCategoryFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MANAGE_CATEGORY);
        if (manageCategoryFragment != null && manageCategoryFragment.isVisible()) {
            return manageCategoryFragment;
        } else {
            return null;
        }
    }
}
