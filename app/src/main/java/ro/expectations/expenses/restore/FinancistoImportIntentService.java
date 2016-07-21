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

package ro.expectations.expenses.restore;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.expectations.expenses.R;
import ro.expectations.expenses.utils.ColorUtils;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardIssuer;
import ro.expectations.expenses.model.ElectronicPaymentType;
import ro.expectations.expenses.provider.ExpensesContract;
import ro.expectations.expenses.utils.ColorStyleUtils;

public class FinancistoImportIntentService extends AbstractRestoreIntentService {

    private static final String TAG = FinancistoImportIntentService.class.getSimpleName();

    private List<ContentValues> mAccountContentValues = new ArrayList<>();
    private Map<String, String> mCurrencies = new HashMap<>();
    private List<Map<String, String>> mCategories = new ArrayList<>();
    private List<Long> mPayeeIds = new ArrayList<>();
    private List<ContentValues> mTransactionContentValues = new ArrayList<>();
    private Map<Long, List<ContentValues>> mTransactionDetailsContentValues = new HashMap<>();

    @Override
    protected void parse(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
        BufferedReader br = new BufferedReader(reader, 65535);
        try {
            boolean insideEntity = false;
            Map<String, String> values = new HashMap<>();
            String line;
            String tableName = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("$")) {
                    if ("$$".equals(line)) {
                        if (tableName != null && values.size() > 0) {
                            processEntry(tableName, values);
                            tableName = null;
                            insideEntity = false;
                        }
                    } else {
                        int i = line.indexOf(":");
                        if (i > 0) {
                            tableName = line.substring(i + 1);
                            insideEntity = true;
                            values.clear();
                        }
                    }
                } else {
                    if (insideEntity) {
                        int i = line.indexOf(":");
                        if (i > 0) {
                            String columnName = line.substring(0, i);
                            String value = line.substring(i + 1);
                            values.put(columnName, value);
                        }
                    }
                }
            }
        } finally {
            br.close();
        }
        Log.i(TAG, "Finished parsing Financisto backup file");

        processAccountEntries();
        processCategoryEntries();
        processTransactionEntries();
        Log.i(TAG, "Finished processing backup file: " + mOperations.size() + " entries to restore");
    }

    @Override
    protected void notifyFailure(Exception e) {
        EventBus.getDefault().postSticky(new ErrorEvent(e));
    }

    @Override
    protected void notifySuccess() {
        EventBus.getDefault().postSticky(new SuccessEvent());
    }

    private void processEntry(String tableName, Map<String, String> values) {
        switch(tableName) {
            case "account":
                processAccountEntry(values);
                break;
            case "currency":
                processCurrencyEntry(values);
                break;
            case "payee":
                processPayeeEntry(values);
                break;
            case "category":
                processCategoryEntry(values);
                break;
            case "transactions":
                if (!values.containsKey("is_template") || Integer.parseInt(values.get("is_template")) == 0) {
                    processTransactionEntry(values);
                }
                break;
        }
    }

    private void processAccountEntry(Map<String, String> values) {
        AccountType accountType;
        CardIssuer cardIssuer = null;
        ElectronicPaymentType electronicPaymentType = null;
        switch (values.get("type")) {
            case "CASH":
                accountType = AccountType.CASH;
                break;
            case "BANK":
                accountType = AccountType.BANK;
                break;
            case "DEBIT_CARD":
                accountType = AccountType.DEBIT_CARD;
                cardIssuer = getCardIssuer(values.get("card_issuer"));
                break;
            case "CREDIT_CARD":
                accountType = AccountType.CREDIT_CARD;
                cardIssuer = getCardIssuer(values.get("card_issuer"));
                break;
            case "ASSET":
                accountType = AccountType.SAVINGS;
                break;
            case "LIABILITY":
                accountType = AccountType.LOAN;
                break;
            case "ELECTRONIC":
                accountType = AccountType.ELECTRONIC;
                electronicPaymentType = getElectronicPaymentType(values.get("card_issuer"));
                break;
            case "PAYPAL":
                accountType = AccountType.ELECTRONIC;
                electronicPaymentType = ElectronicPaymentType.PAYPAL;
                break;
            case "OTHER":
            default:
                accountType = AccountType.OTHER;
                break;
        }

        String accountSubtype ;
        if (cardIssuer == null) {
            accountSubtype = electronicPaymentType == null ? null : electronicPaymentType.toString();
        } else {
            accountSubtype = cardIssuer.toString();
        }

        ContentValues accountValues = new ContentValues();
        accountValues.put(ExpensesContract.Accounts._ID, Long.parseLong(values.get("_id")));
        accountValues.put(ExpensesContract.Accounts.TITLE, values.get("title"));
        accountValues.put(ExpensesContract.Accounts.CURRENCY, values.get("currency_id"));
        accountValues.put(ExpensesContract.Accounts.BALANCE, Long.parseLong(values.get("total_amount")));
        accountValues.put(ExpensesContract.Accounts.TYPE, accountType.toString());
        accountValues.put(ExpensesContract.Accounts.SUBTYPE, accountSubtype);
        accountValues.put(ExpensesContract.Accounts.IS_ACTIVE, Integer.parseInt(values.get("is_active")));
        accountValues.put(ExpensesContract.Accounts.INCLUDE_INTO_TOTALS, Integer.parseInt(values.get("is_include_into_totals")));
        accountValues.put(ExpensesContract.Accounts.SORT_ORDER, Integer.parseInt(values.get("sort_order")));
        accountValues.put(ExpensesContract.Accounts.NOTE, values.get("note"));
        accountValues.put(ExpensesContract.Accounts.CREATED_AT, Long.parseLong(values.get("creation_date")));

        mAccountContentValues.add(accountValues);
    }

    private void processAccountEntries() {
        for (ContentValues accountValues: mAccountContentValues) {
            String currencyId = accountValues.getAsString(ExpensesContract.Accounts.CURRENCY);
            String currencyCode;
            if (mCurrencies.containsKey(currencyId)) {
                currencyCode = mCurrencies.get(currencyId);
            } else {
                currencyCode = "EUR";
            }
            accountValues.put(ExpensesContract.Accounts.CURRENCY, currencyCode);
            mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Accounts.CONTENT_URI)
                    .withValues(accountValues)
                    .build());
        }
    }

    private void processCurrencyEntry(Map<String, String> values) {
        String currencyCode = values.get("name");
        try {
            Currency currency = Currency.getInstance(currencyCode);
            mCurrencies.put(values.get("_id"), currency.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Could not find currency for currency code '" + currencyCode + "', will default to EUR");
        }
    }

    private void processPayeeEntry(Map<String, String> values) {
        long payeeId = Long.parseLong(values.get("_id"));
        mPayeeIds.add(payeeId);
        mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Payees.CONTENT_URI)
                .withValue(ExpensesContract.Payees._ID, payeeId)
                .withValue(ExpensesContract.Payees.NAME, values.get("title"))
                .build());
    }

    private void processCategoryEntry(Map<String, String> values) {
        mCategories.add(new HashMap<>(values));
    }

    private void processCategoryEntries() {
        List<Map<String, String>> parentCategories = new ArrayList<>();
        List<Map<String, String>> childCategories = new ArrayList<>();
        Map<Long, String> parentStyles = new HashMap<>();

        String defaultColor = ColorUtils.toRGB(ContextCompat.getColor(this, R.color.colorIndigo500));
        String defaultStyle = "ColorIndigo";
        String defaultIcon = "ic_question_mark_black_24dp";

        for(Map<String, String> values: mCategories) {
            long id = Long.parseLong(values.get("_id"));
            if (id <= 0) {
                continue;
            }
            String parentId = getParentCategoryId(values);
            values.put("parent_id", parentId);
            if (parentId.equals("0")) {
                parentCategories.add(values);
            } else {
                childCategories.add(values);
            }
        }

        // sort parent categories for best color distribution
        Collections.sort(parentCategories, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> category2, Map<String, String> category1) {
                return  category2.get("title").compareTo(category1.get("title"));
            }
        });

        // special category for split transactions
        mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Categories.CONTENT_URI)
                .withValue(ExpensesContract.Categories._ID, -1)
                .withValue(ExpensesContract.Categories.NAME, "SPLIT")
                .withValue(ExpensesContract.Categories.COLOR, defaultColor)
                .withValue(ExpensesContract.Categories.STYLE, defaultStyle)
                .withValue(ExpensesContract.Categories.ICON, defaultIcon)
                .build());

        // process parent categories
        String[] styles = getResources().getStringArray(R.array.colorPickerStyles);
        int styleIndex = 0;
        for(Map<String, String> values: parentCategories) {
            long id = Long.parseLong(values.get("_id"));
            Map<String, Integer> colors = ColorStyleUtils.getColorsFromStyle(this, styles[styleIndex]);
            String color = ColorUtils.toRGB(colors.get(ColorStyleUtils.COLOR_PRIMARY));
            mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Categories.CONTENT_URI)
                    .withValue(ExpensesContract.Categories._ID, id)
                    .withValue(ExpensesContract.Categories.NAME, values.get("title"))
                    .withValue(ExpensesContract.Categories.COLOR, color)
                    .withValue(ExpensesContract.Categories.STYLE, styles[styleIndex])
                    .withValue(ExpensesContract.Categories.ICON, defaultIcon)
                    .build());
            parentStyles.put(id, styles[styleIndex]);
            styleIndex++;
            if (styleIndex == styles.length) {
                styleIndex = 0;
            }
        }

        // process child categories
        for(Map<String, String> values: childCategories) {
            long parentId = Long.parseLong(values.get("parent_id"));
            String style;
            if (parentStyles.containsKey(parentId)) {
                style = parentStyles.get(parentId);
            } else {
                style = defaultStyle;
            }
            Map<String, Integer> colors = ColorStyleUtils.getColorsFromStyle(this, style);
            String color = ColorUtils.toRGB(colors.get(ColorStyleUtils.COLOR_PRIMARY));
            mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Categories.CONTENT_URI)
                    .withValue(ExpensesContract.Categories._ID, Long.parseLong(values.get("_id")))
                    .withValue(ExpensesContract.Categories.NAME, values.get("title"))
                    .withValue(ExpensesContract.Categories.PARENT_ID, parentId)
                    .withValue(ExpensesContract.Categories.COLOR, color)
                    .withValue(ExpensesContract.Categories.STYLE, style)
                    .withValue(ExpensesContract.Categories.ICON, defaultIcon)
                    .build());
        }
    }

    private void processTransactionEntry(Map<String, String> values) {

        ContentValues transactionValues = new ContentValues();
        ContentValues transactionDetailsValues = new ContentValues();

        long id = Long.parseLong(values.get("_id"));
        long fromAccountId = Long.parseLong(values.get("from_account_id"));
        long fromAmount = Long.parseLong(values.get("from_amount"));
        long toAccountId = Long.parseLong(values.get("to_account_id"));
        long toAmount = Long.parseLong(values.get("to_amount"));

        String parentId = values.get("parent_id");
        if (parentId != null && !parentId.equals("0")) {
            if (toAccountId > 0) {
                Map<String, String> newValues = new HashMap<>(values);
                newValues.put("parent_id", "0");
                newValues.put("from_account_id", values.get("to_account_id"));
                newValues.put("from_amount", values.get("to_amount"));
                newValues.put("to_account_id", "0");
                newValues.put("to_amount", "0");

                processTransactionEntry(newValues);
            }
            transactionDetailsValues.put(ExpensesContract.TransactionDetails.TRANSACTION_ID, Long.parseLong(parentId));
            transactionDetailsValues.put(ExpensesContract.TransactionDetails.FROM_AMOUNT, 0 - fromAmount);
            transactionDetailsValues.put(ExpensesContract.TransactionDetails.IS_SPLIT, 1);
        } else {
            transactionValues.put(ExpensesContract.Transactions._ID, id);
            transactionDetailsValues.put(ExpensesContract.TransactionDetails.TRANSACTION_ID, id);

            if (toAccountId > 0) {
                transactionValues.put(ExpensesContract.Transactions.FROM_ACCOUNT_ID, fromAccountId);
                transactionDetailsValues.put(ExpensesContract.TransactionDetails.FROM_AMOUNT, 0 - fromAmount);
                transactionValues.put(ExpensesContract.Transactions.TO_ACCOUNT_ID, toAccountId);
                transactionDetailsValues.put(ExpensesContract.TransactionDetails.TO_AMOUNT, toAmount);
            } else {
                if (fromAmount > 0) {
                    transactionValues.put(ExpensesContract.Transactions.TO_ACCOUNT_ID, fromAccountId);
                    transactionDetailsValues.put(ExpensesContract.TransactionDetails.TO_AMOUNT, fromAmount);
                } else {
                    transactionDetailsValues.put(ExpensesContract.TransactionDetails.FROM_AMOUNT, 0 - fromAmount);
                    transactionValues.put(ExpensesContract.Transactions.FROM_ACCOUNT_ID, fromAccountId);
                }
            }

            if (values.containsKey("payee_id")) {
                long payeeId = Long.parseLong(values.get("payee_id"));
                if (payeeId > 0) {
                    transactionValues.put(ExpensesContract.Transactions.PAYEE_ID, payeeId);
                }
            }

            long createdAt = Long.parseLong(values.get("datetime"));
            transactionValues.put(ExpensesContract.Transactions.OCCURRED_AT, createdAt);
            transactionValues.put(ExpensesContract.Transactions.CREATED_AT, createdAt);
            transactionValues.put(ExpensesContract.Transactions.UPDATED_AT, Long.parseLong(values.get("updated_on")));

            transactionValues.put(ExpensesContract.Transactions.NOTE, values.get("note"));

            String originalFromCurrencyId = values.get("original_currency_id");
            if (originalFromCurrencyId != null) {
                transactionValues.put(ExpensesContract.Transactions.ORIGINAL_CURRENCY, originalFromCurrencyId);
                String originalFromAmount = values.get("original_from_amount");
                if (originalFromAmount == null) {
                    originalFromAmount = "0";
                }
                transactionValues.put(ExpensesContract.Transactions.ORIGINAL_AMOUNT, Long.parseLong(originalFromAmount));
            }

            mTransactionContentValues.add(transactionValues);
        }

        if (values.containsKey("category_id")) {
            long categoryId = Long.parseLong(values.get("category_id"));
            if (categoryId > 0) {
                transactionDetailsValues.put(ExpensesContract.TransactionDetails.CATEGORY_ID, categoryId);
            }
        }

        if (!mTransactionDetailsContentValues.containsKey(id)) {
            mTransactionDetailsContentValues.put(id, new ArrayList<ContentValues>());
        }
        mTransactionDetailsContentValues.get(id).add(transactionDetailsValues);
    }

    private void processTransactionEntries() {
        for (ContentValues transactionValues: mTransactionContentValues) {
            if (transactionValues.containsKey(ExpensesContract.Transactions.PAYEE_ID)) {
                long payeeId = transactionValues.getAsLong(ExpensesContract.Transactions.PAYEE_ID);
                if (payeeId <= 0 || !mPayeeIds.contains(payeeId)) {
                    transactionValues.remove(ExpensesContract.Transactions.PAYEE_ID);
                }
            }
            if (transactionValues.containsKey(ExpensesContract.Transactions.ORIGINAL_CURRENCY)) {
                String originalFromCurrencyId = transactionValues.getAsString(ExpensesContract.Transactions.ORIGINAL_CURRENCY);
                if (!originalFromCurrencyId.equals("0") && mCurrencies.containsKey(originalFromCurrencyId)) {
                    String originalFromCurrencyCode = mCurrencies.get(originalFromCurrencyId);
                    transactionValues.put(ExpensesContract.Transactions.ORIGINAL_CURRENCY, originalFromCurrencyCode);
                } else {
                    transactionValues.remove(ExpensesContract.Transactions.ORIGINAL_CURRENCY);
                    transactionValues.remove(ExpensesContract.Transactions.ORIGINAL_AMOUNT);
                }
            }
            mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Transactions.CONTENT_URI)
                    .withValues(transactionValues)
                    .build());
        }
        for (Map.Entry<Long, List<ContentValues>> entry: mTransactionDetailsContentValues.entrySet()) {
            List<ContentValues> details = entry.getValue();
            for (ContentValues values: details) {
                mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.TransactionDetails.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }
        mOperations.add(ContentProviderOperation.newUpdate(ExpensesContract.TransactionDetails.CONTENT_URI)
                .withValue(ExpensesContract.TransactionDetails.CATEGORY_ID, -1)
                .withSelection(
                        ExpensesContract.TransactionDetails.TABLE_NAME + "." + ExpensesContract.TransactionDetails.TRANSACTION_ID + " IN (" +
                        "SELECT DISTINCT " + ExpensesContract.TransactionDetails.TABLE_NAME + "." + ExpensesContract.TransactionDetails.TRANSACTION_ID + " " +
                        "FROM " + ExpensesContract.TransactionDetails.TABLE_NAME + " " +
                        "WHERE " + ExpensesContract.TransactionDetails.TABLE_NAME + "." + ExpensesContract.TransactionDetails.IS_SPLIT + " = 1) " +
                        "AND " + ExpensesContract.TransactionDetails.TABLE_NAME + "." + ExpensesContract.TransactionDetails.IS_SPLIT + " = 0",
                        null)
                .build());
    }

    private ElectronicPaymentType getElectronicPaymentType(String paymentType) {
        switch(paymentType) {
            case "PAYPAL":
                return ElectronicPaymentType.PAYPAL;
            case "BITCOIN":
                return ElectronicPaymentType.BITCOIN;
            case "AMAZON":
                return ElectronicPaymentType.AMAZON;
            case "EBAY":
                return ElectronicPaymentType.EBAY;
            case "GOOGLE_WALLET":
                return ElectronicPaymentType.GOOGLE_WALLET;
            case "WEB_MONEY":
            case "YANDEX_MONEY":
            default:
                return ElectronicPaymentType.OTHER;
        }
    }

    private CardIssuer getCardIssuer(String cardIssuer) {
        switch(cardIssuer) {
            case "VISA":
                return CardIssuer.VISA;
            case "VISA_ELECTRON":
                return CardIssuer.VISA_ELECTRON;
            case "MASTERCARD":
                return CardIssuer.MASTERCARD;
            case "MAESTRO":
                return CardIssuer.MAESTRO;
            case "CIRRUS":
                return CardIssuer.CIRRUS;
            case "AMEX":
                return CardIssuer.AMERICAN_EXPRESS;
            case "JCB":
                return CardIssuer.JCB;
            case "DINERS":
                return CardIssuer.DINERS;
            case "DISCOVER":
                return CardIssuer.DISCOVER;
            case "UNIONPAY":
                return CardIssuer.UNIONPAY;
            case "EPS":
                return CardIssuer.EPS;
            case "NETS":
            default:
                return CardIssuer.OTHER;
        }
    }

    public String getParentCategoryId(Map<String, String> category) {

        String parentId = "0";
        int previousLeft = 0;
        int left = Integer.valueOf(category.get("left"));

        for(Map<String, String> values: mCategories) {
            String currentId = values.get("_id");
            int currentLeft = Integer.valueOf(values.get("left"));
            int currentRight = Integer.valueOf(values.get("right"));

            if (left > currentLeft && left < currentRight) {
                if (currentLeft > previousLeft) {
                    parentId = currentId;
                }
            }
        }

        return parentId;
    }

    public static class SuccessEvent {
    }

    public static class ErrorEvent {
        private Exception mException;

        public ErrorEvent(Exception e) {
            mException = e;
        }

        public Exception getException() {
            return mException;
        }
    }
}
