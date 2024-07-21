package com.example.andromeda.pages;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.andromeda.R;
import com.example.andromeda.adapter.TagAdapter;
import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.TagDao;
import com.example.andromeda.databinding.FragmentTagEditPageBinding;
import com.example.andromeda.entity.Tag;
import com.example.andromeda.service.TagService;
import com.example.andromeda.service.impl.TagServiceImpl;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TagEditPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagEditPage extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "TagEditPage";
    
    private FragmentTagEditPageBinding binding;

    //private TagDao tagDao;
    private TagService tagService;

    private List<Tag> tagList;

    private Long procId=-1l;

    public TagEditPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TagEditPage.
     */
    // TODO: Rename and change types and number of parameters
    public static TagEditPage newInstance(String param1, String param2) {
        TagEditPage fragment = new TagEditPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentTagEditPageBinding.inflate(inflater,container,false);

        View view=binding.getRoot();

        binding.addTagFab.setOnClickListener(e->{

            MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
            builder.setTitle("新建标签");

            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View digView=layoutInflater.inflate(R.layout.tag_edit_dialog,null);
            builder.setView(digView);
            builder.setPositiveButton("确定", (dialog,which)->{
                EditText eText = digView.findViewById(R.id.txb_tag_name_edit);
                if(eText!=null)
                {
                    String x=eText.getText().toString();
                    Log.d(TAG, "onCreateView: "+x);
                    Tag tag=new Tag();
                    tag.setTagName(x);
                    Long l = tagService.addTag(tag);
                    tag.setId(l);
                    //注意！这里不可以重新查询，否则会报不一致错误，必须手动维护一致性！
                    tagList.add(0,tag);
                    //插入的一定在最前面
                    binding.tagListsView.getAdapter().notifyItemInserted(0);
                }
            });
            builder.setNegativeButton("取消",(dialog,which)->{

            });
            builder.show();
        });

        tagList=tagService.getAll();
        refreshView();


        registerForContextMenu(binding.tagListsView);
        return view;
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.tag_press_menu, menu);
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.tag_remove_item:{
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("确认删除？");
                builder.setMessage("删除此标签为不可逆操作，如果当前有笔记使用此标签，则无法删除此标签");
                builder.setPositiveButton("确定",(dialog,which)->{
                    int mp=((TagAdapter)binding.tagListsView.getAdapter()).getMPosition();
                    Tag toBeRemove=tagList.get(mp);

                    if(tagService.checkIfUse(toBeRemove.getId())){
                        Toast.makeText(getContext(), "不能删除这个标签，因为正在被使用", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        tagService.deleteById(toBeRemove.getId());
                        tagList.remove(mp);
                        binding.tagListsView.getAdapter().notifyItemRemoved(mp);
                    }

                });
                builder.setNegativeButton("取消",(dialog,which)->{

                });
                builder.show();
                break;
            }
            default:{break;}
        }


        return super.onContextItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //tagDao= DBConfig.getInstance(context.getApplicationContext()).getTagDao();
        tagService= TagServiceImpl.getInstance(context);
    }


    private void refreshView()
    {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        binding.tagListsView.setLayoutManager(layoutManager);
        TagAdapter adapter=new TagAdapter(tagList);



        binding.tagListsView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(binding.tagListsView);
    }
}