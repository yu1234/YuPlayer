package com.yu.exoplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.exoplayer.activity.PlayerActivity;

/**
 * Created by igreentree on 2017/6/29 0029.
 */

public class PlayerUtil {
    public static void playVideo(Context context, Uri uri) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static String getTitle(Context context, Uri uri) {
        String[] filePathColumn = {MediaStore.MediaColumns.TITLE};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        String title = "";
        if (ObjectUtil.isNotNull(cursor) && cursor.moveToFirst()) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            title = cursor.getString(columnIndex);
            cursor.close();
        }
        return title;
    }
}
