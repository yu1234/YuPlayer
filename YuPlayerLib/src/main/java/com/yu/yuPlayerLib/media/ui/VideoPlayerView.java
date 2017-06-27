package com.yu.yuPlayerLib.media.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.yuPlayerLib.R;
import com.yu.yuPlayerLib.R2;
import com.yu.yuPlayerLib.media.impl.ControlDispatcher;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/6/19 0019.
 */

public class VideoPlayerView extends FrameLayout {
    /**
     * =================控件注入 start=====================
     */
    @BindView(R2.id.scrollProcessView)
    LinearLayout scrollProcessView;
    @BindView(R2.id.currentProcessText)
    TextView currentProcessText;
    @BindView(R2.id.changeProcessText)
    TextView changeProcessText;
    @BindView(R2.id.operation_volume_brightness)
    FrameLayout operationVolumeOrBrightness;
    @BindView(R2.id.operation_bg)
    ImageView mOperationBg;
    @BindView(R2.id.operation_full)
    ImageView operationFull;
    @BindView(R2.id.operation_percent)
    ImageView mOperationPercent;
    /**
     * =================控件注入 end=====================
     */
    private final static String TAG = VideoPlayerView.class.getSimpleName();
    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private final AspectRatioFrameLayout contentFrame;
    private final View shutterView;
    private final View surfaceView;
    private final ImageView artworkView;
    private final SubtitleView subtitleView;
    private final VideoPlayerViewBottomControl controllerBottom;
    private final VideoPlayerViewTopControl controllerTop;
    private final VideoPlayerView.ComponentListener componentListener;
    private final FrameLayout overlayFrameLayout;
    private final TextView toolbarTitle;

    private SimpleExoPlayer player;
    private boolean useController;
    private boolean useArtwork;
    private Bitmap defaultArtwork;
    private int controllerShowTimeoutMs;
    private boolean controllerHideOnTouch;
    private GestureDetector gestureDetector;
    private static final float FLIP_DISTANCE = 50;

    private static boolean isScroll;

    private long resumePosition;

    public AudioManager audioManager;
    private int maxVolume, currentVolume = -1, height, width;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    private Activity activity;

    public VideoPlayerView(Context context) {
        this(context, null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
        if (isInEditMode()) {
            contentFrame = null;
            shutterView = null;
            surfaceView = null;
            artworkView = null;
            subtitleView = null;
            controllerBottom = null;
            controllerTop = null;
            componentListener = null;
            overlayFrameLayout = null;
            toolbarTitle = null;
            ImageView logo = new ImageView(context, attrs);
            if (Util.SDK_INT >= 23) {
                configureEditModeLogoV23(getResources(), logo);
            } else {
                configureEditModeLogo(getResources(), logo);
            }
            addView(logo);
            return;
        }

        int playerLayoutId = R.layout.video_player_view;
        boolean useArtwork = true;
        int defaultArtworkId = 0;
        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = VideoPlayerViewBottomControl.DEFAULT_SHOW_TIMEOUT_MS;
        boolean controllerHideOnTouch = true;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView, 0, 0);
            try {
                playerLayoutId = a.getResourceId(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_player_layout_id,
                        playerLayoutId);
                useArtwork = a.getBoolean(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_use_artwork, useArtwork);
                defaultArtworkId = a.getResourceId(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_default_artwork,
                        defaultArtworkId);
                useController = a.getBoolean(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_use_controller, useController);
                surfaceType = a.getInt(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_surface_type, surfaceType);
                resizeMode = a.getInt(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_resize_mode, resizeMode);
                controllerShowTimeoutMs = a.getInt(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_show_timeout,
                        controllerShowTimeoutMs);
                controllerHideOnTouch = a.getBoolean(com.google.android.exoplayer2.ui.R.styleable.SimpleExoPlayerView_hide_on_touch,
                        controllerHideOnTouch);
            } finally {
                a.recycle();
            }
        }

        View view = LayoutInflater.from(context).inflate(playerLayoutId, this);
        ButterKnife.bind(this, view);
        componentListener = new VideoPlayerView.ComponentListener();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = (AspectRatioFrameLayout) findViewById(com.google.android.exoplayer2.ui.R.id.exo_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }

        // Shutter view.
        shutterView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_shutter);

        // Create a surface view and insert it into the content frame, if there is one.
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView = surfaceType == SURFACE_TYPE_TEXTURE_VIEW ? new TextureView(context)
                    : new SurfaceView(context);
            surfaceView.setLayoutParams(params);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }

        // Overlay frame layout.
        overlayFrameLayout = (FrameLayout) findViewById(R.id.exo_overlay);

        // Artwork view.
        artworkView = (ImageView) findViewById(com.google.android.exoplayer2.ui.R.id.exo_artwork);
        this.useArtwork = useArtwork && artworkView != null;
        if (defaultArtworkId != 0) {
            defaultArtwork = BitmapFactory.decodeResource(context.getResources(), defaultArtworkId);
        }

        // Subtitle view.
        subtitleView = (SubtitleView) findViewById(com.google.android.exoplayer2.ui.R.id.exo_subtitles);
        if (subtitleView != null) {
            subtitleView.setUserDefaultStyle();
            subtitleView.setUserDefaultTextSize();
        }

        // Playback control view.
        this.controllerBottom = (VideoPlayerViewBottomControl) findViewById(R.id.videoPlayerViewBottomControl);
        this.controllerTop = (VideoPlayerViewTopControl) findViewById(R.id.videoPlayerViewTopControl);
        this.toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        this.controllerShowTimeoutMs = controllerBottom != null ? controllerShowTimeoutMs : 0;
        this.controllerHideOnTouch = controllerHideOnTouch;
        this.useController = useController && controllerBottom != null;
        hideController();
        //手势监听注册
        gestureDetector = new GestureDetector(view.getContext(), new MySimpleOnGestureListener());
        //音量调节
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        //获取屏幕高度
        WindowManager wm = (WindowManager) this.activity.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        this.width = point.x;//屏幕宽度
        this.height = point.y;//屏幕高度

        Log.i(TAG, operationFull.getWidth() + "==  operationFull.getLayoutParams().width ===");

    }

    /**
     * Switches the view targeted by a given {@link SimpleExoPlayer}.
     *
     * @param player        The player whose target view is being switched.
     * @param oldPlayerView The old view to detach from the player.
     * @param newPlayerView The new view to attach to the player.
     */
    public static void switchTargetView(@NonNull SimpleExoPlayer player,
                                        @Nullable SimpleExoPlayerView oldPlayerView, @Nullable SimpleExoPlayerView newPlayerView) {
        if (oldPlayerView == newPlayerView) {
            return;
        }
        // We attach the new view before detaching the old one because this ordering allows the player
        // to swap directly from one surface to another, without transitioning through a state where no
        // surface is attached. This is significantly more efficient and achieves a more seamless
        // transition when using platform provided video decoders.
        if (newPlayerView != null) {
            newPlayerView.setPlayer(player);
        }
        if (oldPlayerView != null) {
            oldPlayerView.setPlayer(null);
        }
    }

    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    public SimpleExoPlayer getPlayer() {
        return player;
    }

    /**
     * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
     * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and previous
     * assignments are overridden.
     * <p>
     * To transition a {@link SimpleExoPlayer} from targeting one view to another, it's recommended to
     * use {@link #switchTargetView(SimpleExoPlayer, SimpleExoPlayerView, SimpleExoPlayerView)} rather
     * than this method. If you do wish to use this method directly, be sure to attach the player to
     * the new view <em>before</em> calling {@code setPlayer(null)} to detach it from the old one.
     * This ordering is significantly more efficient and may allow for more seamless transitions.
     *
     * @param player The {@link SimpleExoPlayer} to use.
     */
    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
            this.player.clearTextOutput(componentListener);
            this.player.clearVideoListener(componentListener);
            if (surfaceView instanceof TextureView) {
                this.player.clearVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                this.player.clearVideoSurfaceView((SurfaceView) surfaceView);
            }
        }
        this.player = player;
        if (useController) {
            controllerBottom.setPlayer(player);
        }
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.setVideoListener(componentListener);
            player.setTextOutput(componentListener);
            player.addListener(componentListener);
            maybeShowController(false);
            updateForCurrentTrackSelections();
        } else {
            hideController();
            hideArtwork();
        }
    }

    /**
     * Sets the resize mode.
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@AspectRatioFrameLayout.ResizeMode int resizeMode) {
        Assertions.checkState(contentFrame != null);
        contentFrame.setResizeMode(resizeMode);
    }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    public boolean getUseArtwork() {
        return useArtwork;
    }

    /**
     * Sets whether artwork is displayed if present in the media.
     *
     * @param useArtwork Whether artwork is displayed.
     */
    public void setUseArtwork(boolean useArtwork) {
        Assertions.checkState(!useArtwork || artworkView != null);
        if (this.useArtwork != useArtwork) {
            this.useArtwork = useArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns the default artwork to display.
     */
    public Bitmap getDefaultArtwork() {
        return defaultArtwork;
    }

    /**
     * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
     * present in the media.
     *
     * @param defaultArtwork the default artwork to display.
     */
    public void setDefaultArtwork(Bitmap defaultArtwork) {
        if (this.defaultArtwork != defaultArtwork) {
            this.defaultArtwork = defaultArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns whether the playback controls can be shown.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether the playback controls can be shown. If set to {@code false} the playback controls
     * are never visible and are disconnected from the player.
     *
     * @param useController Whether the playback controls can be shown.
     */
    public void setUseController(boolean useController) {
        Assertions.checkState(!useController || controllerBottom != null);
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controllerBottom.setPlayer(player);
        } else if (controllerBottom != null) {
            controllerBottom.hide();
            controllerBottom.setPlayer(null);
        }
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController && controllerBottom.dispatchMediaKeyEvent(event);
    }

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     */
    public void showController() {
        if (useController) {
            maybeShowController(true);
        }
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controllerBottom != null) {
            controllerBottom.hide();
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause
     *                                the controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        Assertions.checkState(controllerBottom != null);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
    }

    /**
     * Returns whether the playback controls are hidden by touch events.
     */
    public boolean getControllerHideOnTouch() {
        return controllerHideOnTouch;
    }

    /**
     * Sets whether the playback controls are hidden by touch events.
     *
     * @param controllerHideOnTouch Whether the playback controls are hidden by touch events.
     */
    public void setControllerHideOnTouch(boolean controllerHideOnTouch) {
        Assertions.checkState(controllerBottom != null);
        this.controllerHideOnTouch = controllerHideOnTouch;
    }


    /**
     * Sets the {@link ControlDispatcher}.
     *
     * @param controlDispatcher The {@link ControlDispatcher}, or null to use
     *                          {@link VideoPlayerViewBottomControl#DEFAULT_CONTROL_DISPATCHER}.
     */
    public void setControlDispatcher(ControlDispatcher controlDispatcher) {
        Assertions.checkState(controllerBottom != null);
        controllerBottom.setControlDispatcher(controlDispatcher);
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds.
     */
    public void setRewindIncrementMs(int rewindMs) {
        Assertions.checkState(controllerBottom != null);
        controllerBottom.setRewindIncrementMs(rewindMs);
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        Assertions.checkState(controllerBottom != null);
        controllerBottom.setFastForwardIncrementMs(fastForwardMs);
    }

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one.
     *
     * @param showMultiWindowTimeBar Whether to show all windows.
     */
    public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
        Assertions.checkState(controllerBottom != null);
        controllerBottom.setShowMultiWindowTimeBar(showMultiWindowTimeBar);
    }

    /**
     * Gets the view onto which video is rendered. This is either a {@link SurfaceView} (default)
     * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
     *
     * @return Either a {@link SurfaceView} or a {@link TextureView}.
     */
    public View getVideoSurfaceView() {
        return surfaceView;
    }

    /**
     * Gets the overlay {@link FrameLayout}, which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay {@link FrameLayout}, or {@code null} if the layout has been customized and
     * the overlay is not present.
     */
    public FrameLayout getOverlayFrameLayout() {
        return overlayFrameLayout;
    }

    /**
     * Gets the {@link SubtitleView}.
     *
     * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
     * subtitle view is not present.
     */
    public SubtitleView getSubtitleView() {
        return subtitleView;
    }


    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);

        return true;
    }

    private void maybeShowController(boolean isForced) {
        if (!useController || player == null) {
            return;
        }
        int playbackState = player.getPlaybackState();
        boolean showIndefinitely = playbackState == ExoPlayer.STATE_IDLE
                || playbackState == ExoPlayer.STATE_ENDED || !player.getPlayWhenReady();
        boolean wasShowingIndefinitely = controllerBottom.isVisible() && controllerBottom.getShowTimeoutMs() <= 0;
        controllerBottom.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        if (isForced || showIndefinitely || wasShowingIndefinitely) {
            controllerBottom.show();
        }
    }

    private void updateForCurrentTrackSelections() {
        if (player == null) {
            return;
        }
        TrackSelectionArray selections = player.getCurrentTrackSelections();
        for (int i = 0; i < selections.length; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO && selections.get(i) != null) {
                // Video enabled so artwork must be hidden. If the shutter is closed, it will be opened in
                // onRenderedFirstFrame().
                hideArtwork();
                return;
            }
        }
        // Video disabled so the shutter must be closed.
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        // Display artwork if enabled and available, else hide it.
        if (useArtwork) {
            for (int i = 0; i < selections.length; i++) {
                TrackSelection selection = selections.get(i);
                if (selection != null) {
                    for (int j = 0; j < selection.length(); j++) {
                        Metadata metadata = selection.getFormat(j).metadata;
                        if (metadata != null && setArtworkFromMetadata(metadata)) {
                            return;
                        }
                    }
                }
            }
            if (setArtworkFromBitmap(defaultArtwork)) {
                return;
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork();
    }

    private boolean setArtworkFromMetadata(Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry metadataEntry = metadata.get(i);
            if (metadataEntry instanceof ApicFrame) {
                byte[] bitmapData = ((ApicFrame) metadataEntry).pictureData;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                return setArtworkFromBitmap(bitmap);
            }
        }
        return false;
    }

    private boolean setArtworkFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (bitmapWidth > 0 && bitmapHeight > 0) {
                if (contentFrame != null) {
                    contentFrame.setAspectRatio((float) bitmapWidth / bitmapHeight);
                }
                artworkView.setImageBitmap(bitmap);
                artworkView.setVisibility(VISIBLE);
                return true;
            }
        }
        return false;
    }

    private void hideArtwork() {
        if (artworkView != null) {
            artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
            artworkView.setVisibility(INVISIBLE);
        }
    }

    @TargetApi(23)
    private static void configureEditModeLogoV23(Resources resources, ImageView logo) {
        logo.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_edit_mode_logo, null));
        logo.setBackgroundColor(resources.getColor(com.google.android.exoplayer2.ui.R.color.exo_edit_mode_background_color, null));
    }

    @SuppressWarnings("deprecation")
    private static void configureEditModeLogo(Resources resources, ImageView logo) {
        logo.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_edit_mode_logo));
        logo.setBackgroundColor(resources.getColor(com.google.android.exoplayer2.ui.R.color.exo_edit_mode_background_color));
    }


    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    /**
     * ========================================屏幕手势监听 start===================================================
     */
    class MySimpleOnGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i(TAG, "onScroll");

            if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                Log.i(TAG, "向右滑");
                changeProcessForScroll(e1, e2, false);
                return true;
            }
            if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                Log.i(TAG, "向左滑");
                changeProcessForScroll(e1, e2, true);
                return true;
            }
            if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                if (e1.getX() > (VideoPlayerView.this.getWidth() / 2)) {
                    onVolumeSlide(e1.getY() - e2.getY());
                } else {
                    onBrightnessSlide(e1.getY() - e2.getY());

                }
                return true;
            }
            if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                if (e1.getX() > (VideoPlayerView.this.getWidth() / 2)) {
                    onVolumeSlide(e1.getY() - e2.getY());
                } else {
                    onBrightnessSlide(e1.getY() - e2.getY());
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i(TAG, "onSingleTapConfirmed");
            if (scrollProcessView.getVisibility() == View.VISIBLE) {
                scrollProcessView.setVisibility(View.GONE);
            }
            if (!useController || player == null) {
                return false;
            }
            if (!controllerBottom.isVisible()) {
                maybeShowController(true);
            } else if (controllerHideOnTouch) {
                controllerBottom.hide();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i(TAG, "onDoubleTap");
            if (ObjectUtil.isNotNull(player)) {
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                } else {
                    player.setPlayWhenReady(true);
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.i(TAG, "onDoubleTapEvent");
            return true;
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return true;
        }

        private void changeProcessForScroll(MotionEvent e1, MotionEvent e2, boolean direction) {
            if (ObjectUtil.isNotNull(player)) {
                isScroll = true;
                if (scrollProcessView.getVisibility() == View.GONE) {
                    scrollProcessView.setVisibility(View.VISIBLE);
                }
                player.setPlayWhenReady(false);
                long duration = player.getDuration();
                resumePosition = player.getCurrentPosition();
                int p = 0;

                if (ObjectUtil.isNotNull(changeProcessText)) {
                    String text = "";
                    if (direction) {
                        p = (int) (((e2.getX() - e1.getX()) / FLIP_DISTANCE) * 1.8);
                        text = "[+";
                        if (p > 9) {
                            int m = p / 60;
                            if (m > 1) {
                                if (m > 9) {
                                    text += m + ":" + (p - m * 60) + "]";
                                } else {
                                    text += "0" + m + ":" + (p - m * 60) + "]";
                                }
                            } else {
                                text += "00:" + p + "]";
                            }
                        } else {
                            text += "00:0" + p + "]";
                        }
                        resumePosition += p * 1000;
                        if (resumePosition > duration) {
                            resumePosition = duration;
                        }
                    } else {
                        p = (int) (((e1.getX() - e2.getX()) / FLIP_DISTANCE) * 1.8);
                        text = "[-";
                        if (p > 9) {
                            int m = p / 60;
                            if (m > 1) {
                                if (m > 9) {
                                    text += m + ":" + (p - m * 60) + "]";
                                } else {
                                    text += "0" + m + ":" + (p - m * 60) + "]";
                                }
                            } else {
                                text += "00:" + p + "]";
                            }
                        } else {
                            text += "00:0" + p + "]";
                        }
                        resumePosition -= p * 1000;
                        if (resumePosition < 0) {
                            resumePosition = 0;
                        }
                    }
                    changeProcessText.setText(text);
                }
                long currentPosition = resumePosition;
                if (ObjectUtil.isNotNull(currentProcessText)) {
                    String text = "";
                    int dh = (int) (duration / (60 * 60 * 1000));
                    int h = (int) (currentPosition / (60 * 60 * 1000));
                    int m = (int) ((currentPosition - h * 60 * 60 * 1000) / (60 * 1000));
                    int s = (int) ((currentPosition - h * 60 * 60 * 1000 - m * 60 * 1000) / (1000));
                    if (dh > 0) {
                        if (h > 9) {
                            text += h + ":";
                        } else {
                            text += "0" + h + ":";
                        }
                    }
                    if (m > 9) {
                        text += m + ":";
                    } else {
                        text += "0" + m + ":";
                    }
                    if (s > 9) {
                        text += s;
                    } else {
                        text += "0" + s;
                    }
                    currentProcessText.setText(text);
                }
            }
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param distanceY
     */
    private void onVolumeSlide(float distanceY) {
        // 显示
        if (currentVolume == -1) {
            isScroll = true;
            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            operationVolumeOrBrightness.setVisibility(View.VISIBLE);
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume < 0)
                currentVolume = 0;
            //初始进度条
            ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
            lp.width = operationFull.getWidth() * currentVolume / maxVolume;
            mOperationPercent.setLayoutParams(lp);
        }
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        if (currentVolume < 0)
            currentVolume = 0;
        int percent = (int) (distanceY / ((float) this.height / maxVolume));
        int index = percent + currentVolume;
        if (index > maxVolume)
            index = maxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = operationFull.getWidth() * index / maxVolume;
        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param distanceY
     */
    private void onBrightnessSlide(float distanceY) {
        if (mBrightness < 0) {
            isScroll = true;
            mBrightness = activity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            operationVolumeOrBrightness.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        float percent = distanceY / height;
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        activity.getWindow().setAttributes(lpa);

        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = (int)(operationFull.getWidth() * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP && isScroll) {
            isScroll = false;
            endGesture();

        }
        return gestureDetector.onTouchEvent(ev);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        currentVolume = -1;
        mBrightness = -1f;
        // 隐藏
        operationVolumeOrBrightness.setVisibility(View.GONE);
        if (ObjectUtil.isNotNull(player) && !player.getPlayWhenReady()) {
            player.seekTo(resumePosition);
            player.setPlayWhenReady(true);
            if (this.scrollProcessView.getVisibility() == View.VISIBLE) {
                this.scrollProcessView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * ========================================屏幕手势监听 end===================================================
     */
    private final class ComponentListener implements SimpleExoPlayer.VideoListener,
            TextRenderer.Output, ExoPlayer.EventListener {

        // TextRenderer.Output implementation

        @Override
        public void onCues(List<Cue> cues) {
            if (subtitleView != null) {
                subtitleView.onCues(cues);
            }
        }

        // SimpleExoPlayer.VideoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            if (contentFrame != null) {
                float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
                contentFrame.setAspectRatio(aspectRatio);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            updateForCurrentTrackSelections();
        }

        // ExoPlayer.EventListener implementation

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (!isScroll) {
                maybeShowController(false);
            }

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            // Do nothing.
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.
        }

    }

}
