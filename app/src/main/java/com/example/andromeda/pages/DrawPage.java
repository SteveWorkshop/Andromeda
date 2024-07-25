package com.example.andromeda.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.andromeda.R;
import com.example.andromeda.comp.CanvasFlyout;
import com.example.andromeda.databinding.FragmentDrawPageBinding;
import com.example.andromeda.util.RandomUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DrawPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DrawPage extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public static final int TAKE_PHOTO=1;
    public static final int SELECT_FILE=2;



    private FragmentDrawPageBinding binding;

    private CanvasFlyout canvasFlyout;

    private int colorSelected=Color.CYAN;

    private Uri imageUri;

    public DrawPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DrawPage.
     */
    // TODO: Rename and change types and number of parameters
    public static DrawPage newInstance(String param1, String param2) {
        DrawPage fragment = new DrawPage();
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
        binding=FragmentDrawPageBinding.inflate(inflater,container,false);
        View view=binding.getRoot();
        canvasFlyout=binding.canvas;

        binding.btnPenI.setOnClickListener(e->{
            showPopupMenu(binding.btnPenI);
        });


        binding.btnEraserI.setOnClickListener(e->{
            if(binding.btnEraserI.isSelected())
            {
                //todo: 记住上一个选择颜色
                canvasFlyout.setPaintColor(Color.CYAN);
                binding.btnEraserI.setSelected(false);
            }
            else{
                canvasFlyout.setPaintColor(canvasFlyout.getBackGround());
                binding.btnEraserI.setSelected(true);
            }
        });

        return view;
    }


    private void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.pen_select_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item->{
            switch (item.getItemId())
            {
                //效率有点低等优化
                case R.id.size_1px:{
                    canvasFlyout.setStrokeSize(1f);
                    break;
                }
                case R.id.size_5px:{
                    canvasFlyout.setStrokeSize(5f);
                    break;
                }
                case R.id.size_10px:{
                    canvasFlyout.setStrokeSize(10f);
                    break;
                }
                case R.id.size_more:{
                    showSizeSelector();
                    break;
                }
                default:{
                    break;
                }
            }
            return false;
        });

        popupMenu.show();
    }

    private void showSizeSelector()
    {
        EditText editText=new EditText(getContext());
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("设置笔画半径");
        builder.setView(editText);
        builder.setPositiveButton("确定", (dialog, which) -> {
            Float value=Float.parseFloat(editText.getText().toString());
            canvasFlyout.setStrokeSize(value);
        });
        builder.setNegativeButton("取消",(dialog,which)->{
            //do nothing
        });
        builder.show();
    }

    private void selectInsertSource(View view)
    {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.insert_source_selector,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item->{
            switch(item.getItemId())
            {
                case R.id.select_from_camera:{
                    String fileName= RandomUtil.getRandomName();


                    File outputFile=new File(getActivity().getExternalCacheDir(),fileName);
                    try {
                        if(outputFile.exists())
                        {
                            outputFile.delete();
                        }
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "喔唷，崩溃了"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    imageUri= FileProvider.getUriForFile(getActivity(),"com.example.andromeda.fileprovider",outputFile);
                    Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(intent,TAKE_PHOTO);
                    break;
                }
                case R.id.select_from_file:{
                    try {
                        Bitmap bitmap=BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        //todo:显示图片


                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "喔唷，崩溃了"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
                default:{
                    break;
                }
            }
            return false;
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case TAKE_PHOTO:{
                if(resultCode== Activity.RESULT_OK){

                }


                break;
            }

            default:{
                break;
            }
        }
    }
}