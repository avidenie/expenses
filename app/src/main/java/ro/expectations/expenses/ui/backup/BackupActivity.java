package ro.expectations.expenses.ui.backup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.common.FloatingActionButtonProvider;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class BackupActivity extends DrawerActivity implements FloatingActionButtonProvider {

    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_backup);
        mMainContent.inflate();

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackupFragment backupFragment = (BackupFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                if (backupFragment != null && backupFragment.isVisible()) {
                    backupFragment.fabAction();
                }
            }
        });

        if (savedInstanceState == null) {
            BackupFragment fragment = BackupFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_backup;
    }

    @Override
    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }
}
