package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

public enum CardIssuer {

    VISA(R.string.card_issuer_visa, R.drawable.ic_card_visa_white_24dp),
    VISA_ELECTRON(R.string.card_issuer_visa_electron, R.drawable.ic_card_visa_white_24dp),
    MASTERCARD(R.string.card_issuer_mastercard, R.drawable.ic_card_mastercard_white_24dp),
    MAESTRO(R.string.card_issuer_maestro, R.drawable.ic_card_maestro_white_24dp),
    AMERICAN_EXPRESS(R.string.card_issuer_american_express, R.drawable.ic_card_amex_white_24dp),
    DISCOVER(R.string.card_issuer_discover, R.drawable.ic_card_discover_white_24dp),
    CIRRUS(R.string.card_issuer_cirrus, R.drawable.ic_card_cirrus_white_24dp),
    JCB(R.string.card_issuer_jcb, R.drawable.ic_card_jcb_white_24dp),
    DINERS(R.string.card_issuer_diners, R.drawable.ic_card_dinners_white_24dp),
    UNIONPAY(R.string.card_issuer_unionpay, R.drawable.ic_card_unionpay_white_24dp),
    EPS(R.string.card_issuer_eps, R.drawable.ic_card_eps_white_24dp),
    OTHER(R.string.card_issuer_other, R.drawable.ic_credit_card_white_24dp);

    public final int titleId;
    public final int iconId;

    CardIssuer(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }
}
