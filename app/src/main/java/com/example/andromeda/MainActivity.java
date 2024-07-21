package com.example.andromeda;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.andromeda.databinding.ActivityMainBinding;
import com.example.andromeda.pages.DrawPage;
import com.example.andromeda.pages.NotePage;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navigationRail.setOnItemSelectedListener(item->{
            switch(item.getItemId()){
                case R.id.notes_list:{
                    switchPage(new NotePage());
                    return true;
                }
                case R.id.draw_pad:{
                    switchPage(new DrawPage());
                    return true;
                }
                default:{
                    break;
                }
            }
            return false;
        });

        binding.navigationRail.setSelectedItemId(R.id.notes_list);
    }

    private void switchPage(Fragment page)
    {
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.main_layout,page);
        transaction.commit();
    }
}