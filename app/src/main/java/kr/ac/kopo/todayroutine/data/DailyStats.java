package kr.ac.kopo.todayroutine.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds daily statistics data for a specific date.
 */
public class DailyStats {
    private int year;
    private int month; // 1-based
    private int day;
    private int overallRate; // 0~100
    private Map<Integer, Integer> routineRates; // routineId -> rate

    public DailyStats(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.overallRate = 0;
        this.routineRates = new HashMap<>();
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getOverallRate() { return overallRate; }
    public void setOverallRate(int overallRate) { this.overallRate = overallRate; }
    public Map<Integer, Integer> getRoutineRates() { return routineRates; }
}
