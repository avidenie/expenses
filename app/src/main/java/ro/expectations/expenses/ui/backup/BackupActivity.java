package ro.expectations.expenses.ui.backup;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ro.expectations.expenses.R;

public class BackupActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private boolean mIsFinancistoInstalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_backup);

        mIsFinancistoInstalled = isFinancistoInstalled();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("@@@", "FAB clicked");
            }
        });

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up tabs from the view pager.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mSectionsPagerAdapter.getCount() > 1) {
            tabLayout.setupWithViewPager(mViewPager);
        } else {
            tabLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_backup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return BackupListFragment.newInstance();
            }

            if (mIsFinancistoInstalled) {
                if (position == 1) {
                    return FinancistoBackupListFragment.newInstance();
                }
            }

            return null;
        }

        @Override
        public int getCount() {
            int count = 1;
            if (mIsFinancistoInstalled) {
                count++;
            }
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Local";
            }

            if (mIsFinancistoInstalled) {
                if (position == 1) {
                    return "Financisto";
                }
            }

            return null;
        }
    }

    public static class BackupListFragment extends Fragment {

        public static BackupListFragment newInstance() {
            return new BackupListFragment();
        }

        public BackupListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_backup, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Hello from Backup");
            return rootView;
        }
    }

    public static class FinancistoBackupListFragment extends Fragment {

        public static FinancistoBackupListFragment newInstance() {
            return new FinancistoBackupListFragment();
        }

        public FinancistoBackupListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_backup, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Hello from Financisto Backup");
            return rootView;
        }
    }
}
