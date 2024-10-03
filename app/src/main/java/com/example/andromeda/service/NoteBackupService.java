package com.example.andromeda.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.andromeda.R;
import com.example.andromeda.service.task.BackupServiceListener;
import com.example.andromeda.service.task.BackupTask;
import com.example.andromeda.service.task.RestoreTask;
import com.example.andromeda.ui.MainActivity;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;

public class NoteBackupService extends Service {

    private BackupTask backupTask;
    private RestoreTask restoreTask;

    private BackupBinder mBinder=new BackupBinder();


    private BackupServiceListener backupListener=new BackupServiceListener() {
        @Getter
        @Setter
        private boolean mode;


//        public void setMode(boolean mode) {
//            this.mode = mode;
//        }

        @Override
        public void onPrep() {
        }

        @Override
        public void onProgress(int progress) {
            
            getNotificationManager().notify(1,getNotification((mode==MODE_BACKUP ?"备份":"恢复")+"中",progress));
        }

        @Override
        public void onSuccess() {
            backupTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification((mode==MODE_BACKUP?"已备份到下载文件夹，请使用系统自带文件管理器查看":"已完成恢复，可以继续使用软件了"),-1));
        }

        @Override
        public void onFailure() {
            backupTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("我们需要一种清洁植物燃料，请通过内部集线器联系我们",-1));
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class BackupBinder extends Binder{
        public void startBackup()
        {
            backupTask=new BackupTask();
            backupListener.setMode(BackupServiceListener.MODE_BACKUP);
            backupTask.setListener(backupListener);
            backupTask.execute("");

        }

        public void startRestore(InputStream inputStream)
        {
            restoreTask=new RestoreTask();
            backupListener.setMode(BackupServiceListener.MODE_RESTORE);
            restoreTask.setListener(backupListener);
            restoreTask.setInputStream(inputStream);
            restoreTask.execute("");
        }
    }

    private NotificationManager getNotificationManager()
    {
        NotificationManager manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel=new NotificationChannel("my_service","备份恢复服务",NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);
        return manager;
    }

    private Notification getNotification(String title,int stage)
    {
        Intent intent=new Intent(this, MainActivity.class);//无所谓
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_service);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_service));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if(stage>0)
        {
            builder.setContentText("第"+stage+"阶段已完成，成，成，成，成");
            
        }
        return builder.build();
    }
}
