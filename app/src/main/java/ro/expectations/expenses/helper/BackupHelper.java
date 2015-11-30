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
