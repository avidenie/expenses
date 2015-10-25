package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

/**
 * Possible values that can be stored in the `account_types` table.
 */
public enum AccountType {

    CASH(R.string.account_type_cash, R.drawable.ic_cash_round),
    BANK_ACCOUNT(R.string.account_type_bank, R.drawable.ic_cash_round),
    DEBIT_CARD(R.string.account_type_debit_card, R.drawable.ic_credit_card_round),
    CREDIT_CARD(R.string.account_type_credit_card, R.drawable.ic_credit_card_round);

    public final int titleId;
    public final int iconId;

    AccountType(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }
}
