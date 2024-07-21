package com.example.andromeda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.util.StringUtil;

import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.databinding.ActivityEditNoteBinding;
import com.example.andromeda.entity.Note;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.entity.dto.NoteDTO;
import com.example.andromeda.entity.vo.NoteVO;
import com.example.andromeda.service.NoteService;
import com.example.andromeda.service.TagService;
import com.example.andromeda.service.impl.NoteServiceImpl;
import com.example.andromeda.service.impl.TagServiceImpl;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

public class EditNoteActivity extends AppCompatActivity {
    private static final String TAG = "EditNoteActivity";

    public static final int MODE_EDIT=0;
    public static final int MODE_APPEND=1;

    private boolean isModified=false;
    private int mode=0;
    private ActivityEditNoteBinding binding;
    private Long edid=-1L;

    private Long tagId=1L;

    private String tagName;


    private NoteService noteService;
    private TagService tagService;

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


        noteService= NoteServiceImpl.getInstance(this.getApplicationContext());
        tagService= TagServiceImpl.getInstance(this.getApplicationContext());

        tagListForSelection=tagService.getAll();

        String[] labels=convertTagDisplayTable(tagListForSelection);

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_popup_window_item, labels);
        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAnchorView(binding.listPopupButton);

        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name=tagListForSelection.get(position).getTagName();
                Long aid=tagListForSelection.get(position).getId();
                tagId=aid;
                tagName=name;
                binding.listPopupButton.setText(name);
                listPopupWindow.dismiss();
            }

        });

        binding.listPopupButton.setOnClickListener (v -> listPopupWindow.show());

        Intent intent=getIntent();
        Long id=(Long)intent.getSerializableExtra("edit_id");
        if(id!=null)
        {
            //切换为编辑模式
            mode=MODE_EDIT;
            edid=id;
            loadNote(edid);
            MyWatcher myWatcher = new MyWatcher();            
            binding.noteTitleTxb.addTextChangedListener(myWatcher);
            binding.editAreaTxb.addTextChangedListener(myWatcher);
        }
        else {
            mode=MODE_APPEND;
            //do nothing
        }

        
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        String title = binding.noteTitleTxb.getText().toString();
        Long tag=tagId;
        String content = binding.editAreaTxb.getText().toString();
        Intent intent=new Intent();
        if(mode==MODE_APPEND)//添加笔记
        {
            Note note=new Note();
            note.setTitle(title);
            note.setTag(tag);
            note.setContent(content);
            Long nwid = saveNote(note);
            intent.putExtra("mode",MODE_APPEND);
            if(nwid>0)
            {
                note.setId(nwid);
                //名称放进去
                NoteVO vo=new NoteVO();
                vo.id=nwid;
                vo.tagId=tagId;
                vo.tagName=tagName;
                vo.createTime=note.getCreateTime();
                vo.updateTime=note.getUpdateTime();
                vo.title=note.getTitle();
                vo.content=note.getContent();

                intent.putExtra("data",vo);
                setResult(RESULT_OK,intent);
            }
            else{
                Toast.makeText(this, "喔唷，崩溃了", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
            }
        }
        else{//修改笔记
            if(isModified)
            {
                Toast.makeText(this, edid+", 检测到更改！", Toast.LENGTH_SHORT).show();
                //todo:异常处理
                Note note=new Note();
                note.setTitle(title);
                note.setTag(tag);
                note.setContent(content);
                note.setId(edid);
                intent.putExtra("mode",MODE_EDIT);

                NoteVO vo=new NoteVO();
                vo.id=edid;
                vo.tagId=tagId;
                vo.tagName=tagName;
                vo.createTime=note.getCreateTime();
                vo.updateTime=System.currentTimeMillis();
                vo.title=title;
                vo.content=content;


                int rows = updateNote(note);
                if(rows>0)
                {
                    intent.putExtra("data",vo);
                    setResult(RESULT_OK,intent);
                }
                else{
                    Toast.makeText(this, "喔唷，崩溃了", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                }
            }
            else{
                //什么都没做
                setResult(RESULT_OK);
            }
        }
        //必须返回一个数据
        finish();
    }

    private Long saveNote(Note note)
    {
        NoteDTO note1=new NoteDTO();
        note1.setTag(note.getTag());
        note1.setContent(note.getContent());
        note1.setTitle(note.getTitle());
        note1.setCreateTime(note.getCreateTime());
        note1.setUpdateTime(note.getUpdateTime());
        return noteService.insertNote(note1);
    }

    private int updateNote(Note note)
    {

        NoteDTO note1=new NoteDTO();
        note1.setId(note.getId());
        note1.setTag(note.getTag());
        note1.setContent(note.getContent());
        note1.setTitle(note.getTitle());
        note1.setCreateTime(note.getCreateTime());
        note1.setUpdateTime(note.getUpdateTime());
        return noteService.updateNote(note1);
    }

    private void loadNote(Long id)
    {
        NoteVO note = noteService.getById(id);
        if(note==null)
        {
            Toast.makeText(this, "未知错误，请尝试重启应用程序，如果问题依旧联系我们", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.noteTitleTxb.setText(note.title);
        binding.editAreaTxb.setText(note.content);
        tagId=note.tagId;
        binding.listPopupButton.setText(note.tagName);
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
            return new String[0];
        }
        String[] ret=new String[tagList.size()];
        for(int i=0;i<tagList.size();i++)
        {
            ret[i]=tagList.get(i).getTagName();
        }
        return ret;
    }
}