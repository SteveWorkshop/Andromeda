package com.example.andromeda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andromeda.BaseApplication;
import com.example.andromeda.R;
import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.entity.Tag;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private List<Tag> tagList;

    @Getter
    @Setter
    private int mPosition=-1;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        holder.tagView.setOnClickListener(v->{

            int position=holder.getAbsoluteAdapterPosition();
            Tag tag=tagList.get(position);

            MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(parent.getContext());
            builder.setTitle("修改名称");
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View digView=layoutInflater.inflate(R.layout.tag_edit_dialog,null);

            EditText eText = digView.findViewById(R.id.txb_tag_name_edit);
            eText.setText(tag.getTagName());
            eText.setSelectAllOnFocus(true);


            builder.setView(digView);
            builder.setPositiveButton("确定",(dialog,which)->{

                String tName=eText.getText().toString();
                tag.setTagName(tName);
                //临时调动一下吧
                TagDao tagDao= DBConfig.getInstance(BaseApplication.getApplication()).getTagDao();
                tagDao.updateTag(tag);
                this.notifyItemChanged(position);
            });
            builder.setNegativeButton("取消",((dialog, which) -> {

            }));
            builder.show();
        });

        holder.itemView.setOnLongClickListener(v->{
            mPosition = holder.getAbsoluteAdapterPosition();
            return false;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag=tagList.get(position);
        String name= tag.getTagName();

        holder.tagName.setText(name);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public TagAdapter(List<Tag> tagList)
    {
        this.tagList=tagList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tagName;
        View tagView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName=itemView.findViewById(R.id.txb_tag_name);
            tagView=itemView;
        }
    }
}
