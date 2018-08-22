package com.halove.xyp.apilibrary.utils;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class FileUtil {

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    //解决Android 5.0以下delete之后又mkdirs总是false的系统bug
    public static void renameAndDelete(File fileOrDirectory) {
        File newFile = new File(fileOrDirectory.getParent() + File.separator
                + "_" + fileOrDirectory.getName());
        fileOrDirectory.renameTo(newFile);
        delete(newFile);
    }

    public static void delete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                delete(child);
        fileOrDirectory.delete();
    }

    public static File getCacheFileByType(Context context, String type) {
        File file;
        if (existSDCard()) {
            file = ContextCompat.getExternalCacheDirs(context)[0];
        } else {
            file = context.getCacheDir();
        }
        file = new File(file.getAbsolutePath() + File.separator + type);
        return file;
    }

    public static File getFileByType(Context context, String type){
        File file;
        if (existSDCard()){
            file = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS)[0];
        }else {
            file = context.getFilesDir();
        }
        file = new File(file.getAbsolutePath() + File.separator + type);
        return file;
    }

    public static long getFolderSize(File file) {
        long size = 0;
        if (file == null)
            return 0;
        File[] files = file.listFiles();
        if (files == null || file.length() == 0)
            return 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                size += getFolderSize(files[i]);
            } else {
                size += files[i].length();
            }
        }
        return size;
    }

    public static String getFileSize(long size) {
        DecimalFormat df = new DecimalFormat("###.##");
        float f = ((float) size / (float) (1024 * 1024));
        if (f < 1.0) {
            float f2 = ((float) size / (float) (1024));
            return df.format(Float.valueOf(f2).doubleValue()) + "KB";
        } else {
            return df.format(Float.valueOf(f).doubleValue()) + "M";
        }
    }

    private static void unzip(InputStream inputStream, String targetDir){
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        String strEntry; //保存每个zip的条目名称

        try {
            BufferedOutputStream dest = null; //缓冲输出流
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry; //每个zip条目的实例


            while ((entry = zis.getNextEntry()) != null) {


                try {
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();


                    File entryFile = new File(targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }
                    if (entry.isDirectory() && !entryFile.exists()) {
                        entryFile.mkdir();
                        continue;
                    }


                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
        }
    }

    public static void unzipFile(String zipFile, String targetDir) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(zipFile);
            unzip(new BufferedInputStream(fis),targetDir);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void unzipFile(InputStream inputStream, String targetDir){
        unzip(inputStream,targetDir);
    }

    public static void saveFile(InputStream inputStream, String path, String fileName) {
        if (inputStream == null) {
            return;
        }
        File saveFile = new File((path.endsWith(File.separator) ? path : path + File.separator) + fileName);
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        try {
            FileOutputStream out = new FileOutputStream(saveFile);
            byte[] buf = new byte[1024];
            int ch;
            while ((ch = inputStream.read(buf)) != -1) {
                out.write(buf, 0, ch);
            }
            out.flush();
            out.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String read(InputStream is) {
        if (is != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取文件下的文件
     * @param dir      那个文件夹下
     * @param fileArray  文件集合路劲
     * @param fileFilter 文件过滤器
     * @throws IOException
     */
    public static void listFileByFilter(File dir, ArrayList<String> fileArray, FileFilter fileFilter)
            throws IOException {
        File[] files = dir.listFiles(fileFilter);
        if (files != null && files.length > 0) {
            for (File file : files) {
                if(file.getName().equals(".thumbnails"))continue;
                if (file.isDirectory()) {
                    listFileByFilter(file, fileArray, fileFilter);
                } else {
                    fileArray.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
