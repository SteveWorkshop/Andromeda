package com.example.andromeda.ui.flyout.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagePath {
    private String fileName;//todo：如果要持久化保存，那么这里该记录什么？
    private Bitmap bitmap;
    private Rect fileSize;
    private Rect imgSize;
}
