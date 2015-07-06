package com.myMinistry.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {
    public static void copyFile(File fromFile, File toFile) throws IOException {
        FileInputStream fromFileStream = new FileInputStream(fromFile);
        FileOutputStream toFileStream = new FileOutputStream(toFile);
        FileChannel fromChannel = fromFileStream.getChannel();
        FileChannel toChannel = toFileStream.getChannel();
        try {
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            if (fromChannel != null)
                fromChannel.close();
            if (toChannel != null)
                toChannel.close();

            fromFileStream.close();
            toFileStream.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalDBFile(Context context, String fileName) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return new File(context.getExternalFilesDir(null), fileName);
        }
        else {
            File[] extDBPath = ContextCompat.getExternalFilesDirs(context, null);
            if(extDBPath != null) {
                if(!extDBPath[0].exists()) {
                    extDBPath[0].mkdirs();
                }
                return new File(extDBPath[0], fileName);
            }
        }
        return null;
    }
}