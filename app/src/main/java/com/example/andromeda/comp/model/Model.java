package com.example.andromeda.comp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Model <T>{
    public static final int PATH=0;
    public static final int IMG=1;
    private Integer type;
    private T data;
}