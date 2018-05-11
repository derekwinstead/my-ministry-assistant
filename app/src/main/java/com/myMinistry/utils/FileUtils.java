package com.myMinistry.utils;

import android.content.Context;

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

    public static File getExternalDBFile(Context context, String fileName) {
        return new File(context.getExternalFilesDir(null), fileName);
    }
}