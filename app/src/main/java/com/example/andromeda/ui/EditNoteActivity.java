package com.example.andromeda.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.andromeda.BaseApplication;
import com.example.andromeda.R;
import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.databinding.ActivityEditNoteBinding;
import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.entity.vo.NoteVO;
import com.example.andromeda.vm.EditNoteViewModel;
import com.example.andromeda.vm.pages.EditNoteViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Objects;

public class EditNoteActivity extends AppCompatActivity {
    private static final String TAG = "EditNoteActivity";

    //callinmode记录怎么打开的
    //mode用来控制存储按钮，需要切换
    //由于没有设计放弃保存的功能，因此无论如何都要返回（只要数据库操作成功）
    //mode可以修改
    //callinmode不需要

    private EditNoteViewModel viewModel;

    public static final int MODE_EDIT=0;
    public static final int MODE_APPEND=1;

    public static final int REQUEST_CODE=1000;

    private boolean isModified=false;
    private int mode=0;

    private int callInMode=0;

    private NoteVO lastSavedVO=new NoteVO();

    private ActivityEditNoteBinding binding;
    private Long edid=-1L;

    private Long tagId=-1024L;

    private Long apsid=-1L;

    private Long lastSavedId=1l;

    private String tagName;


    private NoteDao noteDao;
    private TagDao tagDao;

    private List<Tag> tagListForSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityEditNoteBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.editNoteToolbar);

        //关联viewmodel
        viewModel= new ViewModelProvider(this,new EditNoteViewModelFactory()).get(EditNoteViewModel.class);

        reFindStatus();

        noteDao= DBConfig.getInstance(BaseApplication.getApplication()).getNoteDao();
        tagDao=DBConfig.getInstance(BaseApplication.getApplication()).getTagDao();

        tagListForSelection=tagDao.getAll();
        Tag tag=new Tag();
        tag.setId(Tag.DEFAULT_TAG);
        tag.setTagName("默认标签");
        tagListForSelection.add(0,tag);

        String[] labels=convertTagDisplayTable(tagListForSelection);

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_popup_window_item, labels);
        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAnchorView(binding.listPopupButton);

        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            String name=tagListForSelection.get(position).getTagName();
            Long aid=tagListForSelection.get(position).getId();

            if(mode==MODE_EDIT)
            {
                if(!Objects.equals(aid, tagId))
                {
                    isModified=true;
                    viewModel.selectTag(aid,name);
                    tagId=aid;
                    tagName=name;
                    binding.listPopupButton.setText(name);
                    if(tagName==null)
                    {
                        tagName="默认标签";
                    }
                }

            }
            else{
                isModified=true;
                viewModel.selectTag(aid,name);
                tagId=aid;
                tagName=name;
                binding.listPopupButton.setText(name);
                if(tagName==null)
                {
                    tagName="默认标签";
                }
            }


            listPopupWindow.dismiss();
        });

        binding.listPopupButton.setOnClickListener (v -> listPopupWindow.show());

        Intent intent=getIntent();
        Long id=(Long)intent.getSerializableExtra("edit_id");
        if(id!=null)
        {
            //切换为编辑模式
            mode=MODE_EDIT;
            callInMode=MODE_EDIT;
            edid=id;

            viewModel.setEditInfo(id,MODE_EDIT);

            loadNote(edid);
            MyWatcher myWatcher = new MyWatcher();            
            binding.noteTitleTxb.addTextChangedListener(myWatcher);
            binding.editAreaTxb.addTextChangedListener(myWatcher);
        }
        else {
            mode=MODE_APPEND;
            callInMode=MODE_APPEND;
            viewModel.setEM(MODE_APPEND);
            //do nothing
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.edit_note_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.save_note_menu:
            {
                //Toast.makeText(this, "好好好好好赢赢赢赢赢对对对对对", Toast.LENGTH_SHORT).show();
                //开启后台任务
                persistNote(id->{
                    if(id>0)
                    {
                        apsid=id;
                        mode=MODE_EDIT;
                        //缓存上次状态，用于返回
                        lastSavedVO.tagId=tagId;
                        lastSavedVO.tagName=tagName;
                        lastSavedVO.title=binding.noteTitleTxb.getText().toString();
                        lastSavedVO.content=binding.noteTitleTxb.getText().toString();
                        //lastSavedVO.createTime=note.createTime;
                        lastSavedVO.updateTime=System.currentTimeMillis();
                        Toast.makeText(this, "Ciallo～(∠・ω< )", Toast.LENGTH_SHORT).show();
                    }
                    else{

                    }
                },rows->{
                    if(rows>0)
                    {
                        lastSavedVO.tagId=tagId;
                        lastSavedVO.tagName=tagName;
                        lastSavedVO.title=binding.noteTitleTxb.getText().toString();
                        lastSavedVO.content=binding.noteTitleTxb.getText().toString();
                        //lastSavedVO.createTime=note.createTime;
                        lastSavedVO.updateTime=System.currentTimeMillis();
                        Toast.makeText(this, "Ciallo～(∠・ω< )", Toast.LENGTH_SHORT).show();
                    }
                    else{

                    }
                });
                break;
            }
            case R.id.export_note_menu:{
                if(Build.VERSION.SDK_INT<Build.VERSION_CODES.R)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                    else {
                        exportNote(binding.noteTitleTxb.getText().toString(),binding.editAreaTxb.getText().toString(),tagName);
                    }
                }
                else{
                    //android11以上的情况
                    if(Environment.isExternalStorageManager())
                    {
                        //有权限，直接申请
                        exportNote(binding.noteTitleTxb.getText().toString(),binding.editAreaTxb.getText().toString(),tagName);
                    }
                    else{
                        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(this);
                        builder.setTitle("注意");
                        builder.setMessage("由于在Android11之后，黏糊糊的Google培养基发生了成分变化，请在接下来的操作中，选择允许访问所有文件");
                        builder.setCancelable(false);
                        builder.setPositiveButton("确定",(dialog,which)->{
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQUEST_CODE);
                        });
                        builder.show();
                    }
                }

                break;
            }
            default:{
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent=new Intent();
        if(isModified){
            //修改逻辑
            MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(this);
            builder.setTitle("警告");
            builder.setMessage("您有未保存的内容，是否保存？");
            builder.setCancelable(false);
            builder.setPositiveButton("是",((dialog, which) -> {
                persistNote(id->{
                    if(id>0)
                    {
                        Toast.makeText(this, "Ciallo～(∠・ω< )", Toast.LENGTH_SHORT).show();
                        NoteVO vo=new NoteVO();
                        vo.id=id;
                        vo.tagId=tagId;
                        vo.tagName=tagName;
                        vo.content=binding.editAreaTxb.getText().toString();
                        vo.title=binding.noteTitleTxb.getText().toString();
                        vo.createTime=System.currentTimeMillis();
                        vo.updateTime=System.currentTimeMillis();

                        intent.putExtra("data",vo);
                        setResult(RESULT_OK,intent);
                    }
                    else{
                        Toast.makeText(this, "出错了", Toast.LENGTH_SHORT).show();
                        //返回上次状态
                        if(lastSavedVO.id==null)
                        {
                            setResult(RESULT_CANCELED);
                        }
                        else{
                            intent.putExtra("data",lastSavedVO);
                            setResult(RESULT_OK,intent);
                        }
                    }
                    finish();
                },rows->{
                    if(rows>0)
                    {
                        Toast.makeText(this, "Ciallo～(∠・ω< )", Toast.LENGTH_SHORT).show();
                        NoteVO vo=new NoteVO();
                        vo.id=edid;
                        vo.tagId=tagId;
                        vo.tagName=tagName;
                        vo.content=binding.editAreaTxb.getText().toString();
                        vo.title=binding.noteTitleTxb.getText().toString();
                        vo.createTime=System.currentTimeMillis();
                        vo.updateTime=System.currentTimeMillis();
                        intent.putExtra("data",vo);
                        setResult(RESULT_OK,intent);

                    }
                    else{
                        if(lastSavedVO.id==null)
                        {
                            setResult(RESULT_CANCELED);
                        }
                        else{
                            intent.putExtra("data",lastSavedVO);
                            setResult(RESULT_OK,intent);
                        }
                    }
                    finish();
                });
            }));
            builder.setNegativeButton("否",((dialog, which) -> {
                finish();
            }));
            builder.setNeutralButton("返回编辑",((dialog, which) -> {

            }));
            builder.show();
        }
        else{
            //返回最近状态
            intent.putExtra("data",lastSavedVO);
            setResult(RESULT_OK);
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if (grantResults.length!=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportNote(binding.noteTitleTxb.getText().toString(),binding.editAreaTxb.getText().toString(),tagName);
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }

                break;
            }
            default:{
                break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE&&Build.VERSION.SDK_INT>=Build.VERSION_CODES.R)
        {
            if (Environment.isExternalStorageManager()){
                exportNote(binding.noteTitleTxb.getText().toString(),binding.editAreaTxb.getText().toString(),tagName);
            }
            else{
                Toast.makeText(this, "我们需要管理外部存储的权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportNote(String title, String content, String tagName)
    {
        System.out.println(title+" "+tagName+" "+content);
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/andromeda";
        File dir=new File(path);
        if(!dir.exists())
        {
            dir.mkdir();
        }
        File file=new File(path,title+".txt");
        try {
            FileOutputStream os=new FileOutputStream(file);
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(os));
            writer.write(title+"\n");
            writer.write("标签："+tagName+"\n");
            writer.write("============================\n");
            writer.write(content);
            writer.flush();
            writer.close();
            Toast.makeText(this, "保存成功，Ciallo～(∠・ω< )", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
        }
    }

    private interface CallBack{
        void call(long ret);
    }

    private void persistNote(CallBack callBack1,CallBack callBack2)
    {
        String title = binding.noteTitleTxb.getText().toString();
        Long tag=tagId;
        String content = binding.editAreaTxb.getText().toString();
        if(isModified)
        {
            Note note=new Note();
            note.setTitle(title);
            note.setTag(tag);
            note.setContent(content);
            if(mode==MODE_APPEND)//添加笔记
            {
                new Thread(()->{
                    Long ret = saveNote(note);
                    runOnUiThread(()->{
                        isModified=false;
                        callBack1.call(ret);
                    });
                }).start();
            }
            else{
                note.setId(edid);
                new Thread(()->{
                    int ret = updateNote(note);
                    runOnUiThread(()->{
                        isModified=false;
                        callBack2.call(ret);
                    });
                }).start();
            }
        }
    }


    private Long saveNote(Note note)
    {
        return noteDao.insertNote(note);
    }

    private int updateNote(Note note)
    {
        return noteDao.updateNote(note);
    }

    private void loadNote(Long id)
    {
        NoteVO note = noteDao.getById_V2(id);
        if(note==null)
        {
            Toast.makeText(this, "未知错误，请尝试重启应用程序，如果问题依旧联系我们", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.noteTitleTxb.setText(note.title);
        binding.editAreaTxb.setText(note.content);
        tagId=note.tagId;
        lastSavedId=tagId;
        tagName=note.tagName;
        binding.listPopupButton.setText(note.tagName);

        lastSavedVO.id=id;
        lastSavedVO.tagId=tagId;
        lastSavedVO.tagName=tagName;
        lastSavedVO.title=binding.noteTitleTxb.getText().toString();
        lastSavedVO.content=binding.noteTitleTxb.getText().toString();
        lastSavedVO.createTime=note.createTime;
        lastSavedVO.updateTime=note.updateTime;
    }

    private class MyWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(before!=0||count!=0)
            {
                isModified=true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private String[] convertTagDisplayTable(List<Tag> tagList)
    {
        if(tagList==null||tagList.isEmpty())
        {
            return new String[]{"默认标签"};
        }
        String[] ret=new String[tagList.size()];
        for(int i=0;i<tagList.size();i++)
        {
            ret[i]=tagList.get(i).getTagName();
        }
        return ret;
    }

    private void reFindStatus()
    {
        isModified=viewModel.isModified();
        mode=viewModel.getMode();
        edid=viewModel.getEdid();
        tagId=viewModel.getTagId();
        tagName=viewModel.getTagName();
    }
}