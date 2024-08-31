package com.example.andromeda.service.task;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.andromeda.BaseApplication;
import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.entity.dto.BackupDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class BackupTask extends AsyncTask<String,Integer,Integer> {
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FALIURE=1;

    public static final int STAGE_SYS_PREP=1;
    public static final int STAGE_BACKUP=2;
    public static final int STAGE_DONE=3;

    private Context context;
    private NoteDao noteDao;
    private TagDao tagDao;

    private int total;

    @Getter
    @Setter
    protected BackupListener listener;

    public BackupTask(){
        context= BaseApplication.getApplication();
        noteDao= DBConfig.getInstance(context).getNoteDao();
        tagDao=DBConfig.getInstance(context).getTagDao();
    }

    public BackupTask(BackupListener listener)
    {
        context= BaseApplication.getApplication();
        noteDao= DBConfig.getInstance(context).getNoteDao();
        tagDao=DBConfig.getInstance(context).getTagDao();
        this.listener=listener;
    }

    @Override
    protected void onPreExecute() {
        //todo:额啊(⊙﹏⊙)我也不知道写什么
        super.onPreExecute();
        //我应该申请一下通知权限
        listener.onPrep();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        publishProgress(STAGE_SYS_PREP);
        //todo:bad experience
        List<Note> notes=noteDao.backupAll();
        List<Tag>  tags=tagDao.backupAll();

        if(tags==null||notes==null)
        {
            return TYPE_FALIURE;
        }

        publishProgress(STAGE_BACKUP);

        BackupDTO backupDTO=new BackupDTO();
        backupDTO.setNotes(notes);
        backupDTO.setTags(tags);
        GsonBuilder builder=new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson=builder.create();
        String file=gson.toJson(backupDTO);

        try {
            //写文件
            String fileName="andromeda_backup.json";
            ContentValues contentValues=new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            if(uri!=null)
            {
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
                writer.write(file);
                writer.close();
                writer.flush();
                publishProgress(STAGE_DONE);
                return TYPE_SUCCESS;
            }
            else{
                return TYPE_FALIURE;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return TYPE_FALIURE;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int stage=values[0];
        listener.onProgress(stage);
    }


    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer){
            case TYPE_SUCCESS:{
                listener.onSuccess();
                break;
            }
            case TYPE_FALIURE:{
                listener.onFailure();
                break;
            }
            default:{break;}
        }
    }


    public interface BackupListener{
        void onPrep();
        void onProgress(int progress);
        void onSuccess();
        void onFailure();
    }
}
