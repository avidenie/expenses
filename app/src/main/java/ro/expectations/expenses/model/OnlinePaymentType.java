/*
 * Copyright © 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

public enum OnlinePaymentType {
    PAYPAL(R.string.online_payment_paypal, R.drawable.ic_payment_paypal_black_24dp),
    GOOGLE_WALLET(R.string.online_payment_google_wallet, R.drawable.ic_payment_google_wallet_black_24dp),
    AMAZON(R.string.online_payment_amazon, R.drawable.ic_payment_amazon_black_24dp),
    OTHER(R.string.online_payment_other, R.drawable.ic_online_payment_black_24dp);

    public final int titleId;
    public final int iconId;

    OnlinePaymentType(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }
}
