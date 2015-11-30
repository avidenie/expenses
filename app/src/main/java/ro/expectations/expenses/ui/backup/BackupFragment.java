package ro.expectations.expenses.ui.backup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ro.expectations.expenses.R;
import ro.expectations.expenses.helper.BackupHelper;
import ro.expectations.expenses.restore.AbstractRestoreTask;
import ro.expectations.expenses.restore.FinancistoImportTask;
import ro.expectations.expenses.restore.LocalRestoreTask;
import ro.expectations.expenses.ui.widget.DividerItemDecoration;

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
    private RecyclerView mRecyclerView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        mBackupType = getArguments().getInt(ARG_BACKUP_TYPE);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_backup);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        TextView emptyView = (TextView) rootView.findViewById(R.id.list_backup_empty);
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            emptyView.setText(getString(R.string.no_financisto_backup_found));
        }

        BackupAdapter adapter = new BackupAdapter(getActivity(), getOnClickListener(), emptyView);

        File[] files;
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            files = BackupHelper.listBackups(BackupHelper.getFinancistoBackupFolder());
        } else {
            files = BackupHelper.listBackups(BackupHelper.getLocalBackupFolder(getActivity()));
        }
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
    public void onDetach() {
        super.onDetach();

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        int stringId;
        if (mBackupType == BACKUP_TYPE_FINANCISTO) {
            stringId = R.string.import_financisto_backup;
        } else {
            stringId = R.string.restore_backup;
        }
        mProgressDialog = ProgressDialog.show(getActivity(), null, getString(stringId), true);
    }

    private BackupAdapter.OnClickListener getOnClickListener() {
        BackupAdapter.OnClickListener listener;
        final AbstractRestoreTask.Callback callback = new AbstractRestoreTask.Callback() {
            @Override
            public void onPreExecute() {
                showProgressDialog();
                mIsTaskRunning = true;
            }

            @Override
            public void onPostExecute() {
                mIsTaskRunning = false;
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        };

        switch (mBackupType) {
            case BACKUP_TYPE_FINANCISTO:
                listener = new BackupAdapter.OnClickListener() {
                    @Override
                    public void onClick(File file, BackupAdapter.ViewHolder vh) {
                        new FinancistoImportTask(callback).execute(file);
                    }
                };
                break;
            default:
                listener = new BackupAdapter.OnClickListener() {
                    @Override
                    public void onClick(File file, BackupAdapter.ViewHolder vh) {
                        new LocalRestoreTask(callback).execute(file);
                    }
                };
                break;
        }

        return listener;
    }
}
