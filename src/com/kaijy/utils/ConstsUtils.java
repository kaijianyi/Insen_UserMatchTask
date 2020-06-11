package com.kaijy.utils;

import java.util.Date;

public class ConstsUtils {
    // 总执行次数
    public static final int RUNNUM = 1;
    // BP值
    public static final float BREAKPOINT = 0.25f;
    // 关联任务数
    public static final int AOI = 3;
    // 随机任务数
    public static final int TASKNUM = 4;
    // 随机用户数
    public static final int USERNUM = 25;
    // 任务最小感知时间
    public static final int TASKMINTIME = 5;
    // 任务最大感知时间
    public static final int TASKMAXTIME = 15;

    // 开始时间
    public static final Date STARTTIME = TimeUtils.string2Date("2008-02-02 14:00:00");
    // 结束时间
    public static final Date ENDTIME = TimeUtils.string2Date("2008-02-02 15:00:00");
    // 异常用户开始时间
    public static final Date CARELESSSTARTTIME = TimeUtils.string2Date("2008-02-03 01:00:00");
    // 异常用户结束时间
    public static final Date CARELESSENDTIME = TimeUtils.string2Date("2008-02-03 23:00:00");

    // 任务最小编号
    public static final int TASKMINID = 1;
    // 任务最大编号
    public static final int TASKMAXID = 730;

    // 用户最小编号
    public static final int USERMINID = 1;
    // 用户最大编号
    public static final int USERMAXID = 10357;
    // 用户最小感知时间
    public static final int USERMINTIME = 5;
    // 用户最大感知时间
    public static final int USERMAXTIME = 10;
    // 用户最小bid
    public static final int USERMINBID = 6;
    // 用户最大bid
    public static final int USERMAXBID = 10;
}
