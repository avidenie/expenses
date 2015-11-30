package ro.expectations.expenses.restore;

import android.os.SystemClock;

import java.io.File;

public class LocalRestoreTask extends AbstractRestoreTask {

    public LocalRestoreTask(Callback callback) {
        super(callback);
    }

    @Override
    protected Void doInBackground(File... params) {
        for(int i = 1; i <= 10; i++) {
            SystemClock.sleep(1000);
        }
        return null;
    }
}
