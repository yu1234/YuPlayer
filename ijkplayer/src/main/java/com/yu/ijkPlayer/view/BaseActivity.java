package com.yu.ijkPlayer.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.yu.ijkPlayer.R2;
import com.yu.ijkPlayer.bean.enumBean.EventBusCode;
import com.yu.ijkPlayer.bean.enumBean.PlayerListenerEnum;
import com.yu.ijkPlayer.utils.ScreenRotateUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by igreentree on 2017/7/17 0017.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus注册
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
