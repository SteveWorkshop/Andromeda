package com.example.andromeda.ui.flyout;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.andromeda.R;


//todo: Notice that this may be a tricky solution so current version wont enable this
//if you have better ideas please make pull request!
public class PenMenuFlyout extends LinearLayout {
    private Integer color;


    public PenMenuFlyout(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.flyout_pen_menu,this);
    }
}
