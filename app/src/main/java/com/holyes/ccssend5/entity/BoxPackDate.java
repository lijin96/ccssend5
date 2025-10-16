package com.holyes.ccssend5.entity;

/**
 * @ClassName: BoxPackDate
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2025/8/6 16:33
 */
public class BoxPackDate {
    private String StartDate;//开始日期
    private String EndDate;//结束时间

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }

    public BoxPackDate(String startDate, String endDate) {
        StartDate = startDate;
        EndDate = endDate;
    }

    public BoxPackDate() {
    }
}
