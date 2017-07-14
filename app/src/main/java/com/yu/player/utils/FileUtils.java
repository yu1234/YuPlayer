package com.yu.player.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.xiaoleilu.hutool.util.StrUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class FileUtils {
    // http://www.fileinfo.com/filetypes/video , "dat" , "bin" , "rms"
    public static final String[] VIDEO_EXTENSIONS = {"blv", "3gp", "wmv", "ts", "3gp2", "rmvb", "mp4", "mov", "m4v", "avi", "3gpp", "3gpp2", "mkv", "flv", "divx", "f4v", "rm", "avb", "asf", "ram", "avs", "mpg", "v8", "swf", "m2v", "asx", "ra", "ndivx", "box", "xvid"};
    // http://www.fileinfo.com/filetypes/audio , "spx" , "mid" , "sf"
    public static final String[] AUDIO_EXTENSIONS = {"4mp", "669", "6cm", "8cm", "8med", "8svx", "a2m", "aa", "aa3", "aac", "aax", "abc", "abm", "ac3", "acd", "acd-bak", "acd-zip", "acm", "act", "adg", "afc", "agm", "ahx", "aif", "aifc", "aiff", "ais", "akp", "al", "alaw", "all", "amf", "amr", "ams", "ams", "aob", "ape", "apf", "apl", "ase", "at3", "atrac", "au", "aud", "aup", "avr", "awb", "band", "bap", "bdd", "box", "bun", "bwf", "c01", "caf", "cda", "cdda", "cdr", "cel", "cfa", "cidb", "cmf", "copy", "cpr", "cpt", "csh", "cwp", "d00", "d01", "dcf", "dcm", "dct", "ddt", "dewf", "df2", "dfc", "dig", "dig", "dls", "dm", "dmf", "dmsa", "dmse", "drg", "dsf", "dsm", "dsp", "dss", "dtm", "dts", "dtshd", "dvf", "dwd", "ear", "efa", "efe", "efk", "efq", "efs", "efv", "emd", "emp", "emx", "esps", "f2r", "f32", "f3r", "f4a", "f64", "far", "fff", "flac", "flp", "fls", "frg", "fsm", "fzb", "fzf", "fzv", "g721", "g723", "g726", "gig", "gp5", "gpk", "gsm", "gsm", "h0", "hdp", "hma", "hsb", "ics", "iff", "imf", "imp", "ins", "ins", "it", "iti", "its", "jam", "k25", "k26", "kar", "kin", "kit", "kmp", "koz", "koz", "kpl", "krz", "ksc", "ksf", "kt2", "kt3", "ktp", "l", "la", "lqt", "lso", "lvp", "lwv", "m1a", "m3u", "m4a", "m4b", "m4p", "m4r", "ma1", "mdl", "med", "mgv", "midi", "miniusf", "mka", "mlp", "mmf", "mmm", "mmp", "mo3", "mod", "mp1", "mp2", "mp3", "mpa", "mpc", "mpga", "mpu", "mp_", "mscx", "mscz", "msv", "mt2", "mt9", "mte", "mti", "mtm", "mtp", "mts", "mus", "mws", "mxl", "mzp", "nap", "nki", "nra", "nrt", "nsa", "nsf", "nst", "ntn", "nvf", "nwc", "odm", "oga", "ogg", "okt", "oma", "omf", "omg", "omx", "ots", "ove", "ovw", "pac", "pat", "pbf", "pca", "pcast", "pcg", "pcm", "peak", "phy", "pk", "pla", "pls", "pna", "ppc", "ppcx", "prg", "prg", "psf", "psm", "ptf", "ptm", "pts", "pvc", "qcp", "r", "r1m", "ra", "ram", "raw", "rax", "rbs", "rcy", "rex", "rfl", "rmf", "rmi", "rmj", "rmm", "rmx", "rng", "rns", "rol", "rsn", "rso", "rti", "rtm", "rts", "rvx", "rx2", "s3i", "s3m", "s3z", "saf", "sam", "sb", "sbg", "sbi", "sbk", "sc2", "sd", "sd", "sd2", "sd2f", "sdat", "sdii", "sds", "sdt", "sdx", "seg", "seq", "ses", "sf2", "sfk", "sfl", "shn", "sib", "sid", "sid", "smf", "smp", "snd", "snd", "snd", "sng", "sng", "sou", "sppack", "sprg", "sseq", "sseq", "ssnd", "stm", "stx", "sty", "svx", "sw", "swa", "syh", "syw", "syx", "td0", "tfmx", "thx", "toc", "tsp", "txw", "u", "ub", "ulaw", "ult", "ulw", "uni", "usf", "usflib", "uw", "uwf", "vag", "val", "vc3", "vmd", "vmf", "vmf", "voc", "voi", "vox", "vpm", "vqf", "vrf", "vyf", "w01", "wav", "wav", "wave", "wax", "wfb", "wfd", "wfp", "wma", "wow", "wpk", "wproj", "wrk", "wus", "wut", "wv", "wvc", "wve", "wwu", "xa", "xa", "xfs", "xi", "xm", "xmf", "xmi", "xmz", "xp", "xrns", "xsb", "xspf", "xt", "xwb", "ym", "zvd", "zvr"};

    public static final String[] PICTURE_EXTENSIONS = {"png", "jpg", "jpeg", "bmp", "gif"};

    private static final HashSet<String> mHashVideo;
    private static final HashSet<String> mHashAudio;
    private static final HashSet<String> mHashPicture;
    private static final double KB = 1024.0;
    private static final double MB = KB * KB;
    private static final double GB = KB * KB * KB;
    private static final long SECOND = 1000;
    private static final long MIN = 60 * 1000;
    private static final long HOUR = 60 * 60 * 1000;

    static {
        mHashVideo = new HashSet<String>(Arrays.asList(VIDEO_EXTENSIONS));
        mHashAudio = new HashSet<String>(Arrays.asList(AUDIO_EXTENSIONS));
        mHashPicture = new HashSet<String>(Arrays.asList(PICTURE_EXTENSIONS));
    }

    /**
     * 是否是视频
     */
    public static boolean isVideo(File f) {
        final String ext = getFileExtension(f);
        return mHashVideo.contains(ext);
    }

    /**
     * 是否是音频
     */
    public static boolean isAudio(File f) {
        final String ext = getFileExtension(f);
        return mHashAudio.contains(ext);
    }

    /**
     * 是否是图片
     */
    public static boolean isPicture(File f) {
        final String ext = getFileExtension(f);
        return mHashPicture.contains(ext);
    }

    /**
     * 获取文件后缀
     */
    public static String getFileExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    public static String getUrlExtension(String url) {
        if (!StrUtil.isNotBlank(url)) {
            int i = url.lastIndexOf('.');
            if (i > 0 && i < url.length() - 1) {
                return url.substring(i + 1).toLowerCase();
            }
        }
        return "";
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String formatFileSize(long size) {
        String fileSize;
        if (size < KB)
            fileSize = size + "B";
        else if (size < MB)
            fileSize = String.format("%.1f", size / KB) + "KB";
        else if (size < GB)
            fileSize = String.format("%.1f", size / MB) + "MB";
        else
            fileSize = String.format("%.1f", size / GB) + "GB";

        return fileSize;
    }

    //格式化时长
    public static String formatDuration(long duration) {
        String formatDuration;
        if (duration < MIN) {
            int second = (int) (duration / SECOND);
            if (second > 9) {
                formatDuration = "00:" + second;
            } else {
                formatDuration = "00:0" + second;
            }
        } else if (duration < HOUR) {
            int min = (int) (duration / MIN);
            int second = (int) ((duration - min * MIN) / SECOND);
            if (min > 9) {
                formatDuration = min + ":";
            } else {
                formatDuration = "0" + min + ":";
            }
            if (second > 9) {
                formatDuration += second;
            } else {
                formatDuration += "0" + second;
            }
        } else {
            int hour = (int) (duration / HOUR);
            int min = (int) ((duration - hour * HOUR) / MIN);
            int second = (int) ((duration - hour * HOUR - min * MIN) / SECOND);
            if (hour > 9) {
                formatDuration = hour + ":";
            } else {
                formatDuration = "0" + hour + ":";
            }
            if (min > 9) {
                formatDuration += min + ":";
            } else {
                formatDuration += "0" + min + ":";
            }
            if (second > 9) {
                formatDuration += second;
            } else {
                formatDuration += "0" + second;
            }
        }

        return formatDuration;
    }

    //格式化时长
    public static String formatDuration(Context context, File file) {
        MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(file));
        int duration = mp.getDuration();
        String formatDuration = FileUtils.formatDuration(duration);
        mp.release();
        return formatDuration;
    }

    //格式化时长
    public static String formatDuration(Context context, Uri uri) {
        MediaPlayer mp = MediaPlayer.create(context, uri);
        int duration = mp.getDuration();
        String formatDuration = FileUtils.formatDuration(duration);
        mp.release();
        return formatDuration;
    }

    //格式化时长
    public static String formatDuration(Context context, String path) {
        File file = new File(path);
        if (file.exists()) {
            MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(file));
            int duration = mp.getDuration();
            String formatDuration = FileUtils.formatDuration(duration);
            mp.release();
            return formatDuration;
        } else {
            return "00:00";
        }

    }
}
