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

package ro.expectations.expenses.ui.transactions;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import ro.expectations.expenses.R;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class TransactionsActivity extends DrawerActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ACCOUNT_ID = "account_id";

    private Spinner mAccountsSpinner;
    private long mSelectedAccountId;

    protected void setMainContentView() {
        setContentView(R.layout.activity_drawer_spinner);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            mSelectedAccountId = getIntent().getLongExtra(ARG_ACCOUNT_ID, 0);
        } else {
            mSelectedAccountId = savedInstanceState.getLong(ARG_ACCOUNT_ID, 0);
        }

        mMainContent.setLayoutResource(R.layout.content_transactions);
        mMainContent.inflate();

        // Setup spinner.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        AccountsSpinnerAdapter adapter = new AccountsSpinnerAdapter(
                toolbar.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                null,
                new String[] { ExpensesContract.Accounts.TITLE },
                new int[] { android.R.id.text1 },
                0);
        mAccountsSpinner = (Spinner) findViewById(R.id.spinner);
        mAccountsSpinner.setAdapter(adapter);

        mAccountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id != mSelectedAccountId) {
                    mSelectedAccountId = id;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, TransactionsFragment.newInstance(mSelectedAccountId, true))
                            .commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getSupportLoaderManager().restartLoader(0, null, this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, TransactionsFragment.newInstance(mSelectedAccountId, true))
                    .commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_transactions;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_ACCOUNT_ID, mSelectedAccountId);
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ExpensesContract.Accounts._ID,
                ExpensesContract.Accounts.TITLE
        };
        String sortOrder = ExpensesContract.Accounts.SORT_ORDER + " ASC";
        String selection = ExpensesContract.Accounts.IS_ACTIVE + " = 1";

        return new CursorLoader(
                this,
                ExpensesContract.Accounts.CONTENT_URI,
                projection,
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
        matrixCursor.addRow(new Object[] { "0", getString(R.string.all_accounts) });
        MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, data });

        ((AccountsSpinnerAdapter) mAccountsSpinner.getAdapter()).swapCursor(mergeCursor);
        setSpinnerItemById(mSelectedAccountId);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((AccountsSpinnerAdapter) mAccountsSpinner.getAdapter()).swapCursor(null);
    }

    public void setSpinnerItemById(long selectedId) {
        int spinnerCount = mAccountsSpinner.getCount();
        for (int i = 0; i < spinnerCount; i++) {
            Cursor value = (Cursor) mAccountsSpinner.getItemAtPosition(i);
            long id = value.getLong(value.getColumnIndex(ExpensesContract.Accounts._ID));
            if (id == selectedId) {
                mAccountsSpinner.setSelection(i);
                break;
            }
        }
    }

    private static class AccountsSpinnerAdapter extends SimpleCursorAdapter implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public AccountsSpinnerAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
            return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }
    }
}
