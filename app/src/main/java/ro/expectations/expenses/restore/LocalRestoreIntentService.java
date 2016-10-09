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

import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;

public class LocalRestoreIntentService extends AbstractRestoreIntentService {

    public static final String ACTION_SUCCESS = "LocalRestoreIntentService.ACTION_SUCCESS";
    public static final String ACTION_FAILURE = "LocalRestoreIntentService.ACTION_FAILURE";

    protected static final String TAG = LocalRestoreIntentService.class.getSimpleName();

    @Override
    protected void parse(InputStream input) throws IOException {
        for(int i = 1; i <= 10; i++) {
            SystemClock.sleep(1000);
        }
    }

    @Override
    protected void notifySuccess() {
        Intent successIntent = new Intent(ACTION_SUCCESS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(successIntent);
    }

    @Override
    protected void notifyFailure(Exception e) {
        Intent failureIntent = new Intent(ACTION_FAILURE);
        failureIntent.putExtra("exception", e);
        LocalBroadcastManager.getInstance(this).sendBroadcast(failureIntent);
    }
}
