package ro.expectations.expenses.restore;

import android.content.ContentProviderOperation;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.provider.ExpensesContract;

public class FinancistoImportIntentService extends AbstractRestoreIntentService {

    protected static final String TAG = FinancistoImportIntentService.class.getSimpleName();

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
                            processTable(tableName, values);
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
        Log.i(TAG, "finished restore database: " + mOperations.size() + " to process");
    }

    @Override
    protected void notifyFailure(Exception e) {
        EventBus.getDefault().post(new FailedEvent(e));
    }

    @Override
    protected void notifySuccess() {
        EventBus.getDefault().post(new SuccessEvent());
    }

    private void processTable(String tableName, Map<String, String> values) {
        switch(tableName) {
            case "account":
                Log.e(TAG, "Importing account with ID " + values.get("_id") + ": " + values.toString());
                mOperations.add(ContentProviderOperation.newInsert(ExpensesContract.Accounts.CONTENT_URI)
                        .withValue(ExpensesContract.Accounts.TITLE, values.get("title"))
                        .withValue(ExpensesContract.Accounts.CURRENCY, "RON")
                        .withValue(ExpensesContract.Accounts.BALANCE, values.get("total_amount"))
                        .withValue(ExpensesContract.Accounts.TYPE, AccountType.CASH.toString())
                        .withValue(ExpensesContract.Accounts.IS_ACTIVE, values.get("is_active"))
                        .withValue(ExpensesContract.Accounts.INCLUDE_INTO_TOTALS, values.get("is_include_into_totals"))
                        .withValue(ExpensesContract.Accounts.NOTE, values.get("note") != null ? values.get("note") : "")
                        .withValue(ExpensesContract.Accounts.CREATED_AT, values.get("creation_date"))
                        .build());
                break;
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
