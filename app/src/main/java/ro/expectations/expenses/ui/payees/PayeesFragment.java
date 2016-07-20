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

package ro.expectations.expenses.ui.payees;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import ro.expectations.expenses.helper.DrawableHelper;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.ui.helper.AppBarHelper;
import ro.expectations.expenses.ui.providers.AppBarHelperProvider;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.widget.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.widget.recyclerview.ItemClickHelper;

public class PayeesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private PayeesAdapter mAdapter;
    private TextView mEmptyView;

    private AppBarHelper.State mPreviousState;
    private AppBarHelperProvider mAppBarHelperProvider;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_payees, menu);
            ((DrawerActivity) getActivity()).lockNavigationDrawer();
            MenuItem actionEditPayee = menu.findItem(R.id.action_edit_payee);
            actionEditPayee.setIcon(DrawableHelper.tint(getContext(), actionEditPayee.getIcon(), R.color.colorWhite));
            MenuItem actionDeletePayee = menu.findItem(R.id.action_delete_payee);
            actionDeletePayee.setIcon(DrawableHelper.tint(getContext(), actionDeletePayee.getIcon(), R.color.colorWhite));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            int selectedPayees = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_payees,
                    selectedPayees,
                    selectedPayees
            ));
            if (mAdapter.getSelectedItemCount() == 1) {
                menu.findItem(R.id.action_edit_payee).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_payee).setVisible(false);
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
                case R.id.action_edit_payee:
                    mode.finish();
                    return true;
                case R.id.action_delete_payee:
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

    public PayeesFragment() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_payees, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_payees);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        mEmptyView = (TextView) rootView.findViewById(R.id.list_payees_empty);

        mAdapter = new PayeesAdapter(getActivity());
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
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                mAdapter.setItemSelected(position, !mAdapter.isItemSelected(position));
                if (mAdapter.isChoiceMode()) {
                    if (mActionMode == null) {
                        mActionMode = ((PayeesActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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
                mActionMode = ((PayeesActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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
        String[] projection = {
                ExpensesContract.Payees._ID,
                ExpensesContract.Payees.NAME
        };
        String sortOrder = ExpensesContract.Payees.NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                ExpensesContract.Payees.CONTENT_URI,
                projection,
                null,
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
