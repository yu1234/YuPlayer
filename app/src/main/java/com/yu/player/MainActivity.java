package com.yu.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.exoplayer.activity.PlayerActivity;
import com.yu.player.utils.ReadFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {
    private long fileMinSize = 1024 * 1024 * 10;
    private final static String TAG = MainActivity.class.getSimpleName();
    private ReadFileUtil readFileUtil;
    private List<File> files = new ArrayList<File>();
    /**
     * 图片地址
     */
    private Uri imageUrl = Uri.parse("http://avatar.csdn.net/4/E/8/1_y1scp.jpg");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        readFileUtil = ReadFileUtil.getInstance();

        readFileUtil.getFiles(ReadFileUtil.FileType.VOIDEO, subscriber);
        SimpleDraweeView simpleDraweeView= (SimpleDraweeView) this.findViewById(R.id.SimpleDraweeView);
        //构建Controller
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                //设置需要下载的图片地址
                .setUri(imageUrl)
                //设置点击重试是否开启
                .setTapToRetryEnabled(true)
                //构建
                .build();

        //获取GenericDraweeHierarchy对象
        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                //设置圆形圆角参数
                //.setRoundingParams(rp)
                //设置圆角半径
                .setRoundingParams(RoundingParams.fromCornersRadius(200))
                //设置圆形圆角参数；RoundingParams.asCircle()是将图像设置成圆形
                //.setRoundingParams(RoundingParams.asCircle())
                //设置淡入淡出动画持续时间(单位：毫秒ms)
                .setFadeDuration(5000)
                //构建
                .build();
       // simpleDraweeView.setHierarchy(hierarchy);
        //simpleDraweeView.setController(controller);

       /* Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);*/
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

    Subscriber<File> subscriber = new Subscriber<File>() {
        @Override
        public void onNext(File s) {
            if (ObjectUtil.isNotNull(s)) {
                if (fileMinSize < s.length()) {
                    files.add(s);
                }

            }

        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "Subscriber.onCompleted:" + files.size() + ":======files.size()======");
            for (File file : files) {
                Log.i(TAG, "Subscriber.onCompleted:" + file.getName());
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "Subscriber.onError", e);
        }
    };
}
