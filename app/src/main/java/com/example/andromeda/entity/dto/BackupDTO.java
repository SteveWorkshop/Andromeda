package com.example.andromeda.entity.dto;

import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.Tag;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class BackupDTO implements Serializable {
    private List<Note> notes;
    private List<Tag> tags;
}
