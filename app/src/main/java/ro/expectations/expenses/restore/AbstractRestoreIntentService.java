package ro.expectations.expenses.restore;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import ro.expectations.expenses.provider.ExpensesContract;

abstract public class AbstractRestoreIntentService extends IntentService {

    public static final String ARG_FILE_URI = "arg_file_uri";

    protected static final String TAG = AbstractRestoreIntentService.class.getSimpleName();

    protected ArrayList<ContentProviderOperation> mOperations = new ArrayList<>();

    public AbstractRestoreIntentService() {
        super("RestoreIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String filePath = intent.getStringExtra(AbstractRestoreIntentService.ARG_FILE_URI);
        File file = new File(filePath);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            InputStream decompressedStream = decompressStream(inputStream);
            parse(decompressedStream);
        } catch(IOException e) {
            notifyFailure(e);
            return;
        }

        try {
            emptyDatabase();
        } catch(RemoteException | OperationApplicationException e) {
            notifyFailure(e);
            return;
        }

        try {
            populateDatabase();
        } catch(RemoteException | OperationApplicationException e) {
            notifyFailure(e);
            return;
        }

        notifySuccess();
    }

    protected InputStream decompressStream(InputStream input) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(input, 2);
        byte [] signature = new byte[2];
        pb.read(signature);
        pb.unread(signature);

        // check if the signature matches standard GZIP magic number
        int header = ((int) signature[0] & 0xff) | ((signature[1] << 8) & 0xff00);
        if (GZIPInputStream.GZIP_MAGIC == header) {
            return new GZIPInputStream(pb);
        } else {
            return pb;
        }
    }

    protected void emptyDatabase() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        operations.add(ContentProviderOperation.newDelete(ExpensesContract.Accounts.CONTENT_URI).build());

        getContentResolver().applyBatch(ExpensesContract.CONTENT_AUTHORITY, operations);
    }

    protected void populateDatabase() throws RemoteException, OperationApplicationException {
        getContentResolver().applyBatch(ExpensesContract.CONTENT_AUTHORITY, mOperations);
    }

    abstract protected void parse(InputStream input) throws IOException;

    abstract protected void notifyFailure(Exception e);

    abstract protected void notifySuccess();
}
