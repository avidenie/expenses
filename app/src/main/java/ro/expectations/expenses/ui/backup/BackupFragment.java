package ro.expectations.expenses.ui.backup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import java.io.File;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.BackupHelper;
import ro.expectations.expenses.restore.AbstractRestoreIntentService;
import ro.expectations.expenses.restore.LocalRestoreIntentService;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.widget.fragment.AlertDialogFragment;
import ro.expectations.expenses.widget.fragment.ProgressDialogFragment;
import ro.expectations.expenses.widget.recyclerview.DividerItemDecoration;
import ro.expectations.expenses.widget.recyclerview.ItemClickHelper;

public class BackupFragment extends Fragment {

    private BackupAdapter mAdapter;

    private boolean mDismissProgressBar = false;
    private boolean mLaunchAlertDialog = false;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_backup, menu);
            ((DrawerActivity) getActivity()).lockNavigationDrawer();
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
                    showProgressDialog();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_backup);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);

        TextView mEmptyView = (TextView) rootView.findViewById(R.id.list_backup_empty);

        File[] files = BackupHelper.listBackups(BackupHelper.getLocalBackupFolder(getActivity()));

        mEmptyView.setVisibility(files.length > 0 ? View.GONE : View.VISIBLE);

        mAdapter = new BackupAdapter(getActivity(), files);
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
    public void onResume() {
        super.onResume();

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
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(LocalRestoreIntentService.SuccessEvent successEvent) {
        hideProgressDialog();
        showAlertDialog();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private void showAlertDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null && isResumed()) {
            AlertDialogFragment.newInstance(
                    getString(R.string.title_success),
                    getString(R.string.database_restore_successful),
                    true
            ).show(activity.getSupportFragmentManager(), "AlertDialogFragment");
        } else {
            mLaunchAlertDialog = true;
        }
    }

    private void showProgressDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment.newInstance(getString(R.string.database_restore_progress), false)
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
}
