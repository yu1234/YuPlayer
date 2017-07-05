package com.yu.player.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ImageUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;
import com.yu.exoplayer.utils.PlayerUtil;
import com.yu.player.Impl.OnRecyclerViewClickListener;
import com.yu.player.Impl.OnRecyclerViewLongClickListener;
import com.yu.player.R;
import com.yu.player.bean.VideoFile;
import com.yu.player.dao.CommonDao;
import com.yu.player.utils.DaoUtils;
import com.yu.player.utils.ImageTools;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igreentree on 2017/6/29 0029.
 */

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoHolder> implements OnRecyclerViewClickListener, OnRecyclerViewLongClickListener {
    private Context context;
    private List<VideoFile> videoFiles;
    private OnRecyclerViewClickListener recyclerViewClickListener;
    private OnRecyclerViewLongClickListener recyclerViewLongClickListener;

    public VideoRecyclerViewAdapter(Context context, List<VideoFile> videoFiles) {
        this.context = context;
        this.videoFiles = videoFiles;
        this.recyclerViewLongClickListener = this;
        this.recyclerViewClickListener = this;
    }

    @Override
    public VideoRecyclerViewAdapter.VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoHolder(LayoutInflater.from(context).inflate(R.layout.video_file_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoRecyclerViewAdapter.VideoHolder holder, final int position) {
        if (CollectionUtil.isNotEmpty(videoFiles)) {
            VideoFile videoFile = videoFiles.get(position);
            holder.videoFileTitle.setText(videoFile.getName());
            String duration = videoFile.getFormatDuration();
            holder.duration.setText(duration);
            holder.videoThumbnails.setImageBitmap(ImageTools.getRoundedCornerBitmap(videoFile.getThumbnails(), 20f));
            holder.videoThumbnails.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.videoThumbnails.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            if (videoFile.isNew()) {
                holder.isNew.setVisibility(View.VISIBLE);
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ObjectUtil.isNotNull(recyclerViewClickListener)) {
                        recyclerViewClickListener.onClick(v, position);
                    }
                }
            });
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (ObjectUtil.isNotNull(recyclerViewLongClickListener)) {
                        return recyclerViewLongClickListener.onLongClick(v, position);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (CollectionUtil.isNotEmpty(videoFiles)) {
            return videoFiles.size();
        }
        return 0;
    }

    @Override
    public void onClick(View v, int position) {
        if (ObjectUtil.isNotNull(videoFiles) && ObjectUtil.isNotNull(videoFiles.get(position)) && ObjectUtil.isNotNull(videoFiles.get(position).getUri())) {
            VideoFile videoFile = videoFiles.get(position);
            //改变数据库状态
            CommonDao<VideoFile> videoFileDao = DaoUtils.DaoFactory(context, "VideoFile");
            if (ObjectUtil.isNotNull(videoFileDao)) {
                try {
                    VideoFile videoFile1 = videoFileDao.queryById(videoFile.getId());
                    videoFile1.setWatch(true);
                    videoFile1.setNew(false);
                    videoFileDao.update(videoFile1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            Uri uri = videoFile.getUri();
            PlayerUtil.playVideo(context, uri);
        }

    }

    @Override
    public boolean onLongClick(View v, int position) {
        return false;
    }


    class VideoHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.videoThumbnails)
        ImageView videoThumbnails;
        @BindView(R.id.videoFileTitle)
        TextView videoFileTitle;
        @BindView(R.id.duration)
        TextView duration;
        @BindView(R.id.new_image)
        ImageView isNew;
        View view;

        VideoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}
