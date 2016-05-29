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

package ro.expectations.expenses.ui.backup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import ro.expectations.expenses.R;
import ro.expectations.expenses.backup.BackupIntentService;
import ro.expectations.expenses.helper.BackupHelper;
import ro.expectations.expenses.helper.DrawableHelper;
import ro.expectations.expenses.restore.AbstractRestoreIntentService;
import ro.expectations.expenses.restore.LocalRestoreIntentService;
import ro.expectations.expenses.ui.common.FloatingActionButtonProvider;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.widget.dialog.AlertDialogFragment;
import ro.expectations.expenses.widget.dialog.ProgressDialogFragment;
import ro.expectations.expenses.widget.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.widget.recyclerview.ItemClickHelper;

public class BackupFragment extends Fragment {

    private static final String TAG = BackupFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 0;

    private boolean mReadStorageAllowed = false;
    private boolean mIsFinancistoInstalled = false;

    private FloatingActionButtonProvider mFloatingActionButtonProvider;

    private BackupAdapter mAdapter;
    private TextView mEmptyView;
    private LinearLayout mRequestPermissionRationale;
    private TextView mPermissionRationale;
    private Button mAllowAccess;

    private boolean mDismissProgressBar = false;
    private boolean mLaunchRestoreAlertDialog = false;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_backup, menu);
            ((DrawerActivity) getActivity()).lockNavigationDrawer();
            MenuItem actionRestore = menu.findItem(R.id.action_restore);
            actionRestore.setIcon(DrawableHelper.tint(getContext(), actionRestore.getIcon(), R.color.colorWhite));
            MenuItem actionDelete = menu.findItem(R.id.action_delete);
            actionDelete.setIcon(DrawableHelper.tint(getContext(), actionDelete.getIcon(), R.color.colorWhite));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedFiles = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_backup_files,
                    selectedFiles,
                    selectedFiles
            ));
            if (mAdapter.getSelectedItemCount() == 1) {
                menu.findItem(R.id.action_restore).setVisible(true);
            } else {
                menu.findItem(R.id.action_restore).setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_restore:
                    File file = mAdapter.getItem(mAdapter.getSelectedItemPositions().get(0));
                    Intent localRestoreIntent = new Intent(getActivity(), LocalRestoreIntentService.class);
                    localRestoreIntent.putExtra(AbstractRestoreIntentService.ARG_FILE_URI, Uri.fromFile(file).getPath());
                    getActivity().startService(localRestoreIntent);
                    showProgressDialog(R.string.database_restore_progress);
                    return true;
                case R.id.action_delete:
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

    static BackupFragment newInstance() {
        return new BackupFragment();
    }

    public BackupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FloatingActionButtonProvider) {
            mFloatingActionButtonProvider = (FloatingActionButtonProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FloatingActionButtonProvider");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mIsFinancistoInstalled = isFinancistoInstalled();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        mRequestPermissionRationale = (LinearLayout) rootView.findViewById(R.id.request_permission_rationale);
        mPermissionRationale = (TextView) rootView.findViewById(R.id.permission_rationale);
        mAllowAccess = (Button) rootView.findViewById(R.id.allow_access);
        mEmptyView = (TextView) rootView.findViewById(R.id.list_backup_empty);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_backup);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new BackupAdapter(getActivity(), new File[0]);
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
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
                        mActionMode = ((BackupActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            setupRecyclerView();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRequestPermissionRationale();
            mAllowAccess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                }
            });
            mAllowAccess.setVisibility(View.VISIBLE);
            mFloatingActionButtonProvider.getFloatingActionButton().hide();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.isChoiceMode() && mActionMode == null) {
                mActionMode = ((BackupActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDismissProgressBar) {
            hideProgressDialog();
            mDismissProgressBar = false;
        }

        if (mLaunchRestoreAlertDialog) {
            showRestoreAlertDialog();
            mLaunchRestoreAlertDialog = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mIsFinancistoInstalled && mReadStorageAllowed) {
            menu.add(Menu.NONE, R.id.action_financisto_import, Menu.NONE, R.string.action_financisto_import);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_financisto_import) {
            Intent financistoImportIntent = new Intent(getActivity(), FinancistoImportActivity.class);
            startActivity(financistoImportIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFloatingActionButtonProvider = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupRecyclerView();
            } else {
                showRequestPermissionRationale();
                mAllowAccess.setVisibility(View.GONE);
                mFloatingActionButtonProvider.getFloatingActionButton().hide();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccess(LocalRestoreIntentService.SuccessEvent successEvent) {
        hideProgressDialog();
        showRestoreAlertDialog();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailure(LocalRestoreIntentService.ErrorEvent errorEvent) {
        Log.e(TAG, "An error occurred while restoring backup: "
                + errorEvent.getException().getMessage(), errorEvent.getException());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccess(BackupIntentService.SuccessEvent successEvent) {
        hideProgressDialog();
        showBackupAlertDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailure(BackupIntentService.ErrorEvent errorEvent) {
        Log.e(TAG, "An error occurred while backing up database: "
                + errorEvent.getException().getMessage(), errorEvent.getException());
    }

    public void fabAction() {
        Intent backupIntent = new Intent(getActivity(), BackupIntentService.class);
        getActivity().startService(backupIntent);
        showProgressDialog(R.string.database_backup_progress);
    }

    private void showRequestPermissionRationale() {
        String app = getString(R.string.app_name);
        String permissionRationale = String.format(getString(R.string.read_storage_rationale), app, app);
        mPermissionRationale.setText(Html.fromHtml(permissionRationale));
        mRequestPermissionRationale.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        File[] files = BackupHelper.listBackups(BackupHelper.getLocalBackupFolder(getActivity()));
        mAdapter.setFiles(files);
        mFloatingActionButtonProvider.getFloatingActionButton().show();
        mRequestPermissionRationale.setVisibility(View.GONE);
        mEmptyView.setText(getString(R.string.no_database_backup_found));
        mEmptyView.setVisibility(files.length > 0 ? View.GONE : View.VISIBLE);
        mReadStorageAllowed = true;
        getActivity().invalidateOptionsMenu();
    }

    private void showBackupAlertDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null && isResumed()) {
            AlertDialogFragment.newInstance(
                    getString(R.string.success),
                    getString(R.string.database_backup_successful),
                    true
            ).show(activity.getSupportFragmentManager(), "BackupAlertDialogFragment");
        } else {
            mLaunchRestoreAlertDialog = true;
        }
    }

    private void showRestoreAlertDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null && isResumed()) {
            AlertDialogFragment.newInstance(
                    getString(R.string.success),
                    getString(R.string.database_restore_successful),
                    true
            ).show(activity.getSupportFragmentManager(), "RestoreAlertDialogFragment");
        } else {
            mLaunchRestoreAlertDialog = true;
        }
    }

    private void showProgressDialog(@StringRes int resId) {
        showProgressDialog(getString(resId));
    }

    private void showProgressDialog(String message) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment.newInstance(message, false)
                    .show(activity.getSupportFragmentManager(), "ProgressDialogFragment");
        }
    }

    private void hideProgressDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment) activity
                    .getSupportFragmentManager().findFragmentByTag("ProgressDialogFragment");
            if (isResumed() && progressDialogFragment != null && progressDialogFragment.getDialog().isShowing()) {
                progressDialogFragment.dismiss();
            } else {
                mDismissProgressBar = true;
            }
        }
    }

    private boolean isFinancistoInstalled() {
        PackageManager pm = getActivity().getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo("ru.orangesoftware.financisto", PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return true; //installed;
    }
}
