package ro.expectations.expenses.restore;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardIssuer;
import ro.expectations.expenses.model.ElectronicPaymentType;
import ro.expectations.expenses.provider.ExpensesContract;

public class FinancistoImportIntentService extends AbstractRestoreIntentService {

    protected static final String TAG = FinancistoImportIntentService.class.getSimpleName();

    private ArrayList<ContentValues> mAccountContentValues = new ArrayList<>();
    private Map<String, String> mCurrencies = new HashMap<>();

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
        processAccountEntries();
        Log.i(TAG, "Finished parsing Financisto backup file: " + mOperations.size() + " entries to restore");
    }

    @Override
    protected void notifyFailure(Exception e) {
        EventBus.getDefault().post(new FailedEvent(e));
    }

    @Override
    protected void notifySuccess() {
        EventBus.getDefault().post(new SuccessEvent());
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

        String note = values.get("note");
        if (note == null) {
            note = "";
        }

        ContentValues accountValues = new ContentValues();
        accountValues.put(ExpensesContract.Accounts.TITLE, values.get("title"));
        accountValues.put(ExpensesContract.Accounts.CURRENCY, values.get("currency_id"));
        accountValues.put(ExpensesContract.Accounts.BALANCE, values.get("total_amount"));
        accountValues.put(ExpensesContract.Accounts.TYPE, accountType.toString());
        accountValues.put(ExpensesContract.Accounts.SUBTYPE, accountSubtype);
        accountValues.put(ExpensesContract.Accounts.IS_ACTIVE, values.get("is_active"));
        accountValues.put(ExpensesContract.Accounts.INCLUDE_INTO_TOTALS, values.get("is_include_into_totals"));
        accountValues.put(ExpensesContract.Accounts.SORT_ORDER, values.get("sort_order"));
        accountValues.put(ExpensesContract.Accounts.NOTE, note);
        accountValues.put(ExpensesContract.Accounts.CREATED_AT, values.get("creation_date"));

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
        mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Payees.CONTENT_URI)
                .withValue(ExpensesContract.Payees._ID, values.get("_id"))
                .withValue(ExpensesContract.Payees.NAME, values.get("title"))
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

    public static class SuccessEvent {
    }

    public static class FailedEvent {
        private Exception mException;

        public FailedEvent(Exception e) {
            mException = e;
        }

        public Exception getException() {
            return mException;
        }
    }
}
