package com.example.andromeda.util;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.andromeda.R;

public class PageUtil {
    public static void jumpPage(FragmentManager fragmentManager, Fragment frag1, Fragment frag2)
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(!frag2.isAdded())
        {
            fragmentTransaction.replace(R.id.main_layout,frag2);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else{
            // 隐藏fragment1，即当前碎片
            if(frag1!=null)
            {
                fragmentTransaction.hide(frag1);
            }
            // 显示已经添加过的碎片，即fragment2
            fragmentTransaction.show(frag2);
            // 加入返回栈
            fragmentTransaction.addToBackStack(null);
            // 提交事务
            fragmentTransaction.commitAllowingStateLoss();
        }
    }
}
