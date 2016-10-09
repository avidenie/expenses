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

package ro.expectations.expenses.backup;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class BackupIntentService extends IntentService {

    public static final String ACTION_SUCCESS = "BackupIntentService.ACTION_SUCCESS";
    public static final String ACTION_FAILURE = "BackupIntentService.ACTION_FAILURE";

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
        Intent successIntent = new Intent(ACTION_SUCCESS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(successIntent);
    }

    private void notifyFailure(Exception e) {
        Intent failureIntent = new Intent(ACTION_FAILURE);
        failureIntent.putExtra("exception", e);
        LocalBroadcastManager.getInstance(this).sendBroadcast(failureIntent);
    }
}
