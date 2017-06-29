package com.yu.exoplayer.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.gyf.barlibrary.ImmersionBar;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.exoplayer.R;
import com.yu.exoplayer.R2;
import com.yu.exoplayer.log.EventLogger;
import com.yu.exoplayer.media.ui.VideoPlayerView;
import com.yu.exoplayer.utils.PlayerUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.targetSdkVersion;


public class PlayerActivity extends AppCompatActivity implements ExoPlayer.EventListener, AudioManager.OnAudioFocusChangeListener {
    private final static String TAG = PlayerActivity.class.getSimpleName();
    private static PlayerActivity instance;
    protected String userAgent;
    /**
     * =================控件注入 start=====================
     */
    @BindView(R2.id.videoPlayerView)
    VideoPlayerView videoPlayerView;
    @BindView(R2.id.toolbar_title)
    TextView toolbarTitle;
    /**
     * =================控件注入 end=====================
     */
    private SimpleExoPlayer simpleExoPlayer;
    /**
     * 播放器设置
     */
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private EventLogger eventLogger;
    private DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;

    private AudioManager audioManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        instance = this;
        shouldAutoPlay = true;
        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        setContentView(R.layout.player_main);
        ButterKnife.bind(this);
        videoPlayerView.requestFocus();
        this.audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

    }

    public PlayerActivity getInstance() {
        return instance;
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            requestPermissions();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || simpleExoPlayer == null)) {
            requestPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        ImmersionBar.with(this).destroy();
        if (ObjectUtil.isNotNull(this.audioManager)) {
            this.audioManager.abandonAudioFocus(this);
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.getPlayWhenReady();
            updateResumePosition();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
            trackSelector = null;
            eventLogger = null;
        }
    }
    // Activity input

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        videoPlayerView.showController();
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || videoPlayerView.dispatchMediaKeyEvent(event);
    }

    /**
     * 初始化player
     */
    private void initPlayer() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (ObjectUtil.isNotNull(uri)) {
            boolean needNewPlayer = simpleExoPlayer == null;
            if (needNewPlayer) {
                TrackSelection.Factory adaptiveTrackSelectionFactory =
                        new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
                trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
                eventLogger = new EventLogger(trackSelector);

                DefaultRenderersFactory rendersFactory = new DefaultRenderersFactory(this,
                        null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

                simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(rendersFactory, trackSelector);
                simpleExoPlayer.addListener(this);
                simpleExoPlayer.addListener(eventLogger);
                simpleExoPlayer.setAudioDebugListener(eventLogger);
                simpleExoPlayer.setVideoDebugListener(eventLogger);
                simpleExoPlayer.setMetadataOutput(eventLogger);

                videoPlayerView.setPlayer(simpleExoPlayer);
                simpleExoPlayer.setPlayWhenReady(shouldAutoPlay);
            }
            if (needNewPlayer) {
               String title= PlayerUtil.getTitle(this,uri);
                toolbarTitle.setText(title);
                Uri[] uris;
                uris = new Uri[]{uri};
                if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                    // The player will be reinitialized if the permission is granted.
                    return;
                }
                MediaSource[] mediaSources = new MediaSource[uris.length];
                for (int i = 0; i < uris.length; i++) {
                    mediaSources[i] = buildMediaSource(uris[i], null);
                }
                MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                        : new ConcatenatingMediaSource(mediaSources);
                boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
                if (haveResumePosition) {
                    simpleExoPlayer.seekTo(resumeWindow, resumePosition);
                }
                if (this.requestAudioFocus()) {
                    simpleExoPlayer.prepare(mediaSource, !haveResumePosition, false);
                }

            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            requestPermissions();

        }
    }

    private void requestPermissions() {
        if (this.selfPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            initPlayer();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    public boolean selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = this.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(this, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, ev.getActionMasked() + "=onTouchEvent");
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "onTouchEvent=" + ev.getActionMasked());
        }
        return true;
    }

    private void updateResumePosition() {
        resumeWindow = simpleExoPlayer.getCurrentWindowIndex();
        resumePosition = simpleExoPlayer.isCurrentWindowSeekable() ? Math.max(0, simpleExoPlayer.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return this.buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * =========================================播放时间回调==========================================================
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.i(TAG, "player:onTimelineChanged");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.i(TAG, "player:onTracksChanged");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.i(TAG, "player:onLoadingChanged");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.i(TAG, "player:onPlayerStateChanged:" + "playWhenReady=" + playWhenReady + ":playbackState=" + playbackState);
        if (playbackState == 4 && ObjectUtil.isNotNull(this.audioManager)) {
            this.audioManager.abandonAudioFocus(this);
        } else if (playWhenReady && playbackState == 3) {
            this.requestAudioFocus();
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.i(TAG, "player:onPlayerError");
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initPlayer();
        } else {
            updateResumePosition();
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        Log.i(TAG, "player:onPositionDiscontinuity");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.i(TAG, "player:onPlaybackParametersChanged");
    }

    /**
     * =========================================播放时间回调==========================================================
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    //请求音频焦点
    private boolean requestAudioFocus() {
        if (ObjectUtil.isNotNull(this.audioManager)) {
            // Request audio focus for playback
            int result = this.audioManager.requestAudioFocus(this,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                //长时间丢失焦点,当其他应用申请的焦点为AUDIOFOCUS_GAIN时，
                //会触发此回调事件，例如播放QQ音乐，网易云音乐等
                //通常需要暂停音乐播放，若没有暂停播放就会出现和其他音乐同时输出声音
                Log.d(TAG, "AUDIOFOCUS_LOSS");
                //释放焦点，该方法可根据需要来决定是否调用
                if (ObjectUtil.isNotNull(this.simpleExoPlayer)) {
                    this.simpleExoPlayer.setPlayWhenReady(false);
                }
                //若焦点释放掉之后，将不会再自动获得
                this.audioManager.abandonAudioFocus(this);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //短暂性丢失焦点，当其他应用申请AUDIOFOCUS_GAIN_TRANSIENT或AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE时，
                //会触发此回调事件，例如播放短视频，拨打电话等。
                //通常需要暂停音乐播放
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                if (ObjectUtil.isNotNull(this.simpleExoPlayer)) {
                    this.simpleExoPlayer.setPlayWhenReady(false);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //短暂性丢失焦点并作降音处理
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                //当其他应用申请焦点之后又释放焦点会触发此回调
                //可重新播放音乐
                Log.d(TAG, "AUDIOFOCUS_GAIN");
                if (ObjectUtil.isNotNull(this.simpleExoPlayer)) {
                    this.simpleExoPlayer.setPlayWhenReady(true);
                }
                break;
        }
    }
}
