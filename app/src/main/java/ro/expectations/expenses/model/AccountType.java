package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

/**
 * Possible values that can be stored in the `account_types` table.
 */
public enum AccountType {

    CASH(R.string.account_type_cash, R.drawable.ic_wallet_round),
    DEBIT_CARD(R.string.account_type_debit_card, R.drawable.ic_credit_card_round),
    CREDIT_CARD(R.string.account_type_credit_card, R.drawable.ic_credit_card_round),
    BANK(R.string.account_type_bank, R.drawable.ic_bank_account_round),
    SAVINGS(R.string.account_type_savings, R.drawable.ic_money_bag_round),
    LOAN(R.string.account_type_loan, R.drawable.ic_loan_account_round);

    public final int titleId;
    public final int iconId;

    AccountType(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }
}
