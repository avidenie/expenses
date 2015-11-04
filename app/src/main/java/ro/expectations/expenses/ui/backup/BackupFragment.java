package ro.expectations.expenses.ui.backup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.widget.DividerItemDecoration;

public class BackupFragment extends Fragment {

    private RecyclerView mRecyclerView;

    public BackupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_backup);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);

        View emptyView = rootView.findViewById(R.id.list_accounts_empty);
        BackupAdapter adapter = new BackupAdapter(getActivity(), new BackupAdapter.OnClickListener() {
            @Override
            public void onClick(String filename, BackupAdapter.ViewHolder vh) {
                Log.i("@@@", "Backup filename " + filename + " clicked");
            }
        }, emptyView);
        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
