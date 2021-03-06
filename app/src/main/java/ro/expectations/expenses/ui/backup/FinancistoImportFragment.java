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

package ro.expectations.expenses.ui.backup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.File;

import ro.expectations.expenses.R;
import ro.expectations.expenses.restore.AbstractRestoreIntentService;
import ro.expectations.expenses.restore.FinancistoImportIntentService;
import ro.expectations.expenses.ui.dialog.AlertDialogFragment;
import ro.expectations.expenses.ui.dialog.ConfirmationDialogFragment;
import ro.expectations.expenses.ui.dialog.ProgressDialogFragment;
import ro.expectations.expenses.ui.overview.OverviewActivity;
import ro.expectations.expenses.ui.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.utils.BackupUtils;
import ro.expectations.expenses.utils.DrawableUtils;

public class FinancistoImportFragment extends Fragment
        implements ConfirmationDialogFragment.Listener, DialogInterface.OnClickListener {

    private static final String TAG = FinancistoImportFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 0;
    private static final int CONFIRMATION_DIALOG_REQUEST_CODE = 0;
    private static final String KEY_SELECTED_FILE = "selected_file";

    private FinancistoImportAdapter mAdapter;
    private TextView mEmptyView;
    private LinearLayout mRequestPermissionRationale;
    private TextView mPermissionRationale;
    private Button mAllowAccess;

    private boolean mDismissProgressBar = false;
    private boolean mLaunchAlertDialog = false;

    private File mSelectedFile;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_import_financisto, menu);

            MenuItem actionFinancistoImport = menu.findItem(R.id.action_financisto_import);
            actionFinancistoImport.setIcon(DrawableUtils.tint(getContext(), actionFinancistoImport.getIcon(), R.color.colorWhite));

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            mode.setTitle(getResources().getQuantityString(R.plurals.selected_backup_files, 1, 1));

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_financisto_import:
                    mSelectedFile = mAdapter.getItem(mAdapter.getSelectedItemPosition());

                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(
                                activity.getString(R.string.financisto_import_confirmation_title),
                                activity.getString(R.string.financisto_import_confirmation_message),
                                activity.getString(R.string.button_import),
                                activity.getString(R.string.button_cancel), false);
                        confirmationDialogFragment.setTargetFragment(FinancistoImportFragment.this, CONFIRMATION_DIALOG_REQUEST_CODE);
                        confirmationDialogFragment.show(activity.getSupportFragmentManager(), "ConfirmationDialogFragment");
                    }
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
        }
    };

    private BroadcastReceiver mSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgressDialog();
            showAlertDialog();
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    };

    private BroadcastReceiver mFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Throwable failureException = (Throwable) intent.getSerializableExtra("exception");
            Log.e(TAG, "An error occurred while importing Financisto backup: "
                    + failureException.getMessage(), failureException);
        }
    };

    static FinancistoImportFragment newInstance() {
        return new FinancistoImportFragment();
    }

    public FinancistoImportFragment() {
        // Required empty public constructor
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

        mAdapter = new FinancistoImportAdapter(getActivity(), new File[0]);
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    mAdapter.setItemSelected(position, false);
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                } else if (mAdapter.isChoiceMode()) {
                    mAdapter.setItemSelected(position, true);
                    if (mActionMode == null) {
                        mActionMode = ((FinancistoImportActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    } else {
                        mActionMode.invalidate();
                    }
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    mAdapter.setItemSelected(position, false);
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                } else {
                    mAdapter.setItemSelected(position, true);
                    if (mActionMode == null) {
                        mActionMode = ((FinancistoImportActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    } else {
                        mActionMode.invalidate();
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
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            mSelectedFile = (File) savedInstanceState.getSerializable(KEY_SELECTED_FILE);
            if (mAdapter.isChoiceMode() && mActionMode == null) {
                mActionMode = ((FinancistoImportActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(mSuccessReceiver,
                new IntentFilter(FinancistoImportIntentService.ACTION_SUCCESS));
        localBroadcastManager.registerReceiver(mFailureReceiver,
                new IntentFilter(FinancistoImportIntentService.ACTION_FAILURE));

        if (mDismissProgressBar) {
            hideProgressDialog();
            mDismissProgressBar = false;
        }

        if (mLaunchAlertDialog) {
            showAlertDialog();
            mLaunchAlertDialog = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.unregisterReceiver(mSuccessReceiver);
        localBroadcastManager.unregisterReceiver(mFailureReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SELECTED_FILE, mSelectedFile);
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
            }
        }
    }

    @Override
    public void onConfirmed(int targetRequestCode) {
        if (targetRequestCode == CONFIRMATION_DIALOG_REQUEST_CODE) {
            Intent financistoImportIntent = new Intent(getActivity(), FinancistoImportIntentService.class);
            financistoImportIntent.putExtra(AbstractRestoreIntentService.ARG_FILE_URI, Uri.fromFile(mSelectedFile).getPath());
            getActivity().startService(financistoImportIntent);
            showProgressDialog();
        }
    }

    @Override
    public void onDenied(int targetRequestCode) {
        // nothing to do
    }

    private void showRequestPermissionRationale() {
        String app = getString(R.string.app_name);
        String permissionRationale = String.format(getString(R.string.read_storage_rationale_financisto_import), app, app);
        mPermissionRationale.setText(Html.fromHtml(permissionRationale));
        mRequestPermissionRationale.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        File[] files = BackupUtils.listBackups(BackupUtils.getFinancistoBackupFolder());
        mAdapter.setFiles(files);
        mRequestPermissionRationale.setVisibility(View.GONE);
        mEmptyView.setText(getString(R.string.no_financisto_backup_found));
        mEmptyView.setVisibility(files.length > 0 ? View.GONE : View.VISIBLE);
    }

    private void showAlertDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null && isResumed()) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                    getString(R.string.success),
                    getString(R.string.financisto_import_successful),
                    false
            );
            alertDialogFragment.setTargetFragment(this, 0);
            alertDialogFragment.show(activity.getSupportFragmentManager(), "AlertDialogFragment");
        } else {
            mLaunchAlertDialog = true;
        }
    }

    private void showProgressDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment.newInstance(getString(R.string.financisto_import_progress), false)
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

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent = new Intent(getActivity(), OverviewActivity.class);
        startActivity(intent);
    }
}

