package com.example.andromeda.entity;

import androidx.room.Entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Entity
public class Tag extends BaseEntity implements Serializable {
    public static final long DEFAULT_TAG=-1024l;
    private String tagName;
    //TODO: 标签分类与颜色
}
