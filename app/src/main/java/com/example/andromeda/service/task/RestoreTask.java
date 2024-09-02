package com.example.andromeda.service.task;

import android.content.Context;
import android.os.AsyncTask;

import com.example.andromeda.BaseApplication;
import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.entity.dto.BackupDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class RestoreTask extends AsyncTask<String,Integer,Integer> {
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FALIURE=1;

    public static final int STAGE_SYS_PREP=1;
    public static final int STAGE_RESTORE=2;
    public static final int STAGE_DONE=3;

    private Context context;
    private NoteDao noteDao;
    private TagDao tagDao;

    @Getter
    @Setter
    protected BackupServiceListener listener;

    @Getter
    @Setter
    private InputStream inputStream;

    public RestoreTask(){
        context= BaseApplication.getApplication();
        noteDao= DBConfig.getInstance(context).getNoteDao();
        tagDao=DBConfig.getInstance(context).getTagDao();
    }

    public RestoreTask(BackupServiceListener listener)
    {
        context= BaseApplication.getApplication();
        noteDao= DBConfig.getInstance(context).getNoteDao();
        tagDao=DBConfig.getInstance(context).getTagDao();
        this.listener=listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPrep();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        publishProgress(STAGE_SYS_PREP);
        try{
            BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb=new StringBuffer();
            String line;
            while((line=reader.readLine())!=null)
            {
                sb.append(line);
            }
            reader.close();
            GsonBuilder builder=new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson=builder.create();
            BackupDTO backupDTO = gson.fromJson(sb.toString(), BackupDTO.class);

            publishProgress(STAGE_RESTORE);

            List<Tag> tags = backupDTO.getTags();
            List<Note> notes = backupDTO.getNotes();

            for (Tag tag : tags) {
                tag.setId(null);
                tagDao.insertTag(tag);
            }
            for (Note note:notes)
            {
                note.setId(null);
                noteDao.insertNote(note);
            }
            publishProgress(STAGE_DONE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return TYPE_FALIURE;
        }
        return TYPE_SUCCESS;
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
}
