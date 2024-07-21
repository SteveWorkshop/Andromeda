package com.example.andromeda.util;

import java.util.UUID;

public class RandomUtil {
    public static String getRandomName()
    {
        return UUID.randomUUID().toString();
    }
}
