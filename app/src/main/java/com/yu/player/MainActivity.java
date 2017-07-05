package com.yu.player;

import android.Manifest;
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
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wang.avi.AVLoadingIndicatorView;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yu.exoplayer.R2;
import com.yu.exoplayer.activity.PlayerActivity;
import com.yu.exoplayer.media.ui.VideoPlayerView;
import com.yu.player.Impl.ScanFileCompletedImpl;
import com.yu.player.adapter.VideoRecyclerViewAdapter;
import com.yu.player.bean.VideoFile;
import com.yu.player.utils.CacheUtils;
import com.yu.player.utils.FileUtils;
import com.yu.player.utils.ReadFileUtil;
import com.yu.player.utils.VideoSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements ScanFileCompletedImpl<VideoFile> {

    private final static String TAG = MainActivity.class.getSimpleName();
    private List<VideoFile> videoFiles = new ArrayList<VideoFile>();

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


        //获取权限
        AndPermission.with(this).requestCode(100).permission(Manifest.permission.READ_EXTERNAL_STORAGE).callback(this).start();
    }


    // 成功回调的方法，用注解即可，这里的100就是请求时的requestCode。
    @PermissionYes(100)
    private void getPermissionYes(List<String> grantedPermissions) {
        // TODO 申请权限成功。
        init();
    }

    @PermissionNo(100)
    private void getPermissionNo(List<String> deniedPermissions) {
        // TODO 申请权限失败。
        this.finish();
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
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        init();

    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    //初始化
    private void init() {
        //获取视频列表缓存
        videoFiles = CacheUtils.getVideoFileCache(this);
        if (CollectionUtil.isEmpty(videoFiles)) {
            loading.show();
        } else {
            showList();
        }
        //扫描视频文件
        VideoSubscriber<File> videoSubscriber = VideoSubscriber.getInstance(this);
        videoSubscriber.setScanFileCompleted(this);
        videoSubscriber.startScan();
    }

    //显示列表
    private void showList() {
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


    @Override
    public void onScanCompleted(List<VideoFile> videoFiles) {
        this.videoFiles = videoFiles;
        //重新渲染页面
        Message msg = mHandler.obtainMessage();
        msg.what = 1;
        msg.sendToTarget();
    }
}
