package ro.expectations.expenses.restore;

import android.os.SystemClock;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;

public class LocalRestoreIntentService extends AbstractRestoreIntentService {

    protected static final String TAG = LocalRestoreIntentService.class.getSimpleName();

    @Override
    protected void parse(InputStream input) throws IOException {
        for(int i = 1; i <= 10; i++) {
            SystemClock.sleep(1000);
        }
    }

    @Override
    protected void notifySuccess() {
        EventBus.getDefault().post(new SuccessEvent());
    }

    @Override
    protected void notifyFailure(Exception e) {
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
