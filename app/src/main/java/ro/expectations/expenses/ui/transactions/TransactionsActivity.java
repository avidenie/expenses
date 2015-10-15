package ro.expectations.expenses.ui.transactions;

import android.os.Bundle;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class TransactionsActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainContent.setLayoutResource(R.layout.content_transactions);
        mMainContent.inflate();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_transactions;
    }
}
