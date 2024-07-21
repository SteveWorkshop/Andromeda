package com.example.andromeda.service.impl;

import android.content.Context;

import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.service.TagService;

import java.util.List;

public class TagServiceImpl implements TagService {

    private TagDao tagDao;
    private NoteDao noteDao;
    private static volatile TagServiceImpl instance;
    public static synchronized TagService getInstance(Context context)
    {
        if(instance==null)
        {
            instance=new TagServiceImpl();
            TagDao tagDao= DBConfig.getInstance(context).getTagDao();
            NoteDao noteDao=DBConfig.getInstance(context).getNoteDao();
            instance.tagDao=tagDao;
            instance.noteDao=noteDao;
        }
        return instance;
    }

    @Override
    public List<Tag> getAll() {
        return tagDao.getall();
    }

    @Override
    public Tag getById(Long id) {
        return tagDao.getById(id);
    }

    @Override
    public Long addTag(Tag tag) {
        return tagDao.insertTag(tag);
    }

    @Override
    public int updateTag(Tag tag) {
        return tagDao.updateTag(tag);
    }

    @Override
    public int deleteById(Long id) {
        return 0;
    }

    @Override
    public boolean checkIfUse(Long id) {
        int rows= noteDao.getCount(id);
        return rows>0;
    }

}
