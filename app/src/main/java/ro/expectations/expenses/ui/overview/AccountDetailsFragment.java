package ro.expectations.expenses.ui.overview;

import android.os.Bundle;

import ro.expectations.expenses.ui.transactions.TransactionsFragment;

public class AccountDetailsFragment extends TransactionsFragment {

    public static AccountDetailsFragment newInstance(long accountId) {
        AccountDetailsFragment fragment = new AccountDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    public AccountDetailsFragment() {
        // Required empty public constructor
    }
}
