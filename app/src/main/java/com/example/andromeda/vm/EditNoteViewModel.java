package com.example.andromeda.vm;

import androidx.lifecycle.ViewModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EditNoteViewModel extends ViewModel {

    private boolean isModified=false;
    private int mode=0;

    private long edid=-1L;

    private long tagId=1L;

    private String tagName;


    public void selectTag(long tagId,String tagName)
    {
        this.tagId=tagId;
        this.tagName=tagName;
    }

    public void setEditInfo(long edid,int mode)
    {
        this.edid=edid;
        this.mode=mode;
    }

    public void setEM(int mode)
    {
        this.mode=mode;
    }
}
