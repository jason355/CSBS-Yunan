package com.hfad.swbs;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.DoNotInline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Handler;

public class FileManager {
    public static String deleteOldApkFiles(Context context, String fileName) {
        File file = new File(context.getExternalFilesDir(null), fileName);

        if (file.exists()) {
            boolean get = file.delete();
            if (get) {
                return "1";
            }
        }
        return  "0";
    }
    public static int getDownloadProgress(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        // 创建查询对象
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        // 执行查询并获取 Cursor
        Cursor cursor = downloadManager.query(query);
        if (cursor != null && cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int totalSizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);

            int status = cursor.getInt(statusIndex);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                // 下载完成，处理相应逻辑
                return 100;
            } else if (status == DownloadManager.STATUS_FAILED) {
                // 下载失败，处理相应逻辑
                return -1;
            } else if (status == DownloadManager.STATUS_RUNNING) {
                long totalSize = cursor.getLong(totalSizeIndex);
                long downloaded = cursor.getLong(downloadedIndex);

                // 计算下载进度
                int progress = (int) (downloaded * 100 / totalSize);

                // 在此处处理下载进度，例如更新 UI 界面
                Log.d("Download Progress", "Progress: " + progress + "%");
                return progress;
            }
        }
        return 0;
    }


}
