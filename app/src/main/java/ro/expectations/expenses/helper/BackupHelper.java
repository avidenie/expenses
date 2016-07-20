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

package ro.expectations.expenses.helper;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class BackupHelper {

    public static File getLocalBackupFolder(Context context) {
        File rootFolder = new File(context.getExternalFilesDir(null) + File.separator + "backup");
        rootFolder.mkdirs();
        return rootFolder;
    }

    public static File getFinancistoBackupFolder() {
        return new File(Environment.getExternalStorageDirectory() + File.separator + "financisto");
    }

    public static File[] listBackups(File backupPath) {

        if (!backupPath.isDirectory() || !backupPath.canRead()) {
            return new File[0];
        }
        File[] files = backupPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (new File(dir, filename)).isFile() && filename.endsWith(".backup");
            }
        });

        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File s1, File s2) {
                    return s2.getName().compareTo(s1.getName());
                }
            });
            return files;
        } else {
            return new File[0];
        }
    }
}
