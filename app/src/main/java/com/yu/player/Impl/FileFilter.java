package com.yu.player.Impl;

import com.yu.player.utils.ReadFileUtil;

import java.io.File;

/**
 * Created by igreentree on 2017/6/28 0028.
 */

public interface FileFilter {
    boolean doFilter(File file, ReadFileUtil.FileType fileType);
}
