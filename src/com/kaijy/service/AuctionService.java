package com.kaijy.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.kaijy.model.SenArea;
import com.kaijy.model.Task;
import com.kaijy.model.User;
import com.kaijy.utils.JsonUtils;
import com.kaijy.utils.NumberUtils;

public class AuctionService {

    /**
     * 开始入口
     * 
     * @param originBidUserList
     * @param originTaskList
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<User> startAuction(String originUserListStr, String originTaskListStr)
            throws CloneNotSupportedException {
        // 返回值
        List<User> winnerList = new ArrayList<User>();

        System.out.println("\n$$$$$$$$$$$$$$$$$ winnerSelection开始 $$$$$$$$$$$$$$$$");
        winnerList = winnerSelection(originTaskListStr, originUserListStr);
        System.out.println("$$$$$$$$$$$$$$$$$ winnerSelection结束 $$$$$$$$$$$$$$$$");

        System.out.println("\n$$$$$$$$$$$$$$$$$ paymentDetermination开始 $$$$$$$$$$$$$$$$");
        winnerList = paymentDetermination(originTaskListStr, originUserListStr, winnerList);
        System.out.println("$$$$$$$$$$$$$$$$$ paymentDetermination结束 $$$$$$$$$$$$$$$$");

        return winnerList;
    }

    /*
     * TSA算法
     */
    private static void timeSenArea(List<User> bidUserList, String bidTaskListStr) {
        /** TSA算法 第1步 */
        for (User bidUser : bidUserList) {
            // 每次恢复初始任务数据
            List<Task> bidTaskList = JsonUtils.fastjsonToObj(bidTaskListStr, new TypeToken<List<Task>>() {
            }.getType());
            /** 目前已经按照sa内任务数量倒序 */
            List<SenArea> senAreList = bidUser.getAllocation();
            // sa区域数量
            int senAreSize = senAreList.size();
            /** TSA算法 第3步 */
            while (bidUser.getRemainSenTime() > 0 && senAreSize > 0) {
                // 找到最小分配时间
                for (int i = 0; i < senAreList.size(); i++) {
                    // System.out.println("\n>>>>>>>>>>TSA更新任务信息列表：");
                    // for (Task task : bidTaskList) {
                    // System.out.println("编号：" + task.getId() + ", id：" + task.getTaskId() + ", 原始时间："
                    // + task.getOriginSenTime() + ", 剩余时间：" + task.getRemainSenTime());
                    // }

                    // 假设用户感知时间最小
                    int minSenTime = bidUser.getRemainSenTime();
                    /** TSA算法 第4步 */
                    SenArea maxSenArea = senAreList.get(i);
                    /** TSA算法 第5-6步，遍历任务找最小时间 */
                    for (Task bidTask : bidTaskList) {
                        // 找到关联任务
                        if (maxSenArea.getTaskIdList().contains(bidTask.getId())) {
                            // TODO 当sa中有任务已被完成时，则minSenTime=0，该sa不会分配时间
                            // TODO 该算法是原文中的算法，其忽略了sa中其它未完成任务，
                            // TODO 需要改进-->>增加每个任务具体分配时间??????
                            if (bidTask.getRemainSenTime() < minSenTime) {
                                minSenTime = bidTask.getRemainSenTime();
                            }
                        }
                    }
                    // 设置该sa的分配感知时间
                    maxSenArea.setSenTime(minSenTime);
                    /** TSA算法 第7步，更新用户剩余感知时间 */
                    bidUser.setRemainSenTime(bidUser.getRemainSenTime() - minSenTime);
                    /** TSA算法 第8步，更新sa数量 */
                    senAreSize--;
                    /** TSA算法 第9步，更新sa关联的任务时间 */
                    for (Task bidTask : bidTaskList) {
                        // 找到关联任务
                        if (maxSenArea.getTaskIdList().contains(bidTask.getId())) {
                            bidTask.setRemainSenTime(bidTask.getRemainSenTime() - minSenTime);
                        }
                    }

                    // System.out.println(
                    // "\n>>>>>>>>>>分配信息列表：" + Arrays.toString(maxSenArea.getTaskIdList().toArray()) + "<<-当前分配");
                    // System.out.println(
                    // "编号：" + bidUser.getId() + ", id：" + bidUser.getUserId() + ", 报价：" + bidUser.getBid()
                    // + ", 感知时间：" + bidUser.getOriginSenTime() + ", 剩余感知时间：" + bidUser.getRemainSenTime()
                    // + ", 关联任务：" + Arrays.toString(bidUser.getTaskIdList().toArray()));
                    // for (SenArea senArea : bidUser.getAllocation()) {
                    // System.out.println("任务sa列表：" + Arrays.toString(senArea.getTaskIdList().toArray()) + ", 分配sa时间："
                    // + senArea.getSenTime());
                    // }
                }
            }
            // System.out.println("############################################################################");
        }
    }

    /**
     * 1-0、Winner Selection
     */
    private static List<User> winnerSelection(String originTaskListStr, String originUserListStr)
            throws CloneNotSupportedException {

        // 竞拍流程使用-用户
        List<User> bidUserList = JsonUtils.fastjsonToObj(originUserListStr, new TypeToken<List<User>>() {
        }.getType());
        // 竞拍流程使用-任务
        List<Task> bidTaskList = JsonUtils.fastjsonToObj(originTaskListStr, new TypeToken<List<Task>>() {
        }.getType());
        // 获胜者总集合，返回值
        List<User> winnerList = new ArrayList<User>();

        // 计算总的任务感知时间
        int taskTimeTotal = 0;
        for (Task task : bidTaskList) {
            taskTimeTotal += task.getRemainSenTime();
        }

        // System.out.println("\n>>>>>>>>>>初始任务信息列表：");
        // for (Task task : bidTaskList) {
        // System.out.println("编号：" + task.getId() + ", id：" + task.getTaskId() + ", 原始时间：" + task.getOriginSenTime()
        // + ", 剩余时间：" + task.getRemainSenTime());
        // }

        // 开始拍卖
        while (taskTimeTotal > 0) {

            // TSA时间分配算法
            String bidTaskListStr = JsonUtils.objToFastjson(bidTaskList);
            timeSenArea(bidUserList, bidTaskListStr);

            // 获得winner,深拷贝
            User winner = (User) getWinner(bidUserList, bidTaskList).clone();

            // System.out.println("\n>>>>>>>>>>获胜者信息列表：");
            // System.out.println("编号：" + winner.getId() + ", id：" + winner.getUserId() + ", 报价：" + winner.getBid()
            // + ", 感知时间：" + winner.getOriginSenTime() + ", 剩余感知时间：" + winner.getRemainSenTime() + ", 感知时间和："
            // + winner.getSenTimeTotal() + ", 关联任务：" + Arrays.toString(winner.getTaskIdList().toArray())
            // + ", 单位成本：" + winner.getAveCost() + ", 收益：" + winner.getPay());
            // for (SenArea senArea : winner.getAllocation()) {
            // System.out.println("任务sa列表：" + Arrays.toString(senArea.getTaskIdList().toArray()) + ", 分配sa时间："
            // + senArea.getSenTime());
            // }
            // 添加到获胜者集合
            winnerList.add(winner);

            // 更新候选者集合数据
            Iterator<User> bidUserListIter = bidUserList.iterator();
            while (bidUserListIter.hasNext()) {
                User deleteUser = bidUserListIter.next();
                // 从候选集合删除获胜者
                if (deleteUser.getId() == winner.getId()) {
                    bidUserListIter.remove();
                }
            }

            // 重置用户时间分配方案
            for (User bidUser : bidUserList) {
                bidUser.setRemainSenTime(bidUser.getOriginSenTime());
                for (SenArea senArea : bidUser.getAllocation()) {
                    senArea.setSenTime(0);
                }
            }

            // 更新任务剩余感知时间
            for (SenArea senArea : winner.getAllocation()) {
                for (Task task : bidTaskList) {
                    if (senArea.getTaskIdList().contains(task.getId())) {
                        task.setRemainSenTime(task.getRemainSenTime() - senArea.getSenTime());
                        taskTimeTotal -= senArea.getSenTime();
                    }

                }
            }

            // System.out.println("\n>>>>>>>>>>更新任务信息列表：");
            // for (Task task : bidTaskList) {
            // System.out.println("编号：" + task.getId() + ", id：" + task.getTaskId() + ", 原始时间："
            // + task.getOriginSenTime() + ", 剩余时间：" + task.getRemainSenTime());
            // }
            // System.out.println();
        }
        return winnerList;
    }

    /*
     * 1-1、获胜者算法
     */
    private static User getWinner(List<User> bidUserList, List<Task> bidTaskList) {

        // System.out.println("\n>>>>>>>>>>用户竞标列表：");
        for (User bidUser : bidUserList) {

            // 计算分配方案的所有时间
            int senTimeTotal = 0;
            List<SenArea> allocation = bidUser.getAllocation();
            for (SenArea senArea : allocation) {
                senTimeTotal += senArea.getSenTime() * senArea.getTaskIdList().size();
            }
            bidUser.setSenTimeTotal(senTimeTotal);

            // 计算单位成本
            String aveCostStr = NumberUtils.division(bidUser.getBid(), bidUser.getSenTimeTotal());
            bidUser.setAveCost(aveCostStr);

            // System.out.println("\n>>>>>>>>>>分配信息列表：");
            // System.out.println("编号：" + bidUser.getId() + ", id：" + bidUser.getUserId() + ", 报价：" + bidUser.getBid()
            // + ", 感知时间：" + bidUser.getOriginSenTime() + ", 剩余感知时间：" + bidUser.getRemainSenTime() + ", 感知时间和："
            // + senTimeTotal + ", 关联任务：" + Arrays.toString(bidUser.getTaskIdList().toArray()) + ", 单位成本："
            // + bidUser.getAveCost() + ", 收益：" + bidUser.getPay());
            // for (SenArea senArea : bidUser.getAllocation()) {
            // System.out.println("任务sa列表：" + Arrays.toString(senArea.getTaskIdList().toArray()) + ", 分配sa时间："
            // + senArea.getSenTime());
            // }
            // System.out.println("############################################################################");
        }

        // 获得winner
        User winner = getMinAveCost(bidUserList);
        return winner;
    }

    /*
     * 1-2、选择最小的竞标成本的获胜
     */
    private static User getMinAveCost(List<User> bidUserList) {
        // 假设第一个人竞拍成本最小
        User winner = bidUserList.get(0);
        for (int i = 1; i < bidUserList.size(); i++) {
            if (Float.valueOf(winner.getAveCost()) > Float.valueOf(bidUserList.get(i).getAveCost())) {
                winner = bidUserList.get(i);
            }
        }
        return winner;
    }

    /**
     * 2-0、Payment Determination
     */
    private static List<User> paymentDetermination(String originTaskListStr, String originUserListStr,
            List<User> winnerList) throws CloneNotSupportedException {

        for (User winner : winnerList) {

            // System.out.println("\n>>>>>>>>>>当前获胜者信息：");
            // System.out.println("编号：" + winner.getId() + ", id：" + winner.getUserId() + ", 报价：" + winner.getBid()
            // + ", 感知时间：" + winner.getOriginSenTime() + ", 剩余感知时间：" + winner.getRemainSenTime() + ", 感知时间和："
            // + winner.getSenTimeTotal() + ", 关联任务：" + Arrays.toString(winner.getTaskIdList().toArray())
            // + ", 单位成本：" + winner.getAveCost() + ", 收益：" + winner.getPay());
            // for (SenArea senArea : winner.getAllocation()) {
            // System.out.println("任务sa列表：" + Arrays.toString(senArea.getTaskIdList().toArray()) + ", 分配sa时间："
            // + senArea.getSenTime());
            // }
            // System.out.println("############################################################################");

            // 只删除当前winner，下次迭代时恢复上次被删除的winner
            List<User> deleteUserList = JsonUtils.fastjsonToObj(originUserListStr, new TypeToken<List<User>>() {
            }.getType());
            // 删除获胜者
            Iterator<User> deleteListItor = deleteUserList.iterator();
            while (deleteListItor.hasNext()) {
                User deleteUser = deleteListItor.next();
                if (deleteUser.getId() == winner.getId()) {
                    deleteListItor.remove();
                }
            }
            // 深拷贝用户
            String payUserListStr = JsonUtils.objToFastjson(deleteUserList);
            // 开始
            List<User> nextWinnerList = winnerSelection(originTaskListStr, payUserListStr);
            // 确定支付价格
            for (User nextWinner : nextWinnerList) {
                // System.out.println("\n>>>>>>编号：" + nextWinner.getId() + ", 次级获胜者ID：" + nextWinner.getUserId()
                // + ", 次级获胜者时间和：" + nextWinner.getSenTimeTotal() + ", 次级获胜者报价：" + nextWinner.getBid());
                winner.setPay(getPay(winner, nextWinner));
                // System.out.println(">>>>>>编号：" + winner.getId() + ", 获胜者ID：" + winner.getUserId() + ", 获胜者时间和："
                // + winner.getSenTimeTotal() + ", 获胜者报价：" + winner.getBid() + ", 当前支付价格:" + winner.getPay());
            }
            // System.out.println();
        }
        return winnerList;
    }

    /*
     * 2-1、支付函数
     */
    private static String getPay(User winner, User nextWinner) {
        String winnerPay = winner.getPay();
        // 保留2位小数
        String nextPay = getNextPay(winner.getSenTimeTotal(), nextWinner.getSenTimeTotal(), nextWinner.getBid());
        winnerPay = NumberUtils.getStrMax(winnerPay, nextPay);
        return winnerPay;
    }

    /*
     * 2-2、计算支付价格
     */
    private static String getNextPay(int winnerSenTime, int nextSenTime, int nextBid) {
        float result = (float) winnerSenTime / nextSenTime * nextBid;
        String nextPay = String.format("%.2f", result);
        return nextPay;
    }

}
