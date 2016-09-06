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

package ro.expectations.expenses.ui.categories;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.utils.ColorStyleUtils;
import ro.expectations.expenses.utils.DrawableUtils;

public class CategoriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_PARENT_CATEGORY_ID = "parent_category_id";

    private long mParentCategoryId;

    private CategoriesAdapter mAdapter;
    private TextView mEmptyView;

    private int mStatusBarColor;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_categories, menu);

            Activity activity = getActivity();
            if (activity instanceof DrawerActivity) {
                ((DrawerActivity) activity).lockNavigationDrawer();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int primaryColorDark = ColorStyleUtils.getColorFromTheme(getActivity(), R.attr.colorPrimaryDark);
                    getActivity().getWindow().setStatusBarColor(0XFF000000 | primaryColorDark);
                }
            }

            MenuItem actionEditCategory = menu.findItem(R.id.action_edit_category);
            actionEditCategory.setIcon(DrawableUtils.tint(getContext(), actionEditCategory.getIcon(), R.color.colorWhite));
            MenuItem actionDeleteCategory = menu.findItem(R.id.action_delete_category);
            actionDeleteCategory.setIcon(DrawableUtils.tint(getContext(), actionDeleteCategory.getIcon(), R.color.colorWhite));

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedCategories = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_categories,
                    selectedCategories,
                    selectedCategories
            ));
            if (selectedCategories == 1) {
                menu.findItem(R.id.action_edit_category).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_category).setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_edit_category:
                    if (mAdapter.getSelectedItemCount() == 1) {
                        int position = mAdapter.getSelectedItemPositions().get(0);
                        long categoryId = mAdapter.getItemId(position);
                        Intent editCategoryIntent = new Intent(getActivity(), ManageCategoryActivity.class);
                        editCategoryIntent.putExtra(ManageCategoryActivity.ARG_CATEGORY_ID, categoryId);
                        startActivity(editCategoryIntent);
                    }
                    mode.finish();
                    return true;
                case R.id.action_delete_category:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            mActionMode = null;
            if (mAdapter.isChoiceMode()) {
                mAdapter.clearSelection();
            }

            Activity activity = getActivity();
            if (activity instanceof DrawerActivity) {

                ((DrawerActivity) activity).unlockNavigationDrawer();

                // reset the status bar color to default
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().getWindow().setStatusBarColor(mStatusBarColor);
                }
            }
        }
    };

    public static CategoriesFragment newInstance(long parentCategoryId) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARENT_CATEGORY_ID, parentCategoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParentCategoryId = getArguments().getLong(ARG_PARENT_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBarColor = getActivity().getWindow().getStatusBarColor();
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);

        mEmptyView = (TextView) view.findViewById(R.id.list_categories_empty);
        if (mParentCategoryId > 0) {
            mEmptyView.setText(getString(R.string.no_subcategories_defined));
        }

        mAdapter = new CategoriesAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    mAdapter.setItemSelected(position, false);
                    if (!mAdapter.isChoiceMode()) {
                        mActionMode.finish();
                    } else {
                        mActionMode.invalidate();
                    }
                } else if (mAdapter.isChoiceMode()) {
                    mAdapter.setItemSelected(position, true);
                    mActionMode.invalidate();
                } else if (mParentCategoryId == 0) {
                    Cursor cursor = mAdapter.getCursor();
                    cursor.moveToPosition(position);
                    Intent subcategoryIntent = new Intent(getActivity(), SubcategoriesActivity.class);
                    subcategoryIntent.putExtra(SubcategoriesActivity.ARG_PARENT_CATEGORY_ID, cursor.getLong(CategoriesAdapter.COLUMN_CATEGORY_ID));
                    subcategoryIntent.putExtra(SubcategoriesActivity.ARG_PARENT_CATEGORY_STYLE, cursor.getString(CategoriesAdapter.COLUMN_CATEGORY_STYLE));
                    getActivity().startActivity(subcategoryIntent);
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                mAdapter.setItemSelected(position, !mAdapter.isItemSelected(position));
                if (mAdapter.isChoiceMode()) {
                    if (mActionMode == null) {
                        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    } else {
                        mActionMode.invalidate();
                    }
                } else {
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.isChoiceMode() && mActionMode == null) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;
        if (mParentCategoryId > 0) {
            selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID  + " = ?";
            selectionArgs = new String[] { String.valueOf(mParentCategoryId) };
        } else{
            selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories._ID  + " > 0 AND "
                    + ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID  + " IS NULL";
            selectionArgs = null;
        }

        String sortOrder = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Categories.CONTENT_URI,
                CategoriesAdapter.PROJECTION,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mEmptyView.setVisibility(data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
