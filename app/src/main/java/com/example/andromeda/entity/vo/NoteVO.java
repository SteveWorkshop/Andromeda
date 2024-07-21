package com.example.andromeda.entity.vo;

import java.io.Serializable;

public class NoteVO implements Serializable {
    public Long id;
    public Long tagId;
    public String title;
    public String content;
    public String tagName;
    public Long createTime;
    public Long updateTime;
}
