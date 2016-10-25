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

package ro.expectations.expenses.ui.accounts;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.dialog.ConfirmationDialogFragment;
import ro.expectations.expenses.utils.DrawableUtils;

public class ManageAccountFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ConfirmationDialogFragment.Listener {

    public interface Listener {
        void onBackPressedConfirmed();
        void onNavigateUpConfirmed();
    }

    private static final int ON_NAVIGATE_UP_DIALOG_REQUEST_CODE = 0x101;
    private static final int ON_BACK_PRESSED_DIALOG_REQUEST_CODE = 0x102;

    private static final int LOADER_ACCOUNT = 1;

    private static final String ARG_ACCOUNT_ID = "account_id";

    private static final String INSTANCE_ORIGINAL_ACCOUNT = "original_account";
    private static final String INSTANCE_CURRENT_ACCOUNT = "current_account";

    private TextInputLayout mAccountTitleLayout;
    private TextInputEditText mAccountTitle;

    private Account mOriginalAccount;
    private Account mCurrentAccount;

    private Listener mListener;

    public ManageAccountFragment() {
        // Required empty public constructor
    }

    public static ManageAccountFragment newInstance(long accountId) {
        ManageAccountFragment fragment = new ManageAccountFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
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
                    + " must implement ManageAccountFragment.Listener");
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

        return inflater.inflate(R.layout.fragment_manage_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAccountTitleLayout = (TextInputLayout) view.findViewById(R.id.account_name_input_layout);
        mAccountTitle = (TextInputEditText) view.findViewById(R.id.account_name);
        mAccountTitle.addTextChangedListener(new TextWatcher() {
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
                String accountName = mAccountTitle.getText().toString();
                if (mCurrentAccount != null) {
                    mCurrentAccount.setTitle(accountName);
                }
                if (!accountName.isEmpty()) {
                    mAccountTitleLayout.setErrorEnabled(false);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            long accountId = getArguments() != null ? getArguments().getLong(ARG_ACCOUNT_ID) : 0L;
            if (accountId > 0) {
                Bundle args = new Bundle();
                args.putLong(ARG_ACCOUNT_ID, accountId);
                getLoaderManager().restartLoader(LOADER_ACCOUNT, args, this);
            } else {
                mOriginalAccount = new Account(0, "", "EUR", AccountType.CASH, null, null, true, true, 0, "", 0);
                mCurrentAccount = new Account(mOriginalAccount);
            }
        } else {
            mOriginalAccount = savedInstanceState.getParcelable(INSTANCE_ORIGINAL_ACCOUNT);
            mCurrentAccount = savedInstanceState.getParcelable(INSTANCE_CURRENT_ACCOUNT);
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
        outState.putParcelable(INSTANCE_ORIGINAL_ACCOUNT, mOriginalAccount);
        outState.putParcelable(INSTANCE_CURRENT_ACCOUNT, mCurrentAccount);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
            getActivity(),
            ContentUris.withAppendedId(ExpensesContract.Accounts.CONTENT_URI, args.getLong(ARG_ACCOUNT_ID)),
            Account.PROJECTION,
            null,
            null,
            null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.getCount() > 0) {
            data.moveToFirst();

            mOriginalAccount = new Account(data);
            mCurrentAccount = new Account(mOriginalAccount);

            mAccountTitle.setText(mOriginalAccount.getTitle());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do
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

    public boolean confirmNavigateUp() {
        return confirmDiscard(ON_NAVIGATE_UP_DIALOG_REQUEST_CODE);
    }

    public boolean confirmBackPressed() {
        return confirmDiscard(ON_BACK_PRESSED_DIALOG_REQUEST_CODE);
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
            confirmationDialogFragment.setTargetFragment(ManageAccountFragment.this, requestCode);
            confirmationDialogFragment.show(activity.getSupportFragmentManager(), "NavigateUpConfirmationDialogFragment");
            return true;
        }

        return false;
    }

    private boolean isDirty() {
        return mOriginalAccount == null || mCurrentAccount == null || !mOriginalAccount.equals(mCurrentAccount);
    }

    private boolean validate() {
        return validateAccountName();
    }

    private boolean validateAccountName() {
        if (mCurrentAccount.getTitle().isEmpty()) {
            mAccountTitleLayout.setErrorEnabled(true);
            mAccountTitleLayout.setError(getString(R.string.error_account_name_empty));
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

            if (mCurrentAccount.getId() > 0) {

                // update the account details
                saveQueryHandler.startUpdate(100, null,
                        ContentUris.withAppendedId(ExpensesContract.Accounts.CONTENT_URI, mCurrentAccount.getId()),
                        mCurrentAccount.toContentValues(),
                        null,
                        null);
            } else {

                // insert a new account
                saveQueryHandler.startInsert(100, null,
                        ExpensesContract.Accounts.CONTENT_URI,
                        mCurrentAccount.toContentValues());

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
            if (listener != null) {
                listener.onQueryComplete(token);
            }
        }
    }
}
