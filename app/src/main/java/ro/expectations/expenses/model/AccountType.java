package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

/**
 * Possible values that can be stored in the `account_types` table.
 */
public enum AccountType {

    CASH(R.string.account_type_cash, R.drawable.ic_wallet_white_24dp, R.color.green_500),
    DEBIT_CARD(R.string.account_type_debit_card, R.drawable.ic_credit_card_white_24dp, R.color.blue_500),
    CREDIT_CARD(R.string.account_type_credit_card, R.drawable.ic_credit_card_white_24dp, R.color.teal_500),
    BANK(R.string.account_type_bank, R.drawable.ic_bank_account_white_24dp, R.color.purple_500),
    SAVINGS(R.string.account_type_savings, R.drawable.ic_money_bag_white_24dp, R.color.deep_orange_500),
    LOAN(R.string.account_type_loan, R.drawable.ic_loan_account_white_24dp, R.color.pink_500),
    ELECTRONIC(R.string.account_type_electronic, R.drawable.ic_electronic_payment_white_24dp, R.color.blue_grey_500),
    OTHER(R.string.account_type_other, R.drawable.ic_wallet_white_24dp, R.color.amber_500);

    public final int titleId;
    public final int iconId;
    public final int colorId;

    AccountType(int titleId, int iconId, int colorId) {
        this.titleId = titleId;
        this.iconId = iconId;
        this.colorId = colorId;
    }
}
