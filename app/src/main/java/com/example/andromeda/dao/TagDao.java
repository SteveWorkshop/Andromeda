package com.example.andromeda.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import com.example.andromeda.entity.Tag;

import java.util.List;

@Dao
public interface TagDao {

    @Insert
    Long insertTag(Tag tag);

    @Update
    int updateTag(Tag tag);

    //todo：分页查询
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select id,tagName from Tag order by updateTime desc")
    List<Tag> getall();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select id,tagName from Tag where id=:id")
    Tag getById(Long id);

    @Query("update Tag set isDeleted=1 where id=:id")
    int deleteById(Long id);

    @Query("update Tag set isDeleted=0 where id=:id")
    int recycleById(Long id);

    @Query("delete from Tag where id=:id")
    int eraseById(Long id);


}
