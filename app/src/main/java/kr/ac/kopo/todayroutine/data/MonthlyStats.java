package kr.ac.kopo.todayroutine.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds monthly statistics data for a specific year-month.
 * Maps categoryId -> achievement rate (0~100).
 * Also stores per-routine achievement rates.
 */
public class MonthlyStats {
    private int year;
    private int month; // 1-based (1=January)
    private int overallRate; // 0~100
    private Map<Integer, Integer> categoryRates; // categoryId -> rate
    private Map<Integer, Integer> routineRates; // routineId -> rate
    private int bestCategoryId;

    public MonthlyStats(int year, int month) {
        this.year = year;
        this.month = month;
        this.overallRate = 0;
        this.categoryRates = new HashMap<>();
        this.routineRates = new HashMap<>();
        this.bestCategoryId = -1;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getOverallRate() { return overallRate; }
    public void setOverallRate(int overallRate) { this.overallRate = overallRate; }
    public Map<Integer, Integer> getCategoryRates() { return categoryRates; }
    public Map<Integer, Integer> getRoutineRates() { return routineRates; }
    public int getBestCategoryId() { return bestCategoryId; }
    public void setBestCategoryId(int bestCategoryId) { this.bestCategoryId = bestCategoryId; }
}
