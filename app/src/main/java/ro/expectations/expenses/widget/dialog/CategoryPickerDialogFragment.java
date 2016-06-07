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

package ro.expectations.expenses.widget.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.widget.recyclerview.ItemClickHelper;

public class CategoryPickerDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface Listener {
        void onCategorySelected(int targetRequestCode, int categoryId, String categoryName);
    }

    private Listener callback;
    private CategoryPickerAdapter mAdapter;

    public static CategoryPickerDialogFragment newInstance() {
        CategoryPickerDialogFragment categoryPickerDialogFragment = new CategoryPickerDialogFragment();
        categoryPickerDialogFragment.setCancelable(true);
        return categoryPickerDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment target = getTargetFragment();
        if (target instanceof Listener) {
            callback = (Listener) getTargetFragment();
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
                callback.onCategorySelected(getTargetRequestCode(), categoryId, categoryName);
                dismiss();
            }
        });

        builder.setTitle(getString(R.string.choose_category));
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories._ID  + " > 0 AND "
                + ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID  + " IS NULL";

        String sortOrder = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Categories.CONTENT_URI,
                CategoryPickerAdapter.PROJECTION,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                ExpensesContract.Accounts._ID,
                ExpensesContract.Accounts.TITLE
        });
        matrixCursor.addRow(new Object[] { "0", getString(R.string.no_category) });
        MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, data });

        mAdapter.swapCursor(mergeCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
