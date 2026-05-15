package kr.ac.kopo.todayroutine;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import kr.ac.kopo.todayroutine.data.DailyStats;
import kr.ac.kopo.todayroutine.data.DataManager;
import kr.ac.kopo.todayroutine.data.MonthlyStats;
import kr.ac.kopo.todayroutine.data.Routine;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    // Bottom Sheet Statistics Views
    private TextView tvCurrentMonth, btnPrevMonth, btnNextMonth;
    private CalendarView calendarView;
    private ProgressBar circularProgress;
    private TextView tvStatsTitle, tvOverallRate, tvBestCategory;
    private LinearLayout layoutCategoryRanking;

    private int currentYear;
    private int currentMonth; // 1-based
    private int currentDay;
    private boolean isMonthlyView = false; // Start with Daily View by default
    private boolean isProgrammaticChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize data (loads from SharedPreferences or creates dummy data on first
        // launch)
        DataManager.getInstance().init(this);

        // ViewPager setup
        viewPager = findViewById(R.id.viewPager);
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Bottom Sheet Statistics setup
        setupBottomSheetStats();
    }

    private void setupBottomSheetStats() {
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarView = findViewById(R.id.calendarView);
        circularProgress = findViewById(R.id.circularProgress);
        tvStatsTitle = findViewById(R.id.tvStatsTitle);
        tvOverallRate = findViewById(R.id.tvOverallRate);
        tvBestCategory = findViewById(R.id.tvBestCategory);
        layoutCategoryRanking = findViewById(R.id.layoutCategoryRanking);

        // Initialize to current month
        Calendar now = Calendar.getInstance();
        currentYear = now.get(Calendar.YEAR);
        currentMonth = now.get(Calendar.MONTH) + 1;
        currentDay = now.get(Calendar.DAY_OF_MONTH);

        // Month navigation
        btnPrevMonth.setOnClickListener(v -> {
            isProgrammaticChange = true;
            currentMonth--;
            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }
            isMonthlyView = true;
            updateCalendarViewMonth();
            updateStats();
            isProgrammaticChange = false;
        });

        btnNextMonth.setOnClickListener(v -> {
            isProgrammaticChange = true;
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
            isMonthlyView = true;
            updateCalendarViewMonth();
            updateStats();
            isProgrammaticChange = false;
        });

        // Update stats when date is changed in the calendar
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            if (!isProgrammaticChange) {
                currentYear = year;
                currentMonth = month + 1;
                currentDay = dayOfMonth;
                isMonthlyView = false;
                updateStats();
            }
        });

        updateStats();
    }

    private void updateCalendarViewMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, 1);
        calendarView.setDate(cal.getTimeInMillis(), true, true);
    }

    public void refreshStatistics() {
        updateStats();
    }

    private void updateStats() {
        if (isMonthlyView) {
            updateMonthlyStats();
        } else {
            updateDailyStats();
        }
    }

    private void updateMonthlyStats() {
        tvCurrentMonth.setText(currentMonth + "월");
        tvStatsTitle.setText("이 달의 평균 달성률");
        tvBestCategory.setVisibility(android.view.View.VISIBLE);

        MonthlyStats stats = DataManager.getInstance().getMonthlyStats(currentYear, currentMonth);

        // Overall display
        int rate = stats.getOverallRate();
        circularProgress.setProgress(rate);
        tvOverallRate.setText("완료"); // Remove % from circle

        // Best category
        int bestCatId = stats.getBestCategoryId();
        if (bestCatId != -1) {
            String catName = DataManager.getInstance().getCategoryName(bestCatId);
            tvBestCategory.setText("가장 열심히 한 카테고리: " + catName);
        } else {
            tvBestCategory.setText("데이터가 없습니다");
        }

        // Category ranking
        updateCategoryRanking(stats);
    }

    private void updateDailyStats() {
        tvCurrentMonth.setText(currentMonth + "월");
        tvStatsTitle.setText(currentMonth + "월 " + currentDay + "일 달성률");
        tvBestCategory.setVisibility(android.view.View.GONE);

        DailyStats stats = DataManager.getInstance().getDailyStats(currentYear, currentMonth, currentDay);

        // Circular progress
        int rate = stats.getOverallRate();
        circularProgress.setProgress(rate);
        tvOverallRate.setText(rate + "%");

        // Daily Routine ranking
        updateDailyRoutineRanking(stats);
    }

    private void updateCategoryRanking(MonthlyStats stats) {
        layoutCategoryRanking.removeAllViews();

        Map<Integer, Integer> catRates = stats.getCategoryRates();
        if (catRates.isEmpty())
            return;

        // Sort categories by rate descending
        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(catRates.entrySet());
        Collections.sort(sortedEntries, (a, b) -> b.getValue() - a.getValue());

        // Display in 2 columns
        LinearLayout currentRow = null;
        for (int i = 0; i < sortedEntries.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                layoutCategoryRanking.addView(currentRow);
            }

            Map.Entry<Integer, Integer> entry = sortedEntries.get(i);
            int catId = entry.getKey();
            int catRate = entry.getValue();
            String catName = DataManager.getInstance().getCategoryName(catId);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            itemParams.setMargins(0, 12, 12, 12);
            itemLayout.setLayoutParams(itemParams);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setText(catName);
            tvName.setTextSize(15);
            tvName.setTextColor(Color.parseColor("#2C3E50"));
            LinearLayout.LayoutParams tvNameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvNameParams.rightMargin = 20;
            tvName.setLayoutParams(tvNameParams);

            TextView tvRate = new TextView(this);
            tvRate.setText("진행중"); // Instead of 85%
            tvRate.setTextSize(14);
            tvRate.setTextColor(Color.parseColor("#98D8AA"));
            tvRate.setTypeface(null, Typeface.BOLD);

            itemLayout.addView(tvName);
            itemLayout.addView(tvRate);
            currentRow.addView(itemLayout);
        }

        // Add spacer if odd number of items
        if (sortedEntries.size() % 2 != 0 && currentRow != null) {
            android.view.View spacer = new android.view.View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
            currentRow.addView(spacer);
        }
    }

    private void updateDailyRoutineRanking(DailyStats stats) {
        layoutCategoryRanking.removeAllViews();

        Map<Integer, Integer> routineRates = stats.getRoutineRates();
        if (routineRates.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("이 날 진행할 루틴이 없습니다.");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTextColor(Color.GRAY);
            layoutCategoryRanking.addView(emptyView);
            return;
        }

        // Display in 2 columns
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(routineRates.entrySet());
        LinearLayout currentRow = null;
        for (int i = 0; i < entries.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                layoutCategoryRanking.addView(currentRow);
            }

            Map.Entry<Integer, Integer> entry = entries.get(i);
            int routineId = entry.getKey();
            int routineRate = entry.getValue();
            String routineName = getRoutineName(routineId);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            itemParams.setMargins(0, 12, 12, 12);
            itemLayout.setLayoutParams(itemParams);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setText(routineName);
            tvName.setTextSize(14);
            tvName.setTextColor(Color.parseColor("#2C3E50"));
            LinearLayout.LayoutParams tvNameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvNameParams.rightMargin = 20;
            tvName.setLayoutParams(tvNameParams);

            TextView tvStatus = new TextView(this);
            if (routineRate >= 100) {
                tvStatus.setText("성공!");
                tvStatus.setTextColor(Color.parseColor("#98D8AA")); // Our point green
                tvStatus.setTypeface(null, Typeface.BOLD);
            } else {
                tvStatus.setText(""); // Show nothing if not completed
            }
            tvStatus.setTextSize(13);

            itemLayout.addView(tvName);
            itemLayout.addView(tvStatus);
            currentRow.addView(itemLayout);
        }

        // Add spacer if odd number of items
        if (entries.size() % 2 != 0 && currentRow != null) {
            android.view.View spacer = new android.view.View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
            currentRow.addView(spacer);
        }
    }

    private String getRoutineName(int routineId) {
        for (Routine r : DataManager.getInstance().getRoutineList()) {
            if (r.getId() == routineId) {
                return r.getName();
            }
        }
        return "루틴";
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {
        public MainPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new HomeFragment();
            } else {
                return new CategoryFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Home, Category
        }
    }
}