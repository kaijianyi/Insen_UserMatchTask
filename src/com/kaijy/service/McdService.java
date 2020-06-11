package com.kaijy.service;

import java.util.Iterator;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.kaijy.model.SenArea;
import com.kaijy.model.Task;
import com.kaijy.model.User;
import com.kaijy.utils.JsonUtils;

public class McdService {

    /**
     * 判断是否存在异常用户
     * 
     * @param abnormalWinnerList
     * @return
     */
    public static boolean isAbnormal(List<User> abnormalWinnerList) {
        for (User abnormalWinner : abnormalWinnerList) {
            // 获得异常用户id
            if (abnormalWinner.getCareless() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新设置竞拍任务数据
     * 
     * @param abnormalWinnerList
     * @param abnormalTaskList
     * @return
     */
    public static List<Task> getReTaskData(List<User> abnormalWinnerList, String originTaskListStr) {
        // 更新任务信息
        List<Task> reTaskList = JsonUtils.fastjsonToObj(originTaskListStr, new TypeToken<List<Task>>() {
        }.getType());

        // 任务感知时间重置为0
        for (Task reTask : reTaskList) {
            reTask.setRemainSenTime(0);
        }

        for (User abnormalWinner : abnormalWinnerList) {
            // 找到异常用户
            if (abnormalWinner.getCareless() == 1) {
                List<SenArea> allocation = abnormalWinner.getAllocation();
                for (SenArea senArea : allocation) {
                    for (Task reTask : reTaskList) {
                        // 更新异常用户关联的任务感知时间
                        if (senArea.getTaskIdList().contains(reTask.getId())) {
                            // 更新剩余时间
                            reTask.setRemainSenTime(reTask.getRemainSenTime() + senArea.getSenTime());
                        }
                    }
                }
            }
        }
        return reTaskList;
    }

    /**
     * 重新设置竞拍用户数据
     * 
     * @param abnormalWinnerList
     * @param originUserListStr
     * @return
     */
    public static List<User> getReUserData(List<User> abnormalWinnerList, String originUserListStr) {
        // 更新任务信息
        List<User> reUserList = JsonUtils.fastjsonToObj(originUserListStr, new TypeToken<List<User>>() {
        }.getType());
        for (User abnormalWinner : abnormalWinnerList) {
            // 找到异常用户
            if (abnormalWinner.getCareless() == 1) {
                Iterator<User> reUserListIter = reUserList.iterator();
                while (reUserListIter.hasNext()) {
                    User deleteUser = reUserListIter.next();
                    // 从候选集合删除异常用户
                    if (abnormalWinner.getId() == deleteUser.getId()) {
                        reUserListIter.remove();
                    }
                }
            }
        }
        return reUserList;
    }

    /**
     * 去除异常用户数据
     * 
     * @param mcdBidUserList
     * @return
     */
    // TODO 调用高德地图的API
    public static List<User> getMcdSenuser(String originUserListStr) {
        List<User> mcdBidUserList = JsonUtils.fastjsonToObj(originUserListStr, new TypeToken<List<User>>() {
        }.getType());
        Iterator<User> iteratorUser = mcdBidUserList.iterator();
        while (iteratorUser.hasNext()) {
            User deleteUser = iteratorUser.next();
            if (deleteUser.getCareless() == 1) {
                iteratorUser.remove();
            }
        }
        // 调用MCD算法时间损耗
        int waitTime = mcdBidUserList.size();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mcdBidUserList;
    }
}
