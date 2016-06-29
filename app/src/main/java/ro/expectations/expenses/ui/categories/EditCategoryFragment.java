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

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.ColorHelper;
import ro.expectations.expenses.helper.DrawableHelper;
import ro.expectations.expenses.model.Category;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.widget.dialog.CategoryPickerDialogFragment;
import ro.expectations.expenses.widget.dialog.ColorPickerDialogFragment;
import ro.expectations.expenses.widget.dialog.ConfirmationDialogFragment;

public class EditCategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        CategoryPickerDialogFragment.Listener,
        ColorPickerDialogFragment.Listener,
        ConfirmationDialogFragment.Listener {

    public interface Listener {
        void onBackPressedConfirmed();
        void onNavigateUpConfirmed();
        void onColorSelected(@ColorInt int color, @ColorInt int darkColor, @ColorInt int accentColor);
        void showChangeColor();
        void hideChangeColor();
    }

    private static final int CATEGORY_PICKER_DIALOG_REQUEST_CODE = 0x100;
    private static final int ON_NAVIGATE_UP_DIALOG_REQUEST_CODE = 0x101;
    private static final int ON_BACK_PRESSED_DIALOG_REQUEST_CODE = 0x102;
    private static final int COLOR_PICKER_DIALOG_REQUEST_CODE = 0x103;

    private static final String ARG_CATEGORY_ID = "category_id";

    private static final String INSTANCE_ORIGINAL_CATEGORY = "original_category";
    private static final String INSTANCE_CURRENT_CATEGORY = "current_category";

    private long mCategoryId;

    private TextInputEditText mCategoryName;
    private TextInputEditText mCategoryParent;

    private Category mOriginalCategory;
    private Category mCurrentCategory;

    private Listener mListener;

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
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (Listener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement EditCategoryFragment.Listener");
        }
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
        mCategoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    mCurrentCategory.setName(mCategoryName.getText().toString());
                }
            }
        });

        mCategoryParent = (TextInputEditText) view.findViewById(R.id.category_parent);
        mCategoryParent.setInputType(InputType.TYPE_NULL);
        mCategoryParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryPickerDialogFragment categoryPickerDialogFragment = CategoryPickerDialogFragment.newInstance(mCategoryId);
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
        } else {
            mOriginalCategory = savedInstanceState.getParcelable(INSTANCE_ORIGINAL_CATEGORY);
            mCurrentCategory = savedInstanceState.getParcelable(INSTANCE_CURRENT_CATEGORY);
            renderCurrentColor();
            renderCurrentParentCategory();
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(INSTANCE_ORIGINAL_CATEGORY, mOriginalCategory);
        outState.putParcelable(INSTANCE_CURRENT_CATEGORY, mCurrentCategory);
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

            long parentId = data.getLong(Category.COLUMN_CATEGORY_PARENT_ID);
            String categoryName = data.getString(Category.COLUMN_CATEGORY_NAME);
            String categoryColor = data.getString(Category.COLUMN_CATEGORY_COLOR);
            String parentName = data.getString(Category.COLUMN_CATEGORY_PARENT_NAME);
            int children = data.getInt(Category.COLUMN_CATEGORY_CHILDREN);

            mOriginalCategory = new Category(mCategoryId,
                    categoryName,
                    ColorHelper.fromRGB(categoryColor, ContextCompat.getColor(getActivity(), R.color.colorAmber500)),
                    parentId,
                    children);
            mCurrentCategory = new Category(mOriginalCategory);

            mCategoryName.setText(categoryName);
            if (parentId > 0) {
                mCategoryParent.setText(parentName);
            } else {
                if (children > 0) {
                    mCategoryParent.setVisibility(View.GONE);
                } else {
                    mCategoryParent.setText(R.string.none);
                }
            }

            renderCurrentColor();
            renderCurrentParentCategory();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do
    }

    @Override
    public void onCategorySelected(int targetRequestCode, int parentId, String parentName, @ColorInt int color) {
        if (parentId == 0) {
            mCategoryParent.setText(R.string.none);
            if (mOriginalCategory.getParentId() > 0) {
                mCurrentCategory.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            } else {
                mCurrentCategory.setColor(mOriginalCategory.getColor());
            }
        } else {
            mCategoryParent.setText(parentName);
            mCurrentCategory.setColor(color);
        }
        mCurrentCategory.setParentId(parentId);
        renderCurrentColor();
        renderCurrentParentCategory();
    }

    @Override
    public void onColorSelected(int targetRequestCode, @ColorInt int color, @ColorInt int darkColor, @ColorInt int accentColor) {
        mCurrentCategory.setColor(color);
        mListener.onColorSelected(0xFF000000 | color, 0xFF000000 | darkColor, 0xFF000000 | accentColor);
    }

    @Override
    public void onConfirmed(int targetRequestCode) {
        switch(targetRequestCode) {
            case ON_NAVIGATE_UP_DIALOG_REQUEST_CODE:
                mListener.onNavigateUpConfirmed();
                break;
            case ON_BACK_PRESSED_DIALOG_REQUEST_CODE:
                mListener.onBackPressedConfirmed();
                break;
        }
    }

    @Override
    public void onDenied(int targetRequestCode) {
        // nothing to do
    }

    public void changeColor() {
        ColorPickerDialogFragment colorPickerDialogFragment = ColorPickerDialogFragment.newInstance(mCurrentCategory.getColor());
        colorPickerDialogFragment.setTargetFragment(this, COLOR_PICKER_DIALOG_REQUEST_CODE);
        colorPickerDialogFragment.show(getFragmentManager(), "ColorPickerDialogFragment");
    }

    public void changeIcon() {
        // TODO: implement change icon
    }

    public boolean confirmNavigateUp() {
        return confirmDiscard(ON_NAVIGATE_UP_DIALOG_REQUEST_CODE);
    }

    public boolean confirmBackPressed() {
        return confirmDiscard(ON_BACK_PRESSED_DIALOG_REQUEST_CODE);
    }

    private void renderCurrentColor() {
        int[] colors = getResources().getIntArray(R.array.colorPicker);
        for(int i = 0, n = colors.length; i < n; i++) {
            if (colors[i] == mCurrentCategory.getColor()) {
                int[] darkColors = getResources().getIntArray(R.array.colorPickerDark);
                int[] accentColors = getResources().getIntArray(R.array.colorPickerAccent);
                onColorSelected(-1, colors[i], darkColors[i], accentColors[i]);
                break;
            }
        }
    }

    private void renderCurrentParentCategory() {
        if (mCurrentCategory.getParentId() > 0) {
            mListener.hideChangeColor();
        } else {
            mListener.showChangeColor();
        }
    }

    private boolean confirmDiscard(int requestCode) {
        if (!isDirty()) {
            return false;
        }

        FragmentActivity activity = getActivity();
        if (activity != null) {
            ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(
                    null,
                    getString(R.string.confirm_discard_changes),
                    getString(R.string.button_discard),
                    getString(R.string.button_keep_editing),
                    true);
            confirmationDialogFragment.setTargetFragment(EditCategoryFragment.this, requestCode);
            confirmationDialogFragment.show(activity.getSupportFragmentManager(), "NavigateUpConfirmationDialogFragment");
            return true;
        }

        return false;
    }

    private boolean isDirty() {
        return mOriginalCategory == null || mCurrentCategory == null || !mOriginalCategory.equals(mCurrentCategory);
    }

    private void save() {
        if (isDirty()) {
            SaveQueryHandler saveQueryHandler = new SaveQueryHandler(
                    getActivity().getContentResolver(),
                    new SaveQueryHandler.AsyncQueryListener() {
                        @Override
                        public void onQueryComplete(int token, Object cookie, int result) {
                            mListener.onNavigateUpConfirmed();
                        }
                    });

            // update the child categories color based on parent's color
            if (mCurrentCategory.getParentId() == 0) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(ExpensesContract.Categories.COLOR, ColorHelper.toRGB(mCurrentCategory.getColor()));
                String selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(mCurrentCategory.getId())};
                saveQueryHandler.startUpdate(1, null,
                        ExpensesContract.Categories.CONTENT_URI,
                        updateValues,
                        selection,
                        selectionArgs);
            }

            // update the category details
            saveQueryHandler.startUpdate(2, null,
                    ContentUris.withAppendedId(ExpensesContract.Categories.CONTENT_URI, mCategoryId),
                    mCurrentCategory.toContentValues(),
                    null,
                    null);

        } else {
            mListener.onNavigateUpConfirmed();
        }
    }

    private static class SaveQueryHandler extends AsyncQueryHandler {

        public interface AsyncQueryListener {
            void onQueryComplete(int token, Object cookie, int result);
        }

        private WeakReference<AsyncQueryListener> mListener;

        public SaveQueryHandler(ContentResolver cr, AsyncQueryListener listener) {
            super(cr);
            setQueryListener(listener);
        }

        public SaveQueryHandler(ContentResolver cr) {
            super(cr);
        }

        public void setQueryListener(AsyncQueryListener listener) {
            mListener = new WeakReference<>(listener);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            final AsyncQueryListener listener = mListener.get();
            if (listener != null && token == 2) {
                listener.onQueryComplete(token, cookie, result);
            }
        }
    }
}
