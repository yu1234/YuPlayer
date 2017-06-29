package com.yu.player;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wang.avi.AVLoadingIndicatorView;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.exoplayer.R2;
import com.yu.exoplayer.activity.PlayerActivity;
import com.yu.exoplayer.media.ui.VideoPlayerView;
import com.yu.player.adapter.VideoRecyclerViewAdapter;
import com.yu.player.bean.VideoFile;
import com.yu.player.utils.FileUtils;
import com.yu.player.utils.ReadFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements MediaScannerConnectionClient {

    private final static String TAG = MainActivity.class.getSimpleName();
    private ReadFileUtil readFileUtil;
    private List<File> files = new ArrayList<File>();
    private int index = 0;
    private List<VideoFile> videoFiles = new ArrayList<VideoFile>();
    private MediaScannerConnection mediaScannerConnection;
    /**
     * =================控件注入 start=====================
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading)
    AVLoadingIndicatorView loading;

    /**
     * =================控件注入 end=====================
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));//这里用线性显示 类似于listview

        //实例化一个MediaScannerConnection
        mediaScannerConnection = new MediaScannerConnection(MainActivity.this, this);
        scanVideoFile();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMediaScannerConnected() {

    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        index++;
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (ObjectUtil.isNotNull(cursor)) {
            while (cursor.moveToNext()) {
                //获取id
                long mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
                //获取名称
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE));
                //获取大小
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));
                String formatSize = FileUtils.formatFileSize(size);
                //获取时长
                String formatDuration = FileUtils.formatDuration(this, uri);
                //获取mime_type
                String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
                //获取缩略图
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                VideoFile videoFile = new VideoFile();
                videoFile.setmId(mId);
                videoFile.setName(name);
                videoFile.setFormatSize(formatSize);
                videoFile.setFormatDuration(formatDuration);
                videoFile.setMimeType(mimeType);
                videoFile.setThumbnails(thumbnail);
                videoFile.setUri(uri);
                this.videoFiles.add(videoFile);
            }
        }
        if (index >= files.size()) {
            Message msg = mHandler.obtainMessage();
            msg.what = 1;
            msg.sendToTarget();
        }
    }

    //视频文件扫描
    private void scanVideoFile() {
        //初始化缓存变量
        files = new ArrayList<File>();
        videoFiles = new ArrayList<VideoFile>();
        index = 0;
        if (!mediaScannerConnection.isConnected()) {
            mediaScannerConnection.connect();
        }
        loading.show();
        //开始扫描
        readFileUtil = ReadFileUtil.getInstance();
        readFileUtil.getFiles(ReadFileUtil.FileType.VOIDEO, subscriber);
    }

    //视频文件扫描回调
    Subscriber<File> subscriber = new Subscriber<File>() {
        @Override
        public void onNext(File file) {
            Log.i(TAG, "Subscriber.onNext");
            if (ObjectUtil.isNotNull(file)) {
                if (ObjectUtil.isNotNull(files)) {
                    files.add(file);

                }
            }

        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "Subscriber.onCompleted");
            if (CollectionUtil.isNotEmpty(files)) {
                for (File file : files) {
                    if (mediaScannerConnection.isConnected()) {
                        mediaScannerConnection.scanFile(file.getAbsolutePath(), null);
                    }
                }
            } else {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.sendToTarget();
            }

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "Subscriber.onError", e);
        }
    };

    //显示列表
    private void showList() {
        if (mediaScannerConnection.isConnected()) {
            mediaScannerConnection.disconnect();
        }
        loading.hide();
        mRecyclerView.setAdapter(new VideoRecyclerViewAdapter(MainActivity.this, videoFiles));
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                showList();
            }
            super.handleMessage(msg);
        }
    };
}
