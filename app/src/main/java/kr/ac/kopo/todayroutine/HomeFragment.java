package kr.ac.kopo.todayroutine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kopo.todayroutine.data.DataManager;
import kr.ac.kopo.todayroutine.data.Routine;

public class HomeFragment extends Fragment implements RoutineAdapter.OnItemClickListener {

    private RecyclerView rvRoutines;
    private RoutineAdapter adapter;
    private ProgressBar progressDaily;
    private TextView tvProgressPercent;
    private TextView tvGreeting;
    private TextView tvEditName;
    private List<Routine> todayRoutines = new ArrayList<>();
    private boolean hasShownCelebration = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvRoutines = view.findViewById(R.id.rvRoutines);
        progressDaily = view.findViewById(R.id.progressDaily);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvEditName = view.findViewById(R.id.tvEditName);

        // Check for midnight reset
        DataManager.getInstance().checkMidnightAndReset(requireContext());

        setupRecyclerView();
        loadRoutines();
        updateGreeting();
        updateProgress();

        tvEditName.setOnClickListener(v -> showNameEditDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data every time the fragment becomes visible
        // This ensures routines added/edited in CategoryFragment are reflected here
        loadRoutines();
        updateProgress();
    }

    private void updateGreeting() {
        String name = DataManager.getInstance().getUserName(requireContext());
        tvGreeting.setText(name + "님, 오늘도 화이팅!");
    }

    private void showNameEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("수정");

        final EditText input = new EditText(getContext());
        input.setText(DataManager.getInstance().getUserName(requireContext()));
        builder.setView(input);

        builder.setPositiveButton("저장", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                DataManager.getInstance().setUserName(requireContext(), newName);
                updateGreeting();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loadRoutines() {
        // Get today's routines and sort: incomplete first, completed at the bottom
        List<Routine> allToday = DataManager.getInstance().getTodayRoutines();
        List<Routine> incomplete = new ArrayList<>();
        List<Routine> completed = new ArrayList<>();

        for (Routine r : allToday) {
            if (r.isCompleted()) {
                completed.add(r);
            } else {
                incomplete.add(r);
            }
        }

        todayRoutines.clear();
        todayRoutines.addAll(incomplete);
        todayRoutines.addAll(completed);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupRecyclerView() {
        rvRoutines.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoutineAdapter(todayRoutines, this);
        rvRoutines.setAdapter(adapter);
    }

    private void updateProgress() {
        if (todayRoutines.isEmpty()) {
            progressDaily.setProgress(0);
            tvProgressPercent.setText("0%");
            return;
        }

        int completedCount = 0;
        for (Routine r : todayRoutines) {
            if (r.isCompleted())
                completedCount++;
        }

        int percent = (int) (((float) completedCount / todayRoutines.size()) * 100);
        progressDaily.setProgress(percent);
        tvProgressPercent.setText(percent + "%");

        if (percent == 100 && !hasShownCelebration) {
            hasShownCelebration = true;
            showCelebrationDialog();
        } else if (percent < 100) {
            hasShownCelebration = false;
        }
    }

    private void showCelebrationDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_celebration);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        // Auto dismiss after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 2000);
    }

    @Override
    public void onCheckboxClicked(int position, boolean isChecked) {
        Routine routine = todayRoutines.get(position);
        routine.setCompleted(isChecked);

        if (isChecked) {
            routine.setConsecutiveDays(routine.getConsecutiveDays() + 1);
        } else {
            // Prevent going below 0
            int currentDays = routine.getConsecutiveDays();
            routine.setConsecutiveDays(Math.max(0, currentDays - 1));
        }

        // Re-sort the list and notify
        loadRoutines();
        updateProgress();
        DataManager.getInstance().saveToPrefs(requireContext());

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshStatistics();
        }
    }

    @Override
    public void onEmptyAreaClicked(Routine routine) {
        showMemoDialog(routine);
    }

    private void showMemoDialog(Routine routine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("오늘의 특이사항");

        final EditText input = new EditText(getContext());
        input.setText(routine.getMemo());
        builder.setView(input);

        builder.setPositiveButton("저장", (dialog, which) -> {
            routine.setMemo(input.getText().toString());
            adapter.notifyDataSetChanged();
            DataManager.getInstance().saveToPrefs(requireContext());
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
