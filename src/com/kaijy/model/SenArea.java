package com.kaijy.model;

import java.util.ArrayList;
import java.util.List;

public class SenArea implements Comparable<SenArea>, Cloneable {

    // id
    private int id;
    // 感知区域
    private List<Integer> taskIdList;
    // 分配的感知时间
    private int senTime;

    public SenArea() {
        this.taskIdList = new ArrayList<Integer>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getTaskIdList() {
        return taskIdList;
    }

    public void setTaskIdList(List<Integer> taskIdList) {
        this.taskIdList = taskIdList;
    }

    public int getSenTime() {
        return senTime;
    }

    public void setSenTime(int senTime) {
        this.senTime = senTime;
    }

    @Override
    public int compareTo(SenArea senArea) {
        return this.taskIdList.size() - senArea.getTaskIdList().size();
    }

    @Override
    public SenArea clone() throws CloneNotSupportedException {
        return (SenArea) super.clone();
    }
}
