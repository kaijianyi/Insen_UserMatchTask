package com.kaijy.utils;

import java.util.Random;

public class RandomUtils {

    /**
     * 随机算法
     * 
     * @param min
     * @param max
     * @return
     */
    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
