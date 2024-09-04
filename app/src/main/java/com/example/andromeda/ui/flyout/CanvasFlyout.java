package com.example.andromeda.ui.flyout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.andromeda.ui.flyout.model.ImagePath;
import com.example.andromeda.ui.flyout.model.Model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class CanvasFlyout extends View {

    //TODO:触控笔优化
    public static final int TOUCH_MODE=0;
    public static final int PEN_MODE=1;
    public static final int MOUSE_MODE=2;
    public static final int MIX_MODE=3;

    private int mode=0;

    @Setter
    @Getter
    private int paintColor=Color.CYAN;

    @Setter
    @Getter
    private Float strokeSize=5f;

    @Getter
    @Setter
    private int backGround =Color.WHITE;

    //todo: support more customize features
    private Paint paint=new Paint();
    private Path path=null;

    private List<Model> paths=new ArrayList<>();

    private List<CanvasFlyout.LinePath> pathSaveList=new ArrayList<>();

    public CanvasFlyout(Context context) {
        super(context);
        init();
    }

    public CanvasFlyout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init()
    {
        paint.setColor(paintColor);
        paint.setStrokeWidth(strokeSize);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        this.setBackGround(backGround);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
//        if(pathSaveList.size()>0)
//        {
//            for(LinePath linePath:pathSaveList)
//            {
//                paint.setColor(linePath.color);
//                paint.setStrokeWidth(linePath.size);
//                canvas.drawPath(linePath.path,paint);
//            }
//            if(path!=null)
//            {
//                paint.setColor(paintColor);
//                paint.setStrokeWidth(strokeSize);
//                canvas.drawPath(path,paint);
//            }
//        }

        //new rendering engine to support insert pictures
        if(paths.size()>0)
        {
            for(Model path:paths)
            {
                if(path.getType()==Model.PATH)
                {
                    LinePath linePath=(LinePath) path.getData();
                    paint.setColor(linePath.color);
                    paint.setStrokeWidth(linePath.size);
                    canvas.drawPath(linePath.path,paint);
                }
                else{
                    //绘图
                    ImagePath imagePath=(ImagePath)path.getData();
                    Bitmap bitmap=imagePath.getBitmap();
                    //todo:调整大小
                    canvas.drawBitmap(bitmap,0,0,paint);
                }
            }

            if(path!=null)
            {
                paint.setColor(paintColor);
                paint.setStrokeWidth(strokeSize);
                canvas.drawPath(path,paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //触摸事件
        //System.out.println("____________________touched!"+event.getToolMajor());
        //todo:判断是触控笔
        if(canOperate(event.getToolMajor(),mode))
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:{
                    path=new Path();
                    path.moveTo(event.getX(),event.getY());
                    break;
                }
                case MotionEvent.ACTION_MOVE:{
                    if(path!=null)
                    {
                        path.lineTo(event.getX(),event.getY());//实际效果取决于触控采样率
                        invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    if(path!=null)
                    {
                        LinePath linePath=new LinePath();
                        linePath.path=path;
                        linePath.color=paintColor;
                        linePath.size=strokeSize;
                       // pathSaveList.add(linePath);

                        Model model=new Model();
                        model.setType(Model.PATH);
                        model.setData(linePath);
                        paths.add(model);
                    }
                    path=null;
                    break;
                }
            }
        }
        return true;
    }

    //提供对外API
    //todo：自定义或自适应图片尺寸
    public  void addBitMap(Bitmap bitmap)
    {
        ImagePath imagePath=new ImagePath();
        imagePath.setBitmap(bitmap);

        Model model=new Model();
        model.setType(Model.IMG);

        model.setData(imagePath);
        paths.add(model);
        invalidate();
    }

    //todo:支持redo功能
    public void undo(){
        if(!paths.isEmpty())
        {
            //pathSaveList.remove(pathSaveList.size()-1);
            paths.remove(paths.size()-1);
            invalidate();
        }
    }

    public void undo(int steps){
        if(steps>=0)
        {
            for(int i=0;i<steps;i++)
            {
                undo();
            }
        }
    }


    public void clearAll()
    {
        //pathSaveList.clear();
        paths.clear();
        invalidate();
    }

    //I hope devices will follow this standard, if it does not work, then I have no way to solve.
    //todo: better handle input device type
    private boolean canOperate(float type,int mode)
    {
//        if(mode==TOUCH_MODE)
//        {
//            return type==MotionEvent.TOOL_TYPE_FINGER;
//        }
//        if(mode==PEN_MODE)
//        {
//            return type==MotionEvent.TOOL_TYPE_STYLUS;
//        }
//        if (mode==MOUSE_MODE)
//        {
//            return type==MotionEvent.TOOL_TYPE_MOUSE;
//        }
        return true;//混合模式
    }

    class LinePath{
        Path path;
        Integer color;
        Float size;
    }


}
