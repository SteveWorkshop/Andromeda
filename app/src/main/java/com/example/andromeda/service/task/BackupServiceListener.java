package com.example.andromeda.service.task;

public interface BackupServiceListener {
    boolean MODE_BACKUP=false;
    boolean MODE_RESTORE=true;
    void setMode(boolean mode);
    void onPrep();
    void onProgress(int progress);
    void onSuccess();
    void onFailure();
}
