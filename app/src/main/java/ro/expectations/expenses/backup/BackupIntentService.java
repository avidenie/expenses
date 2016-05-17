package ro.expectations.expenses.backup;

import android.app.IntentService;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

public class BackupIntentService extends IntentService {

    public BackupIntentService() {
        super("BackupIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(10000);
            notifySuccess();
        } catch (InterruptedException e) {
            notifyFailure(e);
        }
    }

    private void notifySuccess() {
        EventBus.getDefault().post(new SuccessEvent());
    }

    private void notifyFailure(Exception e) {
        EventBus.getDefault().post(new ErrorEvent(e));
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
