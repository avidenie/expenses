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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
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
import ro.expectations.expenses.utils.DrawableUtils;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.providers.AppBarHelperProvider;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.helper.AppBarHelper;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;
import ro.expectations.expenses.ui.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;

public class AccountsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private AccountsAdapter mAdapter;
    private TextView mEmptyView;

    private AppBarHelper.State mPreviousState;
    private AppBarHelperProvider mAppBarHelperProvider;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_accounts, menu);
            ((DrawerActivity) getActivity()).lockNavigationDrawer();
            MenuItem actionEditAccount = menu.findItem(R.id.action_edit_account);
            actionEditAccount.setIcon(DrawableUtils.tint(getContext(), actionEditAccount.getIcon(), R.color.colorWhite));
            MenuItem actionCloseAccount = menu.findItem(R.id.action_close_account);
            actionCloseAccount.setIcon(DrawableUtils.tint(getContext(), actionCloseAccount.getIcon(), R.color.colorWhite));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            int selectedAccounts = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_accounts,
                    selectedAccounts,
                    selectedAccounts
            ));
            if (mAdapter.getSelectedItemCount() == 1) {
                menu.findItem(R.id.action_edit_account).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_account).setVisible(false);
            }

            // lock app bar in collapsed state
            AppBarHelper appBarHelper = mAppBarHelperProvider.getAppBarHelper();
            if (mPreviousState == null) {
                mPreviousState = appBarHelper.getState();
            }
            appBarHelper.setExpanded(false, true);
            ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_edit_account:
                    Intent editAccountIntent = new Intent(getActivity(), EditAccountActivity.class);
                    startActivity(editAccountIntent);
                    mode.finish();
                    return true;
                case R.id.action_close_account:
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

            // unlock collapsed app bar
            AppBarHelper appBarHelper = mAppBarHelperProvider.getAppBarHelper();
            if (mPreviousState != null && mPreviousState == AppBarHelper.State.EXPANDED) {
                appBarHelper.setExpanded(true, true);
            }
            mPreviousState = null;
            ViewCompat.setNestedScrollingEnabled(mRecyclerView, true);
        }
    };

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mAppBarHelperProvider = (AppBarHelperProvider) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AppBarHelperProvider");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accounts, container, false);

        mEmptyView = (TextView) rootView.findViewById(R.id.list_accounts_empty);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_accounts);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new AccountsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(mRecyclerView);
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
                } else {
                    long id = parent.getAdapter().getItemId(position);
                    Intent transactionsListingIntent = new Intent(getActivity(), TransactionsActivity.class);
                    transactionsListingIntent.putExtra(TransactionsActivity.ARG_ACCOUNT_ID, id);
                    startActivity(transactionsListingIntent);
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                mAdapter.setItemSelected(position, !mAdapter.isItemSelected(position));
                if (mAdapter.isChoiceMode()) {
                    if (mActionMode == null) {
                        mActionMode = ((AccountsActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.isChoiceMode() && mActionMode == null) {
                mActionMode = ((AccountsActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        getLoaderManager().restartLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ExpensesContract.Accounts._ID,
                ExpensesContract.Accounts.TITLE,
                ExpensesContract.Accounts.CURRENCY,
                ExpensesContract.Accounts.BALANCE,
                ExpensesContract.Accounts.LAST_TRANSACTION_AT,
                ExpensesContract.Accounts.CREATED_AT,
                ExpensesContract.Accounts.TYPE,
                ExpensesContract.Accounts.SUBTYPE
        };
        String sortOrder = ExpensesContract.Accounts.SORT_ORDER + " ASC";
        String selection = ExpensesContract.Accounts.IS_ACTIVE + " = 1";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Accounts.CONTENT_URI,
                projection,
                selection,
                null,
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
