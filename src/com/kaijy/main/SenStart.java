package com.kaijy.main;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.kaijy.model.Platform;
import com.kaijy.model.PlatformTotal;
import com.kaijy.model.Task;
import com.kaijy.model.User;
import com.kaijy.service.AuctionService;
import com.kaijy.service.DataService;
import com.kaijy.service.McdService;
import com.kaijy.service.TaskService;
import com.kaijy.service.UserService;
import com.kaijy.utils.ConstsUtils;
import com.kaijy.utils.JsonUtils;
import com.kaijy.utils.NumberUtils;

public class SenStart {
    public static void main(String[] args) throws CloneNotSupportedException {

        // 汇总数据
        List<Platform> platformList = new ArrayList<Platform>();

        // 开始迭代
        for (int i = 1; i <= ConstsUtils.RUNNUM; i++) {

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ 开始生成随机数据 $$$$$$$$$$$$$$$$$$$$$");
            // 平台汇总数据
            Platform platform = new Platform();

            Instant ranTimeStart = Instant.now();

            // 产生随机任务
            List<Task> originTaskList = TaskService.getRandomTask(ConstsUtils.TASKNUM, ConstsUtils.TASKMINID,
                    ConstsUtils.TASKMAXID, ConstsUtils.TASKMINTIME, ConstsUtils.TASKMAXTIME);

            // 产生随机用户(已关联任务+生成异常用户)
            List<User> originUserList = UserService.getRandomUser(ConstsUtils.USERNUM, ConstsUtils.USERMINID,
                    ConstsUtils.USERMAXID, ConstsUtils.USERMINTIME, ConstsUtils.USERMAXTIME, ConstsUtils.USERMINBID,
                    ConstsUtils.USERMAXBID);

            // 用户Json
            String originUserListStr = JsonUtils.objToFastjson(originUserList);
            // 任务Json
            String originTaskListStr = JsonUtils.objToFastjson(originTaskList);

            // System.out.println(originUserListStr);
            // System.out.println(originTaskListStr);

            // 生成数据时间
            Instant ranTimeEnd = Instant.now();
            long ranRunTime = Duration.between(ranTimeStart, ranTimeEnd).toMillis();
            platform.setRandomRunTime(String.valueOf(ranRunTime));

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ 结束生成随机数据 $$$$$$$$$$$$$$$$$$$$$");

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ 正常拍卖开始 $$$$$$$$$$$$$$$$$$$$$");

            Instant normalTimeStart = Instant.now();

            // 由于下层服务使用深拷贝，并不会改变上层数据，所以可以直接使用originBidUserList，originTaskList
            List<User> normalWinnerList = AuctionService.startAuction(originUserListStr, originTaskListStr);

            // 正常拍卖时间
            Instant normalTimeEnd = Instant.now();
            long normalRunTime = Duration.between(normalTimeStart, normalTimeEnd).toMillis();
            platform.setNormalRunTime(String.valueOf(normalRunTime));
            // 平台正常数据
            platform = DataService.getNormalTotal(platform, normalWinnerList);

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ 正常拍卖结束 $$$$$$$$$$$$$$$$$$$$$");

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ 异常拍卖开始 $$$$$$$$$$$$$$$$$$$$$");

            Instant abnormalTimeStart = Instant.now();

            // 无需重复拍卖
            String normalWinnerListStr = JsonUtils.objToFastjson(normalWinnerList);
            List<User> abnormalWinnerList = JsonUtils.fastjsonToObj(normalWinnerListStr, new TypeToken<List<User>>() {
            }.getType());
            // 判断是否存在异常
            boolean isAbnormal = McdService.isAbnormal(abnormalWinnerList);
            // 浅拷贝
            List<User> nowWinnerList = abnormalWinnerList;
            // 本轮拍卖存在异常用户
            while (isAbnormal) {
                // 重置竞拍任务数据
                List<Task> reTaskList = McdService.getReTaskData(nowWinnerList, originTaskListStr);
                String reTaskListStr = JsonUtils.objToFastjson(reTaskList);
                List<User> reUserList = McdService.getReUserData(abnormalWinnerList, originUserListStr);
                String reUserListStr = JsonUtils.objToFastjson(reUserList);
                // 再次进行拍卖后的结果
                nowWinnerList = AuctionService.startAuction(reUserListStr, reTaskListStr);
                // 合并所有的获胜者，包括异常+正常用户
                abnormalWinnerList.addAll(nowWinnerList);
                // 判断重新拍卖后是否有异常用户
                isAbnormal = McdService.isAbnormal(nowWinnerList);
            }

            // 异常拍卖时间
            Instant abnormalTimeEnd = Instant.now();
            long abnormalRunTime = Duration.between(abnormalTimeStart, abnormalTimeEnd).toMillis();
            platform.setAbnormalRunTime(String.valueOf(abnormalRunTime));

            // 平台异常数据
            platform = DataService.getAbnormalTotal(platform, abnormalWinnerList);

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ 异常拍卖结束 $$$$$$$$$$$$$$$$$$$$$");

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ MCD拍卖开始 $$$$$$$$$$$$$$$$$$$$$");

            Instant mcdTimeStart = Instant.now();

            // 通过MCD算法去除异常用户数据
            List<User> mcdBidUserList = McdService.getMcdSenuser(originUserListStr);
            String mcdBidUserListStr = JsonUtils.objToFastjson(mcdBidUserList);
            // 进行MCD拍卖
            List<User> mcdWinnerList = AuctionService.startAuction(mcdBidUserListStr, originTaskListStr);

            // MCD拍卖时间
            Instant mcdTimeEnd = Instant.now();
            long mcdRunTime = Duration.between(mcdTimeStart, mcdTimeEnd).toMillis();
            platform.setMcdRunTime(String.valueOf(mcdRunTime));

            // 平台MCD数据
            platform = DataService.getMCDTotal(platform, mcdWinnerList);

            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$ MCD拍卖结束 $$$$$$$$$$$$$$$$$$$$$");
            System.out.println("\n~~~~~~~~~~第" + i + "轮拍卖结束~~~~~~~~~\n");

            // 存入当前汇总数据
            platformList.add(platform);
        }

        PlatformTotal platformTotal = new PlatformTotal();

        // 数据求和
        for (Platform platform : platformList) {

            // 求和，100次随机数据-运行时间
            platformTotal.setRandomRunTimeTotal(
                    NumberUtils.addStr(platformTotal.getRandomRunTimeTotal(), platform.getRandomRunTime()));

            // 求和，100次标准-感知时间
            platformTotal.setNormalTimeTotal(platformTotal.getNormalTimeTotal() + platform.getNormalTime());
            // 求和，100次标准-支付
            platformTotal
                    .setNormalPayTotal(NumberUtils.addStr(platformTotal.getNormalPayTotal(), platform.getNormalPay()));
            // 求和，100次标准-运行时间
            platformTotal.setNormalRunTimeTotal(
                    NumberUtils.addStr(platformTotal.getNormalRunTimeTotal(), platform.getNormalRunTime()));

            // 求和，100次异常-感知时间
            platformTotal.setAbnormalTimeTotal(platformTotal.getAbnormalTimeTotal() + platform.getAbnormalTime());
            // 求和，100次异常-支付
            platformTotal.setAbnormalPayTotal(
                    NumberUtils.addStr(platformTotal.getAbnormalPayTotal(), platform.getAbnormalPay()));
            // 求和，100次异常-运行时间
            platformTotal.setAbnormalRunTimeTotal(
                    NumberUtils.addStr(platformTotal.getAbnormalRunTimeTotal(), platform.getAbnormalRunTime()));

            // 求和，100次MCD-感知时间
            platformTotal.setMcdTimeTotal(platformTotal.getMcdTimeTotal() + platform.getMcdTime());
            // 求和，100次MCD-支付
            platformTotal.setMcdPayTotal(NumberUtils.addStr(platformTotal.getMcdPayTotal(), platform.getMcdPay()));
            // 求和，100次MCD-运行时间
            platformTotal.setMcdRunTimeTotal(
                    NumberUtils.addStr(platformTotal.getMcdRunTimeTotal(), platform.getMcdRunTime()));

        }

        // 平均，100次随机数据-运行时间
        platformTotal.setRandomRunTimeAve(NumberUtils.divisionStrTime(platformTotal.getRandomRunTimeTotal(),
                String.valueOf(ConstsUtils.RUNNUM * 1000)));

        // 平均，100次异常-感知时间
        platformTotal.setNormalTimeAve(NumberUtils.divisionStr(String.valueOf(platformTotal.getNormalTimeTotal()),
                String.valueOf(ConstsUtils.RUNNUM)));
        // 平均，100次异常-支付
        platformTotal.setNormalPayAve(
                NumberUtils.divisionStr(platformTotal.getNormalPayTotal(), String.valueOf(ConstsUtils.RUNNUM)));
        // 平均，100次标准-运行时间
        platformTotal.setNormalRunTimeAve(NumberUtils.divisionStrTime(platformTotal.getNormalRunTimeTotal(),
                String.valueOf(ConstsUtils.RUNNUM * 1000)));

        // 平均，100次异常-感知时间
        platformTotal.setAbnormalTimeAve(NumberUtils.divisionStr(String.valueOf(platformTotal.getAbnormalTimeTotal()),
                String.valueOf(ConstsUtils.RUNNUM)));
        // 平均，100次异常-支付
        platformTotal.setAbnormalPayAve(
                NumberUtils.divisionStr(platformTotal.getAbnormalPayTotal(), String.valueOf(ConstsUtils.RUNNUM)));
        // 平均，100次异常-运行时间
        platformTotal.setAbnormalRunTimeAve(NumberUtils.divisionStrTime(platformTotal.getAbnormalRunTimeTotal(),
                String.valueOf(ConstsUtils.RUNNUM * 1000)));

        // 平均，100次MCD-感知时间
        platformTotal.setMcdTimeAve(NumberUtils.divisionStr(String.valueOf(platformTotal.getMcdTimeTotal()),
                String.valueOf(ConstsUtils.RUNNUM)));
        // 平均，100次MCD-支付
        platformTotal.setMcdPayAve(
                NumberUtils.divisionStr(platformTotal.getMcdPayTotal(), String.valueOf(ConstsUtils.RUNNUM)));
        // 平均，100次MCD-运行时间
        platformTotal.setMcdRunTimeAve(NumberUtils.divisionStrTime(platformTotal.getMcdRunTimeTotal(),
                String.valueOf(ConstsUtils.RUNNUM * 1000)));

        System.out.println(">>>>>>>随机数据：");
        // System.out.println("随机数据总时间：" + platformTotal.getRandomRunTimeTotal());
        System.out.println("随机数据平均时间：" + platformTotal.getRandomRunTimeAve());

        System.out.println(">>>>>>>标准算法：");
        // System.out.println("全部感知时间：" + platformTotal.getNormalTimeTotal());
        // System.out.println("全部支付成本：" + platformTotal.getNormalPayTotal());
        // System.out.println("全部运行时间：" + platformTotal.getNormalRunTimeTotal());
        System.out.println("平均感知时间：" + platformTotal.getNormalTimeAve());
        System.out.println("平均支付成本：" + platformTotal.getNormalPayAve());
        System.out.println("平均运行时间：" + platformTotal.getNormalRunTimeAve());

        System.out.println(">>>>>>>异常算法：");
        // System.out.println("全部感知时间：" + platformTotal.getAbnormalTimeTotal());
        // System.out.println("全部支付成本：" + platformTotal.getAbnormalPayTotal());
        // System.out.println("全部运行时间：" + platformTotal.getAbnormalRunTimeTotal());
        System.out.println("平均感知时间：" + platformTotal.getAbnormalTimeAve());
        System.out.println("平均支付成本：" + platformTotal.getAbnormalPayAve());
        System.out.println("平均运行时间：" + platformTotal.getAbnormalRunTimeAve());

        System.out.println(">>>>>>>MCD算法：");
        // System.out.println("全部感知时间：" + platformTotal.getMcdTimeTotal());
        // System.out.println("全部支付成本：" + platformTotal.getMcdPayTotal());
        // System.out.println("全部运行时间：" + platformTotal.getMcdRunTimeTotal());
        System.out.println("平均感知时间：" + platformTotal.getMcdTimeAve());
        System.out.println("平均支付成本：" + platformTotal.getMcdPayAve());
        System.out.println("平均运行时间：" + platformTotal.getMcdRunTimeAve());

        System.out.println();

    }
}
