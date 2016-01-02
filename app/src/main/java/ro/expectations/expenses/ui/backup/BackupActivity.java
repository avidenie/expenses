package ro.expectations.expenses.ui.backup;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class BackupActivity extends DrawerActivity {

    private boolean mIsFinancistoInstalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_backup);
        mMainContent.inflate();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Not yet implemented", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
        });

        if (savedInstanceState == null) {
            BackupFragment fragment = BackupFragment.newInstance(BackupFragment.BACKUP_TYPE_LOCAL);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment, fragment);
            transaction.commit();
        }

        mIsFinancistoInstalled = isFinancistoInstalled();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_backup;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsFinancistoInstalled) {
            menu.add(Menu.NONE, R.id.action_financisto_import, Menu.NONE, R.string.action_financisto_import);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_financisto_import) {
            Intent financistoImportIntent = new Intent(this, FinancistoImportActivity.class);
            startActivity(financistoImportIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isFinancistoInstalled() {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo("ru.orangesoftware.financisto", PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
