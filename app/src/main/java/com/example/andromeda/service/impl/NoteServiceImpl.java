package com.example.andromeda.service.impl;

import android.content.Context;

import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.dto.NoteDTO;
import com.example.andromeda.entity.vo.NoteVO;
import com.example.andromeda.service.NoteService;

import java.util.List;


public class NoteServiceImpl implements NoteService {
    private NoteDao noteDao;
    private TagDao tagDao;

    private static volatile NoteServiceImpl instance;

    public static synchronized NoteService getInstance(Context context) {
        if (instance == null) {
            instance = new NoteServiceImpl();
            NoteDao noteDao = DBConfig.getInstance(context).getNoteDao();
            TagDao tagDao = DBConfig.getInstance(context).getTagDao();
            instance.noteDao = noteDao;
            instance.tagDao = tagDao;
        }
        return instance;
    }


    @Override
    public List<NoteVO> getAllPreview() {
        return noteDao.getAllPreview_V2();
    }

    @Override
    public NoteVO getById(Long id) {
        return noteDao.getById_V2(id);
    }

    @Override
    public Long insertNote(NoteDTO note) {
        Note insNote = new Note();
        insNote.setTag(note.getTag());
        insNote.setTitle(note.getTitle());
        insNote.setContent(note.getContent());
        return noteDao.insertNote(note);
    }

    @Override
    public int updateNote(NoteDTO note) {
        Note insNote = new Note();
        insNote.setId(note.getId());
        insNote.setTag(note.getTag());
        insNote.setTitle(note.getTitle());
        insNote.setContent(note.getContent());
        insNote.setUpdateTime(System.currentTimeMillis());

        return noteDao.updateNote(insNote);
    }

    @Override
    public int deleteById(Long id) {
        return noteDao.deleteById(id);
    }
}
