package kr.ac.kopo.todayroutine.data;

import java.util.List;

public class Routine {
    private int id;
    private String name;
    private String detail;
    private int categoryId;
    private boolean isCompleted;
    private List<Integer> daysOfWeek; // 1: Sun, 2: Mon, ..., 7: Sat
    private int consecutiveDays; // 연속 성공 일수
    private String memo; // 오늘의 메모
    private int totalAttempts; // 전체 시도 횟수 (for achievement rate)

    public Routine(int id, String name, String detail, int categoryId, List<Integer> daysOfWeek) {
        this.id = id;
        this.name = name;
        this.detail = detail;
        this.categoryId = categoryId;
        this.daysOfWeek = daysOfWeek;
        this.isCompleted = false;
        this.consecutiveDays = 0;
        this.memo = "";
        this.totalAttempts = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<Integer> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public int getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(int consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public int getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }

    public int getAchievementRate() {
        if (totalAttempts == 0) return 0;
        // Mock logic: consecutiveDays is completions, totalAttempts is total days active.
        // For realistic dummy data, let's just make up a percentage based on them.
        int rate = (int) (((float) consecutiveDays / totalAttempts) * 100);
        return Math.min(100, Math.max(0, rate));
    }
}
