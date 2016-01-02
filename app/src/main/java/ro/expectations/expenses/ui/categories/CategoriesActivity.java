package ro.expectations.expenses.ui.categories;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class CategoriesActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_categories);
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
            CategoriesFragment fragment = CategoriesFragment.newInstance(0L);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_categories;
    }
}
