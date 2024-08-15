package com.example.andromeda.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

public class ScreenShotUtil {
    public static Bitmap createBitMapScreenSize(View view)
    {
        Bitmap bitmap=Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);//todo:修改背景色自定义功能
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap createBitMapCustomSize(View view,int width,int height)
    {
        int measureWidth=View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(measureWidth,measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        return createBitMapScreenSize(view);
    }
}
