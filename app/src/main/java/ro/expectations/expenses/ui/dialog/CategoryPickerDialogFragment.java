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

package ro.expectations.expenses.ui.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.utils.ColorUtils;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;

public class CategoryPickerDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface Listener {
        void onCategorySelected(int targetRequestCode, int categoryId, String categoryName, @ColorInt int color);
    }

    public static final String ARG_SKIP_CATEGORY_ID = "skip_category_id";

    private Listener mCallback;
    private CategoryPickerAdapter mAdapter;

    public static CategoryPickerDialogFragment newInstance() {
        CategoryPickerDialogFragment categoryPickerDialogFragment = new CategoryPickerDialogFragment();
        categoryPickerDialogFragment.setCancelable(true);
        return categoryPickerDialogFragment;
    }

    public static CategoryPickerDialogFragment newInstance(long skipCategoryId) {
        Bundle args = new Bundle();
        args.putLong(ARG_SKIP_CATEGORY_ID, skipCategoryId);

        CategoryPickerDialogFragment categoryPickerDialogFragment = new CategoryPickerDialogFragment();
        categoryPickerDialogFragment.setArguments(args);
        categoryPickerDialogFragment.setCancelable(true);
        return categoryPickerDialogFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment target = getTargetFragment();
        if (target instanceof Listener) {
            mCallback = (Listener) getTargetFragment();
        } else {
            throw new RuntimeException(getTargetFragment().toString()
                    + " must implement CategoryPickerDialogFragment.Listener");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_recyclerview, null);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        mAdapter = new CategoryPickerAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(position);
                int categoryId = cursor.getInt(CategoryPickerAdapter.COLUMN_CATEGORY_ID);
                String categoryName = cursor.getString(CategoryPickerAdapter.COLUMN_CATEGORY_NAME);
                int color = ColorUtils.fromRGB(cursor.getString(CategoryPickerAdapter.COLUMN_CATEGORY_COLOR), ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                mCallback.onCategorySelected(getTargetRequestCode(), categoryId, categoryName, color);
                dismiss();
            }
        });

        builder.setTitle(getString(R.string.choose_category));
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories._ID  + " > 0 AND "
                + ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID  + " IS NULL";
        String[] selectionArgs = null;

        if (args != null) {
            long skipCategoryId = args.getLong(ARG_SKIP_CATEGORY_ID, -1);
            if (skipCategoryId > 0) {
                selection += " AND " + ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories._ID + " != ?";
                selectionArgs = new String[]{String.valueOf(skipCategoryId)};
            }
        }

        String sortOrder = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Categories.CONTENT_URI,
                CategoryPickerAdapter.PROJECTION,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                ExpensesContract.Categories._ID,
                ExpensesContract.Categories.NAME,
                ExpensesContract.Categories.COLOR
        });
        matrixCursor.addRow(new Object[] { "0", getString(R.string.no_category), ContextCompat.getColor(getActivity(), R.color.colorPrimary)});
        MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, data });

        mAdapter.swapCursor(mergeCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
