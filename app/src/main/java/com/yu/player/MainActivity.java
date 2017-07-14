package com.yu.player;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yu.player.Impl.ScanFileCompletedImpl;
import com.yu.player.adapter.VideoRecyclerViewAdapter;
import com.yu.player.bean.VideoFile;
import com.yu.player.utils.CacheUtils;
import com.yu.player.utils.VideoSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ScanFileCompletedImpl<VideoFile>, PullLoadMoreRecyclerView.PullLoadMoreListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private List<VideoFile> videoFiles = new ArrayList<VideoFile>();
    private Thread databaseThread;

    /**
     * =================控件注入 start=====================
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.recycler_view)
    PullLoadMoreRecyclerView pullLoadMoreRecyclerView;
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
        toolbar.setTitle("视频");
        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        pullLoadMoreRecyclerView.setLinearLayout();
        pullLoadMoreRecyclerView.setOnPullLoadMoreListener(this);

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
        init();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "下拉刷新");
        scanFile();
    }

    @Override
    public void onLoadMore() {

    }

    //初始化
    private void init() {
        //获取视频列表缓存
        this.databaseThread = new Thread(new DatabaseRunnable());
        this.databaseThread.start();

    }

    //显示列表
    private void showList() {
        Log.i(TAG, "showList");
        pullLoadMoreRecyclerView.setPullLoadMoreCompleted();
        loading.hide();
        pullLoadMoreRecyclerView.setAdapter(new VideoRecyclerViewAdapter(MainActivity.this, videoFiles));
    }


    /**
     * 扫描完成回调
     *
     * @param videoFiles
     */
    @Override
    public void onScanCompleted(List<VideoFile> videoFiles) {
        Log.i(TAG, "扫描完成回调");
        this.videoFiles = videoFiles;
        //重新渲染页面
        mHandler.sendEmptyMessage(1);
    }

    public void scanFile() {
        //扫描视频文件
        VideoSubscriber<File> videoSubscriber = VideoSubscriber.getInstance(MainActivity.this);
        videoSubscriber.setScanFileCompleted(MainActivity.this);
        videoSubscriber.startScan();
    }

    //=====================异步操作==============================
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                showList();
            }
            super.handleMessage(msg);
        }
    };


    public class DatabaseRunnable implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "DatabaseRunnable.run");
            videoFiles = CacheUtils.getVideoFileCache(MainActivity.this);
            if (CollectionUtil.isEmpty(videoFiles)) {
                loading.show();
            } else {
                mHandler.sendEmptyMessage(1);
            }
            scanFile();
            if (ObjectUtil.isNotNull(databaseThread)) {
                databaseThread.interrupt();
            }
        }
    }
}
