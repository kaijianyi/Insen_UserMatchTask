package com.kaijy.service;

import java.util.List;

import com.kaijy.model.Platform;
import com.kaijy.model.SenArea;
import com.kaijy.model.User;
import com.kaijy.utils.NumberUtils;

public class DataService {

    /**
     * 获得正常汇总数据
     * 
     * @param platform
     * @param normalWinnerList
     * @return
     */
    public static Platform getNormalTotal(Platform platform, List<User> normalWinnerList) {
        for (User normal : normalWinnerList) {
            int senTimeArea = 0;
            for (SenArea senArea : normal.getAllocation()) {
                senTimeArea += senArea.getSenTime();
            }
            platform.setNormalTime(platform.getNormalTime() + senTimeArea);
            platform.setNormalPay(NumberUtils.addStr(platform.getNormalPay(), normal.getPay()));
        }
        return platform;
    }

    /**
     * 
     * @param platform
     * @param abnormalWinnerList
     * @return
     */
    public static Platform getAbnormalTotal(Platform platform, List<User> abnormalWinnerList) {
        for (User abnormal : abnormalWinnerList) {
            int senTimeArea = 0;
            for (SenArea senArea : abnormal.getAllocation()) {
                senTimeArea += senArea.getSenTime();
            }
            platform.setAbnormalTime(platform.getAbnormalTime() + senTimeArea);
            // 只统计非异常用户支付
            if (abnormal.getCareless() == 0) {
                platform.setAbnormalPay(NumberUtils.addStr(platform.getAbnormalPay(), abnormal.getPay()));
            }
        }
        return platform;

    }

    /**
     * 获取MCD数据
     * 
     * @param platform
     * @param McdWinnerList
     * @return
     */
    public static Platform getMCDTotal(Platform platform, List<User> McdWinnerList) {
        for (User mcd : McdWinnerList) {
            int senTimeArea = 0;
            for (SenArea senArea : mcd.getAllocation()) {
                senTimeArea += senArea.getSenTime();
            }
            platform.setMcdTime(platform.getMcdTime() + senTimeArea);
            platform.setMcdPay(NumberUtils.addStr(platform.getMcdPay(), mcd.getPay()));
        }
        return platform;
    }

}
