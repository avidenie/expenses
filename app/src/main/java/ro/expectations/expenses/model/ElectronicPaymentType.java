package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

public enum ElectronicPaymentType {
    PAYPAL(R.string.electronic_payment_paypal, R.drawable.ic_electronic_payment_paypal_white_24dp),
    GOOGLE_WALLET(R.string.electronic_payment_google_wallet, R.drawable.ic_electronic_payment_google_wallet_white_24dp),
    AMAZON(R.string.electronic_payment_amazon, R.drawable.ic_electronic_payment_amazon_white_24dp),
    EBAY(R.string.electronic_payment_ebay, R.drawable.ic_electronic_payment_ebay_white_24dp),
    BITCOIN(R.string.electronic_payment_bitcoin, R.drawable.ic_electronic_payment_bitcoin_white_24dp),
    OTHER(R.string.electronic_payment_other, R.drawable.ic_electronic_payment_white_24dp);

    public final int titleId;
    public final int iconId;

    ElectronicPaymentType(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }
}
