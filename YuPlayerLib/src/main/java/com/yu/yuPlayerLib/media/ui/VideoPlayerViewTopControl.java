package com.yu.yuPlayerLib.media.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.C;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.yuPlayerLib.R;
import com.yu.yuPlayerLib.R2;
import com.yu.yuPlayerLib.activity.PlayerActivity;
import com.yu.yuPlayerLib.media.impl.ControllerVisibilityListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/6/21 0021.
 */

public class VideoPlayerViewTopControl extends FrameLayout implements View.OnClickListener{
    private final static String TAG=VideoPlayerViewTopControl.class.getSimpleName();
    /**
     * =======================注入控件 start=====================
     */
    @BindView(R2.id.toolbar_back)
    ImageView toolbarBack;
    @BindView(R2.id.orientation_overlay_button)
    LinearLayout orientationOverlayButton;
    @BindView(R2.id.toolbar)
    Toolbar toolbar;
    /**
     * =======================注入控件 end=====================
     */
    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;

    private int showTimeoutMs;
    private long hideAtMs;
    private boolean isAttachedToWindow;
    private Activity activity;

    public VideoPlayerViewTopControl(@NonNull Context context) {
        this(context, null);
    }

    public VideoPlayerViewTopControl(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayerViewTopControl(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int controllerLayoutId = R.layout.video_player_top_control;
        View view = LayoutInflater.from(context).inflate(controllerLayoutId, this);
        this.activity= (Activity) context;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
        ButterKnife.bind(this,view);
        EventBus.getDefault().register(this);
        toolbarBack.setOnClickListener(this);
        orientationOverlayButton.setOnClickListener(this);
        ImmersionBar.with(this.activity).titleBar(toolbar).hideBar(BarHide.FLAG_HIDE_BAR).init();
    }


    /**
     * Shows the playback controls. If  is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    public void show() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            ImmersionBar.with(this.activity).transparentStatusBar().titleBar(toolbar).hideBar(BarHide.FLAG_SHOW_BAR).init();

        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
    public void hide() {
        if (isVisible()) {
            setVisibility(GONE);
            ImmersionBar.with(this.activity).titleBar(toolbar).hideBar(BarHide.FLAG_HIDE_BAR).init();
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        removeCallbacks(hideAction);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ControllerVisibilityListener listener) {
        if (ObjectUtil.isNotNull(listener)) {
            if (View.VISIBLE == listener.getVisibility()) {
                this.show();
            } else {
                this.hide();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(ObjectUtil.isNotNull(activity)){
            if(v.getId()== R.id.toolbar_back){
                this.activity.finish();
            }else if(v.getId()== R.id.orientation_overlay_button){
                Configuration cf =  this.activity.getResources().getConfiguration(); //获取设置的配置信息
                int ori = cf.orientation; //获取屏幕方向
                if (ori == cf.ORIENTATION_LANDSCAPE) {
                    this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                } else if (ori == cf.ORIENTATION_PORTRAIT) {
                    this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        }
    }
}
