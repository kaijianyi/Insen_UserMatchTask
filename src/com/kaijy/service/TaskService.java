package com.kaijy.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kaijy.model.Task;
import com.kaijy.utils.RandomUtils;

public class TaskService {

    /**
     * 生成随机任务
     * 
     * @param taskNum
     * @param taskMinId
     * @param taskMaxId
     * @param taskMinTime
     * @param taskMaxTime
     * @return
     */
    public static List<Task> getRandomTask(int taskNum, int taskMinId, int taskMaxId, int taskMinTime,
            int taskMaxTime) {
        List<Task> taskList = new ArrayList<Task>();
        // 防止生成重复数字
        List<Integer> existList = new ArrayList<Integer>();
        while (taskList.size() < taskNum) {
            Task task = new Task();
            // 任务id范围是[1，730]
            int taskId = RandomUtils.getRandom(taskMinId, taskMaxId);
            if (!existList.contains(taskId)) {
                existList.add(taskId);
                task.setTaskId(taskId);
                int needTime = RandomUtils.getRandom(taskMinTime, taskMaxTime);
                task.setOriginSenTime(needTime);
                task.setRemainSenTime(needTime);
                taskList.add(task);
            }
        }

        // 按照taskId升序
        Collections.sort(taskList);
        // 排序后编号
        for (int j = 0; j < taskList.size(); j++) {
            taskList.get(j).setId(j);
        }
        return taskList;
    }

}
