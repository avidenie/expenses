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

import android.database.Cursor;
import android.os.Bundle;
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
import ro.expectations.expenses.provider.ExpensesContract.Categories;
import ro.expectations.expenses.provider.ExpensesContract.FromAccounts;
import ro.expectations.expenses.provider.ExpensesContract.FromRunningBalances;
import ro.expectations.expenses.provider.ExpensesContract.ParentCategories;
import ro.expectations.expenses.provider.ExpensesContract.Payees;
import ro.expectations.expenses.provider.ExpensesContract.ToAccounts;
import ro.expectations.expenses.provider.ExpensesContract.ToRunningBalances;
import ro.expectations.expenses.provider.ExpensesContract.TransactionDetails;
import ro.expectations.expenses.provider.ExpensesContract.Transactions;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.utils.DrawableUtils;

public class TransactionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String ARG_ACCOUNT_ID = "account_id";
    protected static final String ARG_HANDLE_CLICKS = "handle_clicks";

    private static final String[] PROJECTION = {
            Transactions._ID,
            Transactions.FROM_ACCOUNT_ID,
            FromAccounts.FROM_TITLE,
            TransactionDetails.FROM_AMOUNT,
            FromAccounts.FROM_CURRENCY,
            FromRunningBalances.FROM_BALANCE,
            Transactions.TO_ACCOUNT_ID,
            ToAccounts.TO_TITLE,
            TransactionDetails.TO_AMOUNT,
            ToAccounts.TO_CURRENCY,
            ToRunningBalances.TO_BALANCE,
            Categories.CATEGORY_ID,
            Categories.CATEGORY_NAME,
            Categories.COLOR,
            Categories.ICON,
            Categories.PARENT_ID,
            ParentCategories.PARENT_NAME,
            Transactions.PAYEE_ID,
            Payees.PAYEE_NAME,
            Transactions.NOTE,
            TransactionDetails.IS_SPLIT,
            Transactions.OCCURRED_AT,
            Transactions.CREATED_AT,
            Transactions.ORIGINAL_AMOUNT,
            Transactions.ORIGINAL_CURRENCY
    };
    static final int COLUMN_TRANSACTION_ID = 0;
    static final int COLUMN_FROM_ACCOUNT_ID = 1;
    static final int COLUMN_FROM_ACCOUNT_TITLE = 2;
    static final int COLUMN_FROM_AMOUNT = 3;
    static final int COLUMN_FROM_CURRENCY = 4;
    static final int COLUMN_FROM_BALANCE = 5;
    static final int COLUMN_TO_ACCOUNT_ID = 6;
    static final int COLUMN_TO_ACCOUNT_TITLE = 7;
    static final int COLUMN_TO_AMOUNT = 8;
    static final int COLUMN_TO_CURRENCY = 9;
    static final int COLUMN_TO_BALANCE = 10;
    static final int COLUMN_CATEGORY_ID = 11;
    static final int COLUMN_CATEGORY_NAME = 12;
    static final int COLUMN_CATEGORY_COLOR = 13;
    static final int COLUMN_CATEGORY_ICON = 14;
    static final int COLUMN_PARENT_CATEGORY_ID = 15;
    static final int COLUMN_PARENT_CATEGORY_NAME = 16;
    static final int COLUMN_PAYEE_ID = 17;
    static final int COLUMN_PAYEE_NAME = 18;
    static final int COLUMN_NOTE = 19;
    static final int COLUMN_IS_SPLIT = 20;
    static final int COLUMN_OCCURED_AT = 21;
    static final int COLUMN_CREATED_AT = 22;
    static final int COLUMN_ORIGINAL_AMOUNT = 23;
    static final int COLUMN_ORIGINAL_CURRENCY = 24;

    private long mSelectedAccountId;
    private boolean mHandleClicks = false;
    private TransactionsAdapter mAdapter;
    private TextView mEmptyView;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_transactions, menu);
            ((DrawerActivity) getActivity()).lockNavigationDrawer();
            MenuItem actionEditTransaction = menu.findItem(R.id.action_edit_transaction);
            actionEditTransaction.setIcon(DrawableUtils.tint(getContext(), actionEditTransaction.getIcon(), R.color.colorWhite));
            MenuItem actionDeleteTransaction = menu.findItem(R.id.action_delete_transaction);
            actionDeleteTransaction.setIcon(DrawableUtils.tint(getContext(), actionDeleteTransaction.getIcon(), R.color.colorWhite));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedTransactions = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_transactions,
                    selectedTransactions,
                    selectedTransactions
            ));
            if (selectedTransactions == 1) {
                menu.findItem(R.id.action_edit_transaction).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_transaction).setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_edit_transaction:
                    mode.finish();
                    return true;
                case R.id.action_delete_transaction:
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
            ((DrawerActivity) getActivity()).unlockNavigationDrawer();
        }
    };

    public static TransactionsFragment newInstance(long accountId, boolean handleClicks) {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        args.putBoolean(ARG_HANDLE_CLICKS, handleClicks);
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedAccountId = getArguments().getLong(ARG_ACCOUNT_ID);
            mHandleClicks = getArguments().getBoolean(ARG_HANDLE_CLICKS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        mEmptyView = (TextView) rootView.findViewById(R.id.list_transactions_empty);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);

        mAdapter = new TransactionsAdapter(getActivity(), mSelectedAccountId);
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        if (mHandleClicks) {
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
        } else {
            itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View view, int position) {
                    // nothing to do, just the ripple effect
                }
            });
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mHandleClicks && savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.isChoiceMode() && mActionMode == null) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        getLoaderManager().restartLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mHandleClicks) {
            mAdapter.onSaveInstanceState(outState);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = Transactions.TABLE_NAME + "." + Transactions.CREATED_AT + " DESC";
        String selection = TransactionDetails.TABLE_NAME + "." + TransactionDetails.IS_SPLIT + " = 0";
        String[] selectionArgs = null;
        if (mSelectedAccountId > 0) {
            selection += " AND (" + Transactions.TABLE_NAME + "." + Transactions.FROM_ACCOUNT_ID
                    + " = ? OR " + Transactions.TABLE_NAME + "." + Transactions.TO_ACCOUNT_ID
                    + " = ?)";
            selectionArgs = new String[] {String.valueOf(mSelectedAccountId), String.valueOf(mSelectedAccountId)};
        }

        return new CursorLoader(
                getActivity(),
                Transactions.CONTENT_URI,
                PROJECTION,
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
