package com.kaijy.model;

public class Task implements Comparable<Task>, Cloneable {
    // id
    private int id;

    // 任务id
    private int taskId;

    // 原始感知时间
    private int originSenTime;

    // 剩余感知时间
    private int remainSenTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getRemainSenTime() {
        return remainSenTime;
    }

    public void setRemainSenTime(int remainSenTime) {
        this.remainSenTime = remainSenTime;
    }

    public int getOriginSenTime() {
        return originSenTime;
    }

    public void setOriginSenTime(int originSenTime) {
        this.originSenTime = originSenTime;
    }

    @Override
    public int compareTo(Task task) {
        return this.taskId - task.getTaskId();
    }

    @Override
    public Task clone() throws CloneNotSupportedException {
        return (Task) super.clone();
    }

}
