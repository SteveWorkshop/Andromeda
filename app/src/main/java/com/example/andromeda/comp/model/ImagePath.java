package com.example.andromeda.comp.model;

import android.graphics.Rect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagePath {
    private String fileName;
    private Rect fileSize;
    private Rect imgSize;
}
