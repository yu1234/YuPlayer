package com.yu.yuPlayerLib.media.impl;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;

/**
 * Dispatches operations to the player.
 * <p>
 * Implementations may choose to suppress (e.g. prevent playback from resuming if audio focus is
 * denied) or modify (e.g. change the seek position to prevent a user from seeking past a
 * non-skippable advert) operations.
 */

public interface ControlDispatcher {
    /**
     * Dispatches a {@link ExoPlayer#setPlayWhenReady(boolean)} operation.
     *
     * @param player The player to which the operation should be dispatched.
     * @param playWhenReady Whether playback should proceed when ready.
     * @return True if the operation was dispatched. False if suppressed.
     */
    boolean dispatchSetPlayWhenReady(ExoPlayer player, boolean playWhenReady);

    /**
     * Dispatches a {@link ExoPlayer#seekTo(int, long)} operation.
     *
     * @param player The player to which the operation should be dispatched.
     * @param windowIndex The index of the window.
     * @param positionMs The seek position in the specified window, or {@link C#TIME_UNSET} to seek
     *     to the window's default position.
     * @return True if the operation was dispatched. False if suppressed.
     */
    boolean dispatchSeekTo(ExoPlayer player, int windowIndex, long positionMs);
}
