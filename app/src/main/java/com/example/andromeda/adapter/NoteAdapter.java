package com.example.andromeda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andromeda.R;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.entity.vo.NoteVO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    @Getter
    @Setter
    private int mPosition=-1;


    @Getter
    @Setter
    private CallBack callBack;

    private List<NoteVO> noteVOList;

    public NoteAdapter(List<NoteVO> noteVOList) {
        this.noteVOList = noteVOList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.noteView.setOnClickListener(v -> {
            int position = holder.getAbsoluteAdapterPosition();//这个场景下问题不大
            //还要记住是谁
            //mPosition=position;
            NoteVO note = noteVOList.get(position);
            callBack.onClick(note,position);
        });
        //长按逻辑，保存按压位置
        holder.itemView.setOnLongClickListener(v->{
            mPosition= holder.getAbsoluteAdapterPosition();
            return false;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteVO note=noteVOList.get(position);
        String title=note.title;
        String tagName=note.tagName;
        Long updateTime=note.updateTime;
        Long tagId=note.tagId;

        String strDateFormat = "yyyy/MM/dd HH:mm";
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(strDateFormat);
        Date date=new Date(updateTime);
        String convertedTime=simpleDateFormat.format(date);
        holder.title.setText(title);
        if(tagId==null|| tagId== Tag.DEFAULT_TAG||tagName==null|| tagName.isEmpty())
        {
            holder.tag.setText("无标签");
        }
        else{
            holder.tag.setText(tagName);
        }

        holder.updateTime.setText(convertedTime);
    }

    @Override
    public int getItemCount() {
        return noteVOList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView tag;
        TextView updateTime;
        View noteView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteView = itemView;
            title = itemView.findViewById(R.id.txb_title);
            tag = itemView.findViewById(R.id.txb_tag);
            updateTime = itemView.findViewById(R.id.txb_update_time);
        }
    }


    public interface CallBack{
        void onClick(NoteVO noteVO,int position);
    }
}
