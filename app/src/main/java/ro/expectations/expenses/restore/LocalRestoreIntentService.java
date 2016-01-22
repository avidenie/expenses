package ro.expectations.expenses.restore;

import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.event.EventBus;
import ro.expectations.expenses.ui.backup.BackupFragment;

public class LocalRestoreIntentService extends AbstractRestoreIntentService {

    protected static final String TAG = LocalRestoreIntentService.class.getSimpleName();

    @Override
    protected void parse(InputStream input) throws IOException {
        for(int i = 1; i <= 10; i++) {
            SystemClock.sleep(1000);
        }
    }

    @Override
    protected void notifyFailure(Exception e) {
        EventBus.getDefault().post(new BackupFragment.FailedEvent(e));
    }

    @Override
    protected void notifySuccess() {
        EventBus.getDefault().post(new BackupFragment.SuccessEvent());
    }
}
