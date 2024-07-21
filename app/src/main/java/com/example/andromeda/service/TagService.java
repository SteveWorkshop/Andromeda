package com.example.andromeda.service;

import com.example.andromeda.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getAll();
    Tag getById(Long id);
    Long addTag(Tag tag);
    int updateTag(Tag tag);
    int deleteById(Long id);
    boolean checkIfUse(Long id);
}
