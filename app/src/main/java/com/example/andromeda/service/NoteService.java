package com.example.andromeda.service;

import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.dto.NoteDTO;
import com.example.andromeda.entity.vo.NoteVO;

import java.util.List;

public interface NoteService {
    List<NoteVO> getAllPreview();
    NoteVO getById(Long id);
    Long insertNote(NoteDTO note);
    int updateNote(NoteDTO note);
    int deleteById(Long id);
}
