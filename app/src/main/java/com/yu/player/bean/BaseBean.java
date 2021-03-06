package com.yu.player.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by igreentree on 2017/7/5 0005.
 */
@DatabaseTable
public class BaseBean {

    @DatabaseField(id=true)
    private long id;
    @DatabaseField
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
