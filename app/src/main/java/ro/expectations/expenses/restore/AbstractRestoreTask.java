package ro.expectations.expenses.restore;

import android.os.AsyncTask;

import java.io.File;

abstract public class AbstractRestoreTask extends AsyncTask<File, Integer, Void> {

    private Callback mCallback;

    public AbstractRestoreTask(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        mCallback.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mCallback.onPostExecute();
    }

    public interface Callback {
        void onPreExecute();
        void onPostExecute();
    }
}
