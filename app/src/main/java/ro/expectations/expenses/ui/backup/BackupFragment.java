package ro.expectations.expenses.ui.backup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
    private boolean mIsTaskRunning;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;


    static BackupFragment newInstance(@BackupType int backupType) {
        BackupFragment fragment = new BackupFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BACKUP_TYPE, backupType);
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mIsTaskRunning) {
            showProgressDialog();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        super.onDetach();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(FinancistoImportIntentService.SuccessEvent successEvent) {
        if (mBackupType != BACKUP_TYPE_FINANCISTO) {
            return;
        }

        hideProgressDialog();

        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
            builder.setTitle(getString(R.string.title_success));
            builder.setMessage(getString(R.string.financisto_import_successful));
            builder.setPositiveButton(getString(R.string.button_ok), null);
            mAlertDialog = builder.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(LocalRestoreIntentService.SuccessEvent successEvent) {
        if (mBackupType != BACKUP_TYPE_LOCAL) {
            return;
        }

        hideProgressDialog();

        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
            builder.setTitle(getString(R.string.title_success));
            builder.setMessage(getString(R.string.database_restore_successful));
            builder.setPositiveButton(getString(R.string.button_ok), null);
            builder.show();
        }
    }

    private void showProgressDialog() {
        int stringId;
        mIsTaskRunning = true;
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            stringId = R.string.financisto_import_progress;
        } else {
            stringId = R.string.database_restore_progress;
        }
        mProgressDialog = ProgressDialog.show(getActivity(), null, getString(stringId), true);
    }

    private void hideProgressDialog() {
        mIsTaskRunning = false;
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
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
}
