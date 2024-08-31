package com.example.andromeda.fragment;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andromeda.BaseApplication;
import com.example.andromeda.adapter.NoteAdapter;
import com.example.andromeda.config.DBConfig;
import com.example.andromeda.dao.NoteDao;
import com.example.andromeda.entity.vo.NoteVO;

import com.example.andromeda.ui.EditNoteActivity;
import com.example.andromeda.R;
import com.example.andromeda.databinding.FragmentNotePageBinding;
import com.example.andromeda.util.VersionUpdateUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotePage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotePage extends Fragment {

    public static final int LIST=0;
    public static final int GRID=1;

    public static final int NOTE_EXITED=0;
    public static final int EDIT_EXITED=1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    private static final String TAG = "NotePage";

    private FragmentNotePageBinding binding;

    private NoteDao noteDao;

    private List<NoteVO> noteVOList;

    private int mPosition;

    public NotePage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotePage.
     */
    // TODO: Rename and change types and number of parameters
    public static NotePage newInstance(String param1, String param2) {
        NotePage fragment = new NotePage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentNotePageBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        //
        binding.noteLoadingProgressBar.setVisibility(View.VISIBLE);
        binding.txbErrorNoteIndic.setVisibility(View.VISIBLE);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.noteToolbar);
        registerForContextMenu(binding.noteListsView);

        Activity activity = getActivity();

        //添加笔记
        binding.addNoteFab.setOnClickListener(e->{
            Intent intent=new Intent(getActivity(), EditNoteActivity.class);
            startActivityForResult(intent,NOTE_EXITED);
        });

        new Thread(()->{
            noteVOList=noteDao.getAllPreview_V2();

            activity.runOnUiThread(()->{
                binding.noteLoadingProgressBar.setVisibility(View.GONE);
                if(noteVOList==null)
                {
                    Toast.makeText(activity, "我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
                } else if (noteVOList.size()==0) {

                    binding.txbErrorNoteIndic.setText("还没有笔记，轻触右下角以创建一个");
                }
                else{
                    binding.txbErrorNoteIndic.setVisibility(View.GONE);
                    refreshView_v2();
                }
            });
        }).start();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        noteDao= DBConfig.getInstance(BaseApplication.getApplication()).getNoteDao();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case NOTE_EXITED:{
                if(resultCode==Activity.RESULT_OK)
                {
                    //应该是在这里触发刷新？
                    Log.d(TAG, "onActivityResult: 获取返回");
                    if(data!=null)
                    {
                        NoteVO ret = (NoteVO) data.getSerializableExtra("data");
                        if(ret!=null)
                        {
                            noteVOList.add(0,ret);
                            binding.noteListsView.getAdapter().notifyItemInserted(0);
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), "emmm", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case EDIT_EXITED:{
                if(resultCode==Activity.RESULT_OK)
                {
                    //获取点击的下标
                    if(data!=null)
                    {
                        NoteVO ret = (NoteVO) data.getSerializableExtra("data");
                        if(ret!=null)
                        {
                            noteVOList.set(mPosition,ret);
                            binding.noteListsView.getAdapter().notifyItemChanged(mPosition);
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), "emmm", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:{
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(binding.noteListsView);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.note_press_menu,menu);
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.note_remove_item:{
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("确认删除？");
                builder.setMessage("在当前版本中，删除笔记将永远无法恢复，未来将提供回收站功能，将来将可以恢复现在删除的笔记");
                builder.setPositiveButton("确定",(dialog,which)->{
                    int mp=((NoteAdapter)binding.noteListsView.getAdapter()).getMPosition();
                    NoteVO toBeRemove= noteVOList.get(mp);
                    Activity activity=getActivity();
                    new Thread(()->{
                        noteDao.deleteById(toBeRemove.id);
                        noteVOList.remove(mp);
                        activity.runOnUiThread(()->{
                            binding.noteListsView.getAdapter().notifyItemRemoved(mp);
                        });
                    }).start();
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.note_list_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.edit_tags:
            {
                jumpPage(this,new TagEditPage());
                break;
            }
            case R.id.back_up_notes:{
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
                {
                    //处理通知权限
                    if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
                    }
                    else {
                        //
                    }
                }
                break;
            }
            case R.id.about:{
                MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("关于此应用程序");
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View digView=layoutInflater.inflate(R.layout.about_us_info_page,null);
                TextView ver = digView.findViewById(R.id.txb_version_name);
                String verName= VersionUpdateUtil.getVerName(getContext());
                ver.setText(verName);
                builder.setView(digView);

                builder.setPositiveButton("确定",(dialog,which)->{

                });
                builder.show();
                break;
            }
            default:{
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length!=0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    
                }
                else{
                    Toast.makeText(getContext(), "由于Google培养基的机制，我们必须显示通知保证后台稳定性", Toast.LENGTH_SHORT).show();
                }
            }
            default:{break;}
        }
    }

    private void jumpPage(Fragment frag1, Fragment frag2)
    {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(!frag2.isAdded())
        {
            fragmentTransaction.replace(R.id.main_layout,frag2);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else{
            fragmentTransaction
                    // 隐藏fragment1，即当前碎片
                    .hide(frag1)
                    // 显示已经添加过的碎片，即fragment2
                    .show(frag2)
                    // 加入返回栈
                    .addToBackStack(null)
                    // 提交事务
                    .commitAllowingStateLoss();
        }
    }


    private void refreshView_v2()
    {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        //todo:自定义视图
        binding.noteListsView.setLayoutManager(layoutManager);
        NoteAdapter adapter=new NoteAdapter(noteVOList);
        //增加回调
        adapter.setCallBack((noteVO, position) -> {
            mPosition=position;
            Long id=noteVO.id;
            Intent intent = new Intent(NotePage.this.getContext(), EditNoteActivity.class);
            intent.putExtra("edit_id", id);
            startActivityForResult(intent, NotePage.EDIT_EXITED);
        });
        binding.noteListsView.setAdapter(adapter);
    }
}