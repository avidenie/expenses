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

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.Map;

import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Category;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.dialog.CategoryPickerDialogFragment;
import ro.expectations.expenses.ui.dialog.ColorPickerDialogFragment;
import ro.expectations.expenses.ui.dialog.ConfirmationDialogFragment;
import ro.expectations.expenses.ui.dialog.IconPickerDialogFragment;
import ro.expectations.expenses.utils.ColorStyleUtils;
import ro.expectations.expenses.utils.ColorUtils;
import ro.expectations.expenses.utils.DrawableUtils;

public class ManageCategoryFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        CategoryPickerDialogFragment.Listener,
        ColorPickerDialogFragment.Listener,
        IconPickerDialogFragment.Listener,
        ConfirmationDialogFragment.Listener {

    public interface Listener {
        void onBackPressedConfirmed();
        void onNavigateUpConfirmed();
        void onColorStyleSelected(@StyleRes int style);
        void onIconSelected(@DrawableRes int icon);
        void showChangeColor();
        void hideChangeColor();
    }

    private static final int CATEGORY_PICKER_DIALOG_REQUEST_CODE = 0x100;
    private static final int ON_NAVIGATE_UP_DIALOG_REQUEST_CODE = 0x101;
    private static final int ON_BACK_PRESSED_DIALOG_REQUEST_CODE = 0x102;
    private static final int COLOR_PICKER_DIALOG_REQUEST_CODE = 0x103;
    private static final int ICON_PICKER_DIALOG_REQUEST_CODE = 0x104;

    private static final int LOADER_CATEGORY = 1;
    private static final int LOADER_PARENT_CATEGORY = 2;

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_PARENT_CATEGORY_ID = "parent_category_id";

    private static final String INSTANCE_ORIGINAL_CATEGORY = "original_category";
    private static final String INSTANCE_CURRENT_CATEGORY = "current_category";

    private TextInputLayout mCategoryNameLayout;
    private TextInputEditText mCategoryName;
    private TextInputEditText mCategoryParent;

    private Category mOriginalCategory;
    private Category mCurrentCategory;

    private Listener mListener;

    public ManageCategoryFragment() {
        // Required empty public constructor
    }

    public static ManageCategoryFragment newInstance(long categoryId) {
        ManageCategoryFragment fragment = new ManageCategoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ManageCategoryFragment newInstanceWithParent(long parentCategoryId) {
        ManageCategoryFragment fragment = new ManageCategoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARENT_CATEGORY_ID, parentCategoryId);
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
                    + " must implement ManageCategoryFragment.Listener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_manage_category, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCategoryNameLayout = (TextInputLayout) view.findViewById(R.id.category_name_input_layout);
        mCategoryName = (TextInputEditText) view.findViewById(R.id.category_name);
        mCategoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // don't care
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // don't care
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String categoryName = mCategoryName.getText().toString();
                if (mCurrentCategory != null) {
                    mCurrentCategory.setName(categoryName);
                }
                if (!categoryName.isEmpty()) {
                    mCategoryNameLayout.setErrorEnabled(false);
                }
            }
        });

        mCategoryParent = (TextInputEditText) view.findViewById(R.id.category_parent);
        mCategoryParent.setInputType(InputType.TYPE_NULL);
        mCategoryParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long skipCategoryId = mCurrentCategory != null ? mCurrentCategory.getId() : 0;
                CategoryPickerDialogFragment categoryPickerDialogFragment = CategoryPickerDialogFragment.newInstance(skipCategoryId);
                categoryPickerDialogFragment.setTargetFragment(ManageCategoryFragment.this, CATEGORY_PICKER_DIALOG_REQUEST_CODE);
                categoryPickerDialogFragment.show(getFragmentManager(), "category_picker");
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            long categoryId = getArguments() != null ? getArguments().getLong(ARG_CATEGORY_ID) : 0L;
            if (categoryId > 0) {
                Bundle args = new Bundle();
                args.putLong(ARG_CATEGORY_ID, categoryId);
                getLoaderManager().restartLoader(LOADER_CATEGORY, args, this);
            } else {
                long parentId = getArguments() != null ? getArguments().getLong(ARG_PARENT_CATEGORY_ID) : 0L;
                if (parentId > 0) {
                    Bundle args = new Bundle();
                    args.putLong(ARG_CATEGORY_ID, parentId);
                    getLoaderManager().restartLoader(LOADER_PARENT_CATEGORY, args, this);
                } else {
                    mOriginalCategory = new Category(
                            0,
                            "",
                            ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                            "ColorIndigo",
                            "ic_question_mark_black_24dp",
                            0,
                            0);
                    mCurrentCategory = new Category(mOriginalCategory);
                    mCategoryParent.setText(R.string.none);
                    renderCurrentCategoryDetails();
                }
            }
        } else {
            mOriginalCategory = savedInstanceState.getParcelable(INSTANCE_ORIGINAL_CATEGORY);
            mCurrentCategory = savedInstanceState.getParcelable(INSTANCE_CURRENT_CATEGORY);
            renderCurrentColor();
            renderCurrentIcon();
            renderCurrentParentCategory();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(Menu.NONE, R.id.action_save, Menu.NONE, R.string.action_save);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(DrawableUtils.tint(getActivity(), R.drawable.ic_done_black_24dp, R.color.colorWhite));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (validate()) {
                save();
            }
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
            ContentUris.withAppendedId(ExpensesContract.Categories.CONTENT_URI, args.getLong(ARG_CATEGORY_ID)),
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

            if (loader.getId() == LOADER_CATEGORY) {

                long categoryId = data.getLong(Category.COLUMN_CATEGORY_ID);
                long parentId = data.getLong(Category.COLUMN_CATEGORY_PARENT_ID);
                String categoryName = data.getString(Category.COLUMN_CATEGORY_NAME);
                String categoryColor = data.getString(Category.COLUMN_CATEGORY_COLOR);
                String categoryStyle = data.getString(Category.COLUMN_CATEGORY_STYLE);
                String categoryIcon = data.getString(Category.COLUMN_CATEGORY_ICON);
                String parentName = data.getString(Category.COLUMN_CATEGORY_PARENT_NAME);
                int children = data.getInt(Category.COLUMN_CATEGORY_CHILDREN);

                mOriginalCategory = new Category(
                        categoryId,
                        categoryName,
                        ColorUtils.fromRGB(categoryColor, ContextCompat.getColor(getActivity(), R.color.colorPrimary)),
                        categoryStyle,
                        categoryIcon,
                        parentId,
                        children);

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

            } else {

                long parentId = data.getLong(Category.COLUMN_CATEGORY_ID);
                String parentName = data.getString(Category.COLUMN_CATEGORY_NAME);
                String parentColor = data.getString(Category.COLUMN_CATEGORY_COLOR);
                String parentStyle = data.getString(Category.COLUMN_CATEGORY_STYLE);
                String parentIcon = data.getString(Category.COLUMN_CATEGORY_ICON);

                mOriginalCategory = new Category(
                        0,
                        "",
                        ColorUtils.fromRGB(parentColor, ContextCompat.getColor(getActivity(), R.color.colorPrimary)),
                        parentStyle,
                        parentIcon,
                        parentId,
                        0);

                mCategoryParent.setText(parentName);
            }

            mCurrentCategory = new Category(mOriginalCategory);

            renderCurrentCategoryDetails();
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
    public void onColorSelected(int targetRequestCode, @ColorInt int color, @StyleRes int style) {
        mCurrentCategory.setColor(color);
        mCurrentCategory.setStyle(getResources().getResourceEntryName(style));
        mListener.onColorStyleSelected(style);
    }

    @Override
    public void onIconSelected(int targetRequestCode, String icon) {
        mCurrentCategory.setIcon(icon);
        mListener.onIconSelected(DrawableUtils.getIdentifier(getActivity(), icon));
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
        ColorPickerDialogFragment colorPickerDialogFragment = ColorPickerDialogFragment.newInstance(mCurrentCategory.getStyle());
        colorPickerDialogFragment.setTargetFragment(this, COLOR_PICKER_DIALOG_REQUEST_CODE);
        colorPickerDialogFragment.show(getFragmentManager(), "ColorPickerDialogFragment");
    }

    public void changeIcon() {
        IconPickerDialogFragment iconPickerDialogFragment = IconPickerDialogFragment.newInstance();
        iconPickerDialogFragment.setTargetFragment(this, ICON_PICKER_DIALOG_REQUEST_CODE);
        iconPickerDialogFragment.show(getFragmentManager(), "IconPickerDialogFragment");
    }

    public boolean confirmNavigateUp() {
        return confirmDiscard(ON_NAVIGATE_UP_DIALOG_REQUEST_CODE);
    }

    public boolean confirmBackPressed() {
        return confirmDiscard(ON_BACK_PRESSED_DIALOG_REQUEST_CODE);
    }

    public Intent getParentActivityIntent() {
        if (mCurrentCategory == null) {
            return null;
        }
        long parentCategoryId = mCurrentCategory.getParentId();
        if (parentCategoryId > 0) {
            Intent intent = new Intent(getActivity(), SubcategoriesActivity.class);
            intent.putExtra(SubcategoriesActivity.ARG_PARENT_CATEGORY_ID, parentCategoryId);
            intent.putExtra(SubcategoriesActivity.ARG_PARENT_CATEGORY_STYLE, mCurrentCategory.getStyle());
            return intent;
        } else {
            return new Intent(getActivity(), CategoriesActivity.class);
        }
    }

    private void renderCurrentColor() {
        String[] styles = getResources().getStringArray(R.array.colorPickerStyles);
        for (String style : styles) {
            if (style.equals(mCurrentCategory.getStyle())) {
                Map<String, Integer> colors = ColorStyleUtils.getColorsFromStyle(getActivity(), style);
                onColorSelected(-1, colors.get(ColorStyleUtils.COLOR_PRIMARY), ColorStyleUtils.getIdentifier(getActivity(), style));
                break;
            }
        }
    }

    private void renderCurrentIcon() {
        onIconSelected(-1, mCurrentCategory.getIcon());
    }

    private void renderCurrentParentCategory() {
        if (mCurrentCategory.getParentId() > 0) {
            mListener.hideChangeColor();
        } else {
            mListener.showChangeColor();
        }
    }

    private void renderCurrentCategoryDetails() {
        renderCurrentColor();
        renderCurrentIcon();
        renderCurrentParentCategory();
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
            confirmationDialogFragment.setTargetFragment(ManageCategoryFragment.this, requestCode);
            confirmationDialogFragment.show(activity.getSupportFragmentManager(), "NavigateUpConfirmationDialogFragment");
            return true;
        }

        return false;
    }

    private boolean isDirty() {
        return mOriginalCategory == null || mCurrentCategory == null || !mOriginalCategory.equals(mCurrentCategory);
    }

    private boolean validate() {
        return validateCategoryName();
    }

    private boolean validateCategoryName() {
        if (mCurrentCategory.getName().isEmpty()) {
            mCategoryNameLayout.setErrorEnabled(true);
            mCategoryNameLayout.setError(getString(R.string.error_category_name_empty));
            return false;
        }
        return true;
    }

    private void save() {

        if (isDirty()) {
            SaveQueryHandler saveQueryHandler = new SaveQueryHandler(
                    getActivity().getContentResolver(),
                    new SaveQueryHandler.SaveQueryListener() {
                        @Override
                        public void onQueryComplete(int token) {
                            mListener.onNavigateUpConfirmed();
                        }
                    });

            if (mCurrentCategory.getId() > 0) {

                // update the child categories color based on parent's color
                if (mCurrentCategory.getParentId() == 0) {
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(ExpensesContract.Categories.COLOR, ColorUtils.toRGB(mCurrentCategory.getColor()));
                    String selection = ExpensesContract.Categories.TABLE_NAME + "." + ExpensesContract.Categories.PARENT_ID + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(mCurrentCategory.getId())};
                    saveQueryHandler.startUpdate(0, null,
                            ExpensesContract.Categories.CONTENT_URI,
                            updateValues,
                            selection,
                            selectionArgs);
                }

                // update the category details
                saveQueryHandler.startUpdate(100, null,
                        ContentUris.withAppendedId(ExpensesContract.Categories.CONTENT_URI, mCurrentCategory.getId()),
                        mCurrentCategory.toContentValues(),
                        null,
                        null);
            } else {

                saveQueryHandler.startInsert(100, null,
                        ExpensesContract.Categories.CONTENT_URI,
                        mCurrentCategory.toContentValues());

            }

        } else {
            mListener.onNavigateUpConfirmed();
        }
    }

    private static class SaveQueryHandler extends AsyncQueryHandler {

        public interface SaveQueryListener {
            void onQueryComplete(int token);
        }

        private WeakReference<SaveQueryListener> mListener;

        public SaveQueryHandler(ContentResolver cr, SaveQueryListener listener) {
            super(cr);
            setQueryListener(listener);
        }

        public void setQueryListener(SaveQueryListener listener) {
            mListener = new WeakReference<>(listener);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            final SaveQueryListener listener = mListener.get();
            if (listener != null) {
                listener.onQueryComplete(token);
            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            final SaveQueryListener listener = mListener.get();
            if (listener != null && token == 100) {
                listener.onQueryComplete(token);
            }
        }
    }
}
