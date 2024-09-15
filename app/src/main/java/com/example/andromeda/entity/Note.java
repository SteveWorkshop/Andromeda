package com.example.andromeda.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
//@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Note extends BaseEntity implements Serializable {

    private String title;
    private String content;
    private Long tag=Tag.DEFAULT_TAG;
}
