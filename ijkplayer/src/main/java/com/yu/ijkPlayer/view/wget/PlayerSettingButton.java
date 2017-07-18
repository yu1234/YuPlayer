package com.yu.ijkPlayer.view.wget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yu.ijkPlayer.R;
import com.yu.ijkPlayer.utils.DensityUtils;


/**
 * Created by yu on 2017/7/18 0018.
 */

public class PlayerSettingButton extends Button{
    private Resources resources;
    private Enum type;
private Context context;
    public PlayerSettingButton(Context context) {
        this(context,null);
    }

    public PlayerSettingButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayerSettingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        resources= context.getResources();
        this.post(new Runnable() {
            @Override
            public void run() {
                initView();
            }
        });
    }

    /**
     * 初始化页面
     * @return
     */
    private void initView(){
        this.setBackground(resources.getDrawable(R.drawable.ijk_player_setting_button_style,null));
        this.setHeight(DensityUtils.dp2px(this.context, 35.0f));
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setGravity(Gravity.CENTER);
        this.setTextColor(resources.getColorStateList(R.color.ijk_player_setting_button_color));
    }

    public Enum getType() {
        return type;
    }

    public void setType(Enum type) {
        this.type = type;
    }
}
