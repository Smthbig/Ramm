package com.ramm;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.*;
import java.net.URLDecoder;
import java.util.Objects;

public class FileUtil {

    public static boolean isExistFile(String path) {
        return path != null && new File(path).exists();
    }

    public static void makeDir(String path) {
        if (!isExistFile(path)) {
            new File(path).mkdirs();
        }
    }

    public static String getExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String readFile(String path) {
        StringBuilder result = new StringBuilder();
        try (FileReader fr = new FileReader(path)) {
            char[] buffer = new char[1024];
            int len;
            while ((len = fr.read(buffer)) != -1) {
                result.append(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void writeFile(String path, String content) {
        makeDir(new File(path).getParent());
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(String from, String to) {
        if (!isExistFile(from)) return;
        makeDir(new File(to).getParent());
        try (InputStream in = new FileInputStream(from);
             OutputStream out = new FileOutputStream(to)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void moveFile(String from, String to) {
        copyFile(from, to);
        deleteFile(from);
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File sub : children) {
                    deleteFile(sub.getAbsolutePath());
                }
            }
        }
        file.delete();
    }

    public static String convertUriToFilePath(Context context, Uri uri) {
        String path = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                path = Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if ("raw".equalsIgnoreCase(type)) {
                return split[1];
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    path = cursor.getString(index);
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }

        if (path != null) {
            try {
                return URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}