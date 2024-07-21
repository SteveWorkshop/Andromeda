package com.example.andromeda.config;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.Tag;

@Database(version = 3,entities ={Note.class, Tag.class}, exportSchema=false)
public abstract class DBConfig extends RoomDatabase {
   public static final String DB_NAME="note.db";
   private static volatile DBConfig instance;

   public static synchronized DBConfig getInstance(Context context)
   {
      if(instance==null)
      {
         instance=create(context.getApplicationContext());
      }
      return instance;
   }

   private static DBConfig create(final Context context)
   {
      return Room.databaseBuilder(context, DBConfig.class,DB_NAME)
              .allowMainThreadQueries()
              .fallbackToDestructiveMigration()
              .build();
   }
   public abstract NoteDao getNoteDao();
   public abstract TagDao getTagDao();

}
