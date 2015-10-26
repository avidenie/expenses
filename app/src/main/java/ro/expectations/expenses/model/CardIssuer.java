package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

public enum CardIssuer {

    VISA(R.string.card_issuer_visa, R.drawable.ic_card_visa_round),
    VISA_ELECTRON(R.string.card_issuer_visa_electron, R.drawable.ic_card_visa_round),
    MASTERCARD(R.string.card_issuer_mastercard, R.drawable.ic_card_mastercard_round),
    MAESTRO(R.string.card_issuer_maestro, R.drawable.ic_card_maestro_round),
    AMERICAN_EXPRESS(R.string.card_issuer_american_express, R.drawable.ic_card_amex_round),
    DISCOVER(R.string.card_issuer_discover, R.drawable.ic_card_discover_round),
    CIRRUS(R.string.card_issuer_cirrus, R.drawable.ic_card_cirrus_round),
    JCB(R.string.card_issuer_jcb, R.drawable.ic_card_jcb_round),
    DINERS(R.string.card_issuer_diners, R.drawable.ic_card_dinners_round),
    UNIONPAY(R.string.card_issuer_unionpay, R.drawable.ic_card_unionpay_round),
    EPS(R.string.card_issuer_eps, R.drawable.ic_card_eps_round),
    OTHER(R.string.card_issuer_other, R.drawable.ic_credit_card_round);

    public final int titleId;
    public final int iconId;

    CardIssuer(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }
}
