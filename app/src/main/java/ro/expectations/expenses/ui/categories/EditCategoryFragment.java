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
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.DrawableHelper;
import ro.expectations.expenses.model.Category;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.widget.dialog.CategoryPickerDialogFragment;

public class EditCategoryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, CategoryPickerDialogFragment.Listener {

    private static final int CATEGORY_PICKER_DIALOG_REQUEST_CODE = 0;

    private static final String ARG_CATEGORY_ID = "category_id";

    private long mCategoryId;

    private TextInputEditText mCategoryName;
    private TextInputEditText mCategoryParent;

    public EditCategoryFragment() {
        // Required empty public constructor
    }

    public static EditCategoryFragment newInstance(long categoryId) {
        EditCategoryFragment fragment = new EditCategoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getLong(ARG_CATEGORY_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_edit_category, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCategoryName = (TextInputEditText) view.findViewById(R.id.category_name);
        mCategoryParent = (TextInputEditText) view.findViewById(R.id.category_parent);
        mCategoryParent.setInputType(InputType.TYPE_NULL);
        mCategoryParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryPickerDialogFragment categoryPickerDialogFragment = CategoryPickerDialogFragment.newInstance();
                categoryPickerDialogFragment.setTargetFragment(EditCategoryFragment.this, CATEGORY_PICKER_DIALOG_REQUEST_CODE);
                categoryPickerDialogFragment.show(getFragmentManager(), "category_picker");
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(Menu.NONE, R.id.action_save, Menu.NONE, R.string.action_save);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(DrawableHelper.tint(getActivity(), R.drawable.ic_done_black_24dp, R.color.colorWhite));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_save) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
            getActivity(),
            ContentUris.withAppendedId(ExpensesContract.Categories.CONTENT_URI, mCategoryId),
            Category.PROJECTION,
            null,
            null,
            null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            data.moveToFirst();
            String categoryName = data.getString(Category.COLUMN_CATEGORY_NAME);
            mCategoryName.setText(categoryName);
            long parentId = data.getLong(Category.COLUMN_CATEGORY_PARENT_ID);
            if (parentId > 0) {
                mCategoryParent.setText(data.getString(Category.COLUMN_CATEGORY_PARENT_NAME));
            } else {
                int children = data.getInt(Category.COLUMN_CATEGORY_CHILDREN);
                if (children > 0) {
                    mCategoryParent.setVisibility(View.GONE);
                } else {
                    mCategoryParent.setText(R.string.none);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do
    }

    @Override
    public void onCategorySelected(int targetRequestCode, int parentId, String parentName) {
        if (parentId == 0) {
            mCategoryParent.setText(R.string.none);
        } else {
            mCategoryParent.setText(parentName);
        }
    }

    public void changeColor() {
        // TODO: implement change color
    }

    public void changeIcon() {
        // TODO: implement change icon
    }

    private void save() {
        // TODO: implement save method
        NavUtils.navigateUpFromSameTask(getActivity());
    }
}
