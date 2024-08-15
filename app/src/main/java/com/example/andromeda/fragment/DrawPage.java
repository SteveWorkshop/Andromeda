package com.example.andromeda.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.andromeda.R;
import com.example.andromeda.adapter.ColorAdapter;
import com.example.andromeda.ui.flyout.CanvasFlyout;
import com.example.andromeda.databinding.FragmentDrawPageBinding;
import com.example.andromeda.util.RandomUtil;
import com.example.andromeda.util.ScreenShotUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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


    public static final int TAKE_PHOTO = 1;
    public static final int SELECT_FILE = 2;


    private FragmentDrawPageBinding binding;

    private CanvasFlyout canvasFlyout;

    private int colorSelected = Color.CYAN;

    private Uri imageUri;

    private List<ColorAdapter.ColorTag> colorList;

    public DrawPage() {
        // Required empty public constructor
        colorList = new ArrayList<>();
        colorList.add(new ColorAdapter.ColorTag("青色", Color.CYAN));
        colorList.add(new ColorAdapter.ColorTag("红色", Color.RED));
        colorList.add(new ColorAdapter.ColorTag("淡蓝", R.color.aqua));
        colorList.add(new ColorAdapter.ColorTag("绿色", R.color.green));
        colorList.add(new ColorAdapter.ColorTag("黑色", R.color.black));
        colorList.add(new ColorAdapter.ColorTag("蓝色", R.color.blue));
        colorList.add(new ColorAdapter.ColorTag("粉色", R.color.hotpink));
        colorList.add(new ColorAdapter.ColorTag("紫色", R.color.blueviolet));
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
        binding = FragmentDrawPageBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.drawToolbar);

        canvasFlyout = binding.canvas;

        binding.btnPenI.setOnClickListener(v -> {
            showPopupMenu(binding.btnPenI);
        });


        binding.btnEraserI.setOnClickListener(v -> {
            if (binding.btnEraserI.isSelected()) {
                //todo: 记住上一个选择颜色
                canvasFlyout.setPaintColor(Color.CYAN);
                binding.btnEraserI.setSelected(false);
            } else {
                canvasFlyout.setPaintColor(canvasFlyout.getBackGround());
                binding.btnEraserI.setSelected(true);
            }
        });

        binding.btnCleanI.setOnClickListener(v -> {
            canvasFlyout.clearAll();
        });

        binding.btnUndoI.setOnClickListener(v -> {
            canvasFlyout.undo();
        });

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Ninja Cat");
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View digView = layoutInflater.inflate(R.layout.warning_dialog, null);
        builder.setView(digView);
        builder.setPositiveButton("确定", (dialog, which) -> {
        });
        builder.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.draw_export_menu, menu);
    }

    @SuppressLint("WrongThread")//无视风险，继续调用（doge）
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        View pic = binding.canvas;
        Bitmap bitmap = ScreenShotUtil.createBitMapScreenSize(pic);
        switch (item.getItemId()) {
            case R.id.album_exp_menu: {
                //Toast.makeText(getContext(), "Ciallo", Toast.LENGTH_SHORT).show();
                String fileName = RandomUtil.getRandomName() + ".png";
                saveBitMapOnDisk(bitmap, fileName);
                break;
            }
            case R.id.share_exp_munu: {
                //仅在内部缓存处理
                try {
                    String fileName = RandomUtil.getRandomName() + ".png";
                    String tmpp = getActivity().getCacheDir().getAbsolutePath() + "/" + fileName;
                    File tmpf = new File(tmpp);
                    if (tmpf.exists()) {
                        tmpf.delete();
                    }
                    tmpf.createNewFile();
                    FileOutputStream fs=new FileOutputStream(tmpf);
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,fs);//？？
                    fs.flush();
                    fs.close();
                    //这样会挂
                    //todo：老系统可以用Uri.fromFile(tmpf)
                    Uri uri=FileProvider.getUriForFile(getContext(),"com.example.andromeda.fileprovider",tmpf);
                    //打开分享窗口
                    Intent shareIntent=new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/**");
                    shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                    Intent chooser = Intent.createChooser(shareIntent, "将手稿发送到");
                    getActivity().startActivity(chooser);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "哦我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            }
            default: {
                break;
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private void saveBitMapOnDisk(Bitmap bitmap, String fileName) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        //todo:安卓9以下要不要照顾？
        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        //插入数据库后通过系统获取文件句柄
        if (uri != null) {
            try {
                OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                if (os != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.close();
                }
                Toast.makeText(getContext(), "已保存到DCIM文件夹", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "哦我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "哦我们都有不顺利的时候", Toast.LENGTH_SHORT).show();
        }

    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.pen_select_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                //效率有点低等优化
                case R.id.size_1px: {
                    canvasFlyout.setStrokeSize(1f);
                    break;
                }
                case R.id.size_5px: {
                    canvasFlyout.setStrokeSize(5f);
                    break;
                }
                case R.id.size_10px: {
                    canvasFlyout.setStrokeSize(10f);
                    break;
                }
                case R.id.size_more: {
                    showSizeSelector();
                    break;
                }
                case R.id.popup_picker: {
                    shoColorPicker();
                    break;
                }
                default: {
                    break;
                }
            }
            return false;
        });

        popupMenu.show();
    }

    private void showSizeSelector() {
        EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("设置笔画半径");
        builder.setView(editText);
        builder.setPositiveButton("确定", (dialog, which) -> {
            Float value = Float.parseFloat(editText.getText().toString());
            canvasFlyout.setStrokeSize(value);
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            //do nothing
        });
        builder.show();
    }

    private void shoColorPicker() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("选择画笔颜色");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.flyout_simple_color_picker_v2, null);
        builder.setView(dialogView);
        RecyclerView list = dialogView.findViewById(R.id.color_pane);
        loadColorPane(list);
        builder.setPositiveButton("确定", (dialog, which) -> {
            ColorAdapter adapter = (ColorAdapter) list.getAdapter();
            if (adapter != null) {
                int mPosition = adapter.getMPosition();
                if (mPosition >= 0) {
                    ColorAdapter.ColorTag tag = colorList.get(mPosition);
                    colorSelected = tag.getValue();
                    canvasFlyout.setPaintColor(colorSelected);
                }
            }
        });
        builder.setNegativeButton("取消", ((dialog, which) -> {

        }));
        builder.show();
    }

    private void loadColorPane(RecyclerView view) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        view.setLayoutManager(layoutManager);
        ColorAdapter adapter = new ColorAdapter(colorList);
        view.setAdapter(adapter);
    }

    private void selectInsertSource(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.insert_source_selector, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.select_from_camera: {
                    String fileName = RandomUtil.getRandomName();


                    File outputFile = new File(getActivity().getExternalCacheDir(), fileName);
                    try {
                        if (outputFile.exists()) {
                            outputFile.delete();
                        }
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "喔唷，崩溃了" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    imageUri = FileProvider.getUriForFile(getActivity(), "com.example.andromeda.fileprovider", outputFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, TAKE_PHOTO);
                    break;
                }
                case R.id.select_from_file: {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        //todo:显示图片


                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "喔唷，崩溃了" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
                default: {
                    break;
                }
            }
            return false;
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO: {
                if (resultCode == Activity.RESULT_OK) {

                }


                break;
            }

            default: {
                break;
            }
        }
    }
}