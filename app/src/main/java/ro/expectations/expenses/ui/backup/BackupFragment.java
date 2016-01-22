package ro.expectations.expenses.ui.backup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.BackupHelper;
import ro.expectations.expenses.restore.AbstractRestoreIntentService;
import ro.expectations.expenses.restore.FinancistoImportIntentService;
import ro.expectations.expenses.restore.LocalRestoreIntentService;
import ro.expectations.expenses.widget.fragment.AlertDialogFragment;
import ro.expectations.expenses.widget.fragment.ProgressDialogFragment;
import ro.expectations.expenses.widget.recyclerview.DividerItemDecoration;

public class BackupFragment extends Fragment {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BACKUP_TYPE_LOCAL, BACKUP_TYPE_FINANCISTO})
    public @interface BackupType {
    }

    public static final int BACKUP_TYPE_LOCAL = 0;
    public static final int BACKUP_TYPE_FINANCISTO = 1;

    private static final String ARG_BACKUP_TYPE = "backup_type";

    private int mBackupType;
    private boolean mDismissProgressBar = false;
    private boolean mLaunchAlertDialog = false;

    static BackupFragment newInstance(@BackupType int backupType) {
        Bundle args = new Bundle();
        args.putInt(ARG_BACKUP_TYPE, backupType);

        BackupFragment fragment = new BackupFragment();
        fragment.setArguments(args);
        return fragment;
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

        mBackupType = getArguments().getInt(ARG_BACKUP_TYPE);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_backup);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        TextView mEmptyView = (TextView) rootView.findViewById(R.id.list_backup_empty);
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            mEmptyView.setText(getString(R.string.no_financisto_backup_found));
        }

        File[] files;
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            files = BackupHelper.listBackups(BackupHelper.getFinancistoBackupFolder());
        } else {
            files = BackupHelper.listBackups(BackupHelper.getLocalBackupFolder(getActivity()));
        }

        mEmptyView.setVisibility(files.length > 0 ? View.GONE : View.VISIBLE);

        BackupAdapter adapter = new BackupAdapter(getActivity(), getOnClickListener());
        adapter.setFiles(files);
        mRecyclerView.setAdapter(adapter);

        return rootView;
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
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(SuccessEvent successEvent) {
        hideProgressDialog();
        showAlertDialog();
    }

    private void showAlertDialog() {
        int stringId;
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            stringId = R.string.financisto_import_successful;
        } else {
            stringId = R.string.database_restore_successful;
        }
        FragmentActivity activity = getActivity();
        if (activity != null && isResumed()) {
            AlertDialogFragment.newInstance(
                    getString(R.string.title_success),
                    getString(stringId),
                    true
            ).show(activity.getSupportFragmentManager(), "AlertDialogFragment");
        } else {
            mLaunchAlertDialog = true;
        }
    }

    private void showProgressDialog() {
        int stringId;
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            stringId = R.string.financisto_import_progress;
        } else {
            stringId = R.string.database_restore_progress;
        }
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment.newInstance(getString(stringId), false)
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

    private BackupAdapter.OnClickListener getOnClickListener() {
        BackupAdapter.OnClickListener listener;

        switch (mBackupType) {
            case BACKUP_TYPE_FINANCISTO:
                listener = new BackupAdapter.OnClickListener() {
                    @Override
                    public void onClick(File file, BackupAdapter.ViewHolder vh) {
                        Intent financistoImportIntent = new Intent(getActivity(), FinancistoImportIntentService.class);
                        financistoImportIntent.putExtra(AbstractRestoreIntentService.ARG_FILE_URI, Uri.fromFile(file).getPath());
                        getActivity().startService(financistoImportIntent);
                        showProgressDialog();
                    }
                };
                break;
            default:
                listener = new BackupAdapter.OnClickListener() {
                    @Override
                    public void onClick(File file, BackupAdapter.ViewHolder vh) {
                        Intent localImportIntent = new Intent(getActivity(), LocalRestoreIntentService.class);
                        localImportIntent.putExtra(AbstractRestoreIntentService.ARG_FILE_URI, Uri.fromFile(file).getPath());
                        getActivity().startService(localImportIntent);
                        showProgressDialog();
                    }
                };
                break;
        }

        return listener;
    }

    public static class SuccessEvent {
    }

    public static class FailedEvent {
        private Exception mException;

        public FailedEvent(Exception e) {
            mException = e;
        }

        public Exception getException() {
            return mException;
        }
    }
}
