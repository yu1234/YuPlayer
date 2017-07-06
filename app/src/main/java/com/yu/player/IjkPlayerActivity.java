package com.yu.player;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yu.ijkplayer.*;

import java.util.List;

/**
 * Created by igreentree on 2017/7/6 0006.
 */

public class IjkPlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取权限
        AndPermission.with(this).requestCode(100).permission(Manifest.permission.READ_EXTERNAL_STORAGE).callback(this).start();

    }
    // 成功回调的方法，用注解即可，这里的100就是请求时的requestCode。
    @PermissionYes(100)
    private void getPermissionYes(List<String> grantedPermissions) {
        // TODO 申请权限成功。
        Intent intent=new Intent(this, com.yu.ijkplayer.MainActivity.class);
        this.startActivity(intent);
    }

    @PermissionNo(100)
    private void getPermissionNo(List<String> deniedPermissions) {
        // TODO 申请权限失败。
        this.finish();
    }
}
