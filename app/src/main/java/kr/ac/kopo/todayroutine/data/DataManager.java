package kr.ac.kopo.todayroutine.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataManager {
    private static DataManager instance;

    private List<Routine> routineList;
    private List<Category> categoryList;
    private String todayMemo = "";

    private static final String PREF_NAME = "RoutinePrefs";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_ROUTINES = "routines_json";
    private static final String KEY_CATEGORIES = "categories_json";
    private static final String KEY_DATA_INITIALIZED = "data_initialized";

    private static final Gson gson = new Gson();

    private DataManager() {
        routineList = new ArrayList<>();
        categoryList = new ArrayList<>();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * Must be called once at app startup (e.g., in MainActivity.onCreate).
     * Loads saved data from SharedPreferences, or initializes dummy data on first launch.
     */
    public void init(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean initialized = prefs.getBoolean(KEY_DATA_INITIALIZED, false);

        if (initialized) {
            // Load from SharedPreferences
            loadFromPrefs(context);
        } else {
            // First launch -> create dummy data and save
            initDummyData();
            saveToPrefs(context);
            prefs.edit().putBoolean(KEY_DATA_INITIALIZED, true).apply();
        }
    }

    // ===== Persistence (SharedPreferences + Gson) =====

    public void saveToPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ROUTINES, gson.toJson(routineList));
        editor.putString(KEY_CATEGORIES, gson.toJson(categoryList));
        editor.apply();
    }

    private void loadFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String routinesJson = prefs.getString(KEY_ROUTINES, null);
        String categoriesJson = prefs.getString(KEY_CATEGORIES, null);

        if (routinesJson != null) {
            Type routineListType = new TypeToken<ArrayList<Routine>>(){}.getType();
            routineList = gson.fromJson(routinesJson, routineListType);
        }

        if (categoriesJson != null) {
            Type categoryListType = new TypeToken<ArrayList<Category>>(){}.getType();
            categoryList = gson.fromJson(categoriesJson, categoryListType);
        }
    }

    // ===== Username =====

    public String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "학생");
    }

    public void setUserName(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    // ===== Dummy Data (First Launch Only) =====

    private void initDummyData() {
        categoryList.add(new Category(1, "건강"));
        categoryList.add(new Category(2, "공부"));
        categoryList.add(new Category(3, "자기계발"));
        categoryList.add(new Category(4, "취미"));
        categoryList.add(new Category(5, "생활습관"));

        routineList.add(new Routine(1, "달리기 30분", "동네 한바퀴 달리기", 1, Arrays.asList(2, 4, 6)));
        routineList.add(new Routine(2, "영단어 50개 암기", "토익 영단어장", 2, Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
        routineList.add(new Routine(3, "물 2리터 마시기", "텀블러 사용하기", 1, Arrays.asList(1, 2, 3, 4, 5, 6)));
        routineList.add(new Routine(4, "독서 30분", "소설책 읽기", 3, Arrays.asList(1, 7)));
        routineList.add(new Routine(5, "스쿼트 100개", "하체 운동", 1, Arrays.asList(1, 2, 3, 4, 5)));
        routineList.add(new Routine(6, "코딩 문제 풀기", "백준/프로그래머스", 2, Arrays.asList(1, 2, 3, 4, 7)));
        routineList.add(new Routine(7, "명상 10분", "마음 챙김", 3, Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
        routineList.add(new Routine(8, "일기 쓰기", "하루 정리", 3, Arrays.asList(1, 2, 5, 6, 7)));
        routineList.add(new Routine(9, "비타민 먹기", "종합 비타민", 5, Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
        routineList.add(new Routine(10, "악기 연습", "기타/피아노", 4, Arrays.asList(1, 6, 7)));
        routineList.add(new Routine(11, "스트레칭", "전신 스트레칭", 1, Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
        routineList.add(new Routine(12, "뉴스레터 읽기", "경제/IT 소식", 3, Arrays.asList(2, 3, 4, 5, 6)));
        routineList.add(new Routine(13, "방 정리 10분", "미니멀리즘", 5, Arrays.asList(1, 7)));

        // Some sample state for "Current Month"
        routineList.get(0).setConsecutiveDays(4);
        routineList.get(0).setTotalAttempts(5);
        routineList.get(1).setConsecutiveDays(10);
        routineList.get(1).setTotalAttempts(12);
        routineList.get(2).setConsecutiveDays(5);
        routineList.get(2).setTotalAttempts(5);
    }

    // ===== Getters =====

    public List<Routine> getRoutineList() {
        return routineList;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public String getTodayMemo() {
        return todayMemo;
    }

    public void setTodayMemo(String todayMemo) {
        this.todayMemo = todayMemo;
    }

    public List<Routine> getTodayRoutines() {
        List<Routine> todayList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        for (Routine r : routineList) {
            if (r.getDaysOfWeek().contains(todayDayOfWeek)) {
                todayList.add(r);
            }
        }
        return todayList;
    }

    public void checkMidnightAndReset(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String lastDate = prefs.getString(KEY_LAST_DATE, "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        if (!todayDate.equals(lastDate)) {
            for (Routine r : routineList) {
                if (!r.isCompleted()) {
                    r.setConsecutiveDays(0);
                }
                r.setCompleted(false);
            }
            todayMemo = "";
            prefs.edit().putString(KEY_LAST_DATE, todayDate).apply();
            saveToPrefs(context); // Save reset state
        }
    }

    // ===== CRUD Helpers (all auto-save) =====

    public int getNextRoutineId() {
        int maxId = 0;
        for (Routine r : routineList) {
            if (r.getId() > maxId) maxId = r.getId();
        }
        return maxId + 1;
    }

    public int getNextCategoryId() {
        int maxId = 0;
        for (Category c : categoryList) {
            if (c.getId() > maxId) maxId = c.getId();
        }
        return maxId + 1;
    }

    public void addRoutine(Routine routine) {
        routineList.add(routine);
    }

    public void deleteRoutine(int routineId) {
        for (int i = 0; i < routineList.size(); i++) {
            if (routineList.get(i).getId() == routineId) {
                routineList.remove(i);
                break;
            }
        }
    }

    public void addCategory(Category category) {
        categoryList.add(category);
    }

    public void deleteCategoryWithRoutines(int categoryId) {
        List<Routine> toRemove = new ArrayList<>();
        for (Routine r : routineList) {
            if (r.getCategoryId() == categoryId) {
                toRemove.add(r);
            }
        }
        routineList.removeAll(toRemove);

        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId() == categoryId) {
                categoryList.remove(i);
                break;
            }
        }
    }

    public List<Routine> getRoutinesByCategory(int categoryId) {
        List<Routine> result = new ArrayList<>();
        for (Routine r : routineList) {
            if (r.getCategoryId() == categoryId) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Generate monthly statistics for a given year-month.
     * Uses seeded random values based on routine ID + month to produce
     * consistent, plausible dummy data for demo purposes.
     */
    public MonthlyStats getMonthlyStats(int year, int month) {
        MonthlyStats stats = new MonthlyStats(year, month);

        if (routineList.isEmpty() || categoryList.isEmpty()) {
            return stats;
        }

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;

        // 미래 날짜인 경우 더미 데이터를 생성하지 않고 빈 통계 반환
        if (year > currentYear || (year == currentYear && month > currentMonth)) {
            return stats;
        }

        // Generate per-routine rates using a simple seed formula
        int totalRate = 0;
        int routineCount = 0;

        // Track per-category totals
        java.util.Map<Integer, Integer> catSumMap = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> catCountMap = new java.util.HashMap<>();

        for (Routine r : routineList) {
            // Simplified: All dummy routines are 100% successful for monthly demo
            int rate = 100;

            stats.getRoutineRates().put(r.getId(), rate);
            totalRate += rate;
            routineCount++;

            int catId = r.getCategoryId();
            catSumMap.put(catId, catSumMap.getOrDefault(catId, 0) + rate);
            catCountMap.put(catId, catCountMap.getOrDefault(catId, 0) + 1);
        }

        // Overall average set to 100 to fill the gauge
        if (routineCount > 0) {
            stats.setOverallRate(100);
        }

        // Category averages and find best
        int bestRate = -1;
        int bestCatId = -1;
        for (Category cat : categoryList) {
            int catId = cat.getId();
            if (catCountMap.containsKey(catId)) {
                int avg = catSumMap.get(catId) / catCountMap.get(catId);
                stats.getCategoryRates().put(catId, avg);
                if (avg > bestRate) {
                    bestRate = avg;
                    bestCatId = catId;
                }
            }
        }
        stats.setBestCategoryId(bestCatId);

        return stats;
    }

    /**
     * Get category name by ID.
     */
    public String getCategoryName(int categoryId) {
        for (Category c : categoryList) {
            if (c.getId() == categoryId) {
                return c.getName();
            }
        }
        return "알 수 없음";
    }

    /**
     * Generate daily statistics for a given year-month-day.
     * Only includes routines scheduled for that day of the week.
     */
    public DailyStats getDailyStats(int year, int month, int day) {
        DailyStats stats = new DailyStats(year, month, day);

        if (routineList.isEmpty()) {
            return stats;
        }

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        // 미래 날짜인 경우 더미 데이터를 생성하지 않고 빈 통계 반환
        if (year > currentYear || (year == currentYear && month > currentMonth) || 
            (year == currentYear && month == currentMonth && day > currentDay)) {
            return stats;
        }

        // Determine day of week (1=Sun, 2=Mon, ..., 7=Sat)
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        int totalRate = 0;
        int routineCount = 0;

        for (Routine r : routineList) {
            // Check if routine is scheduled for this day
            if (r.getDaysOfWeek() != null && r.getDaysOfWeek().contains(dayOfWeek)) {
                // Seed based on routine ID + date
                int seed = (r.getId() * 7 + year * 11 + month * 13 + day * 17) % 100;

                // Binary success/fail for daily view (0 or 100)
                int rate = (seed > 40) ? 100 : 0; 

                // Use actual data for "today"
                if (year == currentYear && month == currentMonth && day == currentDay) {
                    rate = r.isCompleted() ? 100 : 0;
                }

                stats.getRoutineRates().put(r.getId(), rate);
                totalRate += rate;
                routineCount++;
            }
        }

        if (routineCount > 0) {
            stats.setOverallRate(totalRate / routineCount);
        }

        return stats;
    }
}

