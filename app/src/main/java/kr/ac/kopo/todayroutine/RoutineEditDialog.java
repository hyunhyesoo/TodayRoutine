package kr.ac.kopo.todayroutine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.ac.kopo.todayroutine.data.Category;
import kr.ac.kopo.todayroutine.data.DataManager;
import kr.ac.kopo.todayroutine.data.Routine;

public class RoutineEditDialog extends DialogFragment {

    private Routine existingRoutine; // null = Add Mode, non-null = Edit Mode
    private OnDataChangeListener listener;

    private TextView tvDialogTitle;
    private EditText etRoutineName, etRoutineDetail;
    private RadioGroup rgCategories;
    private TextView tvAddNewCategory;
    private TextView btnCancel, btnDelete, btnSave;
    private TextView[] dayViews;
    private boolean[] daySelected = new boolean[7]; // index 0=Mon(2), 1=Tue(3)... 6=Sun(1)

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public static RoutineEditDialog newInstance(Routine routine) {
        RoutineEditDialog dialog = new RoutineEditDialog();
        dialog.existingRoutine = routine;
        return dialog;
    }

    public void setOnDataChangeListener(OnDataChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View view = inflater.inflate(R.layout.dialog_edit_routine, container, false);

        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        etRoutineName = view.findViewById(R.id.etRoutineName);
        etRoutineDetail = view.findViewById(R.id.etRoutineDetail);
        rgCategories = view.findViewById(R.id.rgCategories);
        tvAddNewCategory = view.findViewById(R.id.tvAddNewCategory);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

        // Setup Day Views
        LinearLayout layoutDays = view.findViewById(R.id.layoutDaysSelect);
        dayViews = new TextView[] {
                view.findViewById(R.id.daySelectMon),
                view.findViewById(R.id.daySelectTue),
                view.findViewById(R.id.daySelectWed),
                view.findViewById(R.id.daySelectThu),
                view.findViewById(R.id.daySelectFri),
                view.findViewById(R.id.daySelectSat),
                view.findViewById(R.id.daySelectSun)
        };

        setupDayToggle();
        setupCategoryRadioButtons();
        setupMode();
        setupButtons();

        return view;
    }

    private void setupMode() {
        if (existingRoutine != null) {
            // Edit Mode
            tvDialogTitle.setText("루틴 수정");
            etRoutineName.setText(existingRoutine.getName());
            etRoutineDetail.setText(existingRoutine.getDetail());
            btnDelete.setVisibility(View.VISIBLE);

            // Set days
            // Our dayViews index: 0=Mon(2), 1=Tue(3), 2=Wed(4), 3=Thu(5), 4=Fri(6),
            // 5=Sat(7), 6=Sun(1)
            int[] dayMapping = { 2, 3, 4, 5, 6, 7, 1 };
            for (int i = 0; i < 7; i++) {
                if (existingRoutine.getDaysOfWeek().contains(dayMapping[i])) {
                    daySelected[i] = true;
                    dayViews[i].setBackgroundResource(R.drawable.bg_circle_day_active);
                    dayViews[i].setTextColor(Color.WHITE);
                }
            }

            // Pre-select category radio button
            for (int i = 0; i < rgCategories.getChildCount(); i++) {
                View child = rgCategories.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) child;
                    for (int j = 0; j < row.getChildCount(); j++) {
                        View rowChild = row.getChildAt(j);
                        if (rowChild instanceof RadioButton) {
                            RadioButton rb = (RadioButton) rowChild;
                            if (rb.getId() == existingRoutine.getCategoryId()) {
                                rb.setChecked(true);
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            // Add Mode
            tvDialogTitle.setText("새 루틴 추가");
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void setupDayToggle() {
        int[] dayMapping = { 2, 3, 4, 5, 6, 7, 1 }; // Mon-Sun
        for (int i = 0; i < dayViews.length; i++) {
            final int idx = i;
            dayViews[i].setOnClickListener(v -> {
                daySelected[idx] = !daySelected[idx];
                if (daySelected[idx]) {
                    dayViews[idx].setBackgroundResource(R.drawable.bg_circle_day_active);
                    dayViews[idx].setTextColor(Color.WHITE);
                } else {
                    dayViews[idx].setBackgroundResource(R.drawable.bg_circle_day_inactive);
                    dayViews[idx].setTextColor(Color.parseColor("#7F8C8D"));
                }
            });
        }
    }

    private void setupCategoryRadioButtons() {
        List<Category> categories = DataManager.getInstance().getCategoryList();
        rgCategories.removeAllViews();

        // Build a 2-column-like layout using LinearLayouts inside the RadioGroup
        LinearLayout currentRow = null;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);

            if (i % 2 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT));
                rgCategories.addView(currentRow);
            }

            RadioButton rb = new RadioButton(getContext());
            rb.setId(cat.getId());
            rb.setText(cat.getName());
            rb.setTextSize(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1);
            params.setMargins(0, 8, 0, 8);
            rb.setLayoutParams(params);

            currentRow.addView(rb);
        }

        // Since RadioButtons are inside nested LinearLayouts within the RadioGroup,
        // RadioGroup's auto-mutual-exclusion won't work. We handle it manually.
        setupManualRadioGroupBehavior();

        // Add new category button
        tvAddNewCategory.setOnClickListener(v -> showAddCategoryInline());
    }

    private void setupManualRadioGroupBehavior() {
        // We need to walk through all nested RadioButtons and set click listeners
        for (int i = 0; i < rgCategories.getChildCount(); i++) {
            View child = rgCategories.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View rowChild = row.getChildAt(j);
                    if (rowChild instanceof RadioButton) {
                        RadioButton rb = (RadioButton) rowChild;
                        rb.setOnClickListener(v -> {
                            // Uncheck all others
                            uncheckAllRadioButtons();
                            rb.setChecked(true);
                        });
                    }
                }
            }
        }
    }

    private void uncheckAllRadioButtons() {
        for (int i = 0; i < rgCategories.getChildCount(); i++) {
            View child = rgCategories.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View rowChild = row.getChildAt(j);
                    if (rowChild instanceof RadioButton) {
                        ((RadioButton) rowChild).setChecked(false);
                    }
                }
            }
        }
    }

    private int getSelectedCategoryId() {
        for (int i = 0; i < rgCategories.getChildCount(); i++) {
            View child = rgCategories.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View rowChild = row.getChildAt(j);
                    if (rowChild instanceof RadioButton && ((RadioButton) rowChild).isChecked()) {
                        return rowChild.getId();
                    }
                }
            }
        }
        return -1;
    }

    private void showAddCategoryInline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("새 카테고리 추가");

        final EditText input = new EditText(getContext());
        input.setHint("카테고리 이름");
        builder.setView(input);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                Category newCat = new Category(DataManager.getInstance().getNextCategoryId(), name);
                DataManager.getInstance().addCategory(newCat);
                DataManager.getInstance().saveToPrefs(requireContext());
                setupCategoryRadioButtons(); // Refresh radio buttons
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnDelete.setOnClickListener(v -> {
            if (existingRoutine != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle("루틴 삭제")
                        .setMessage("'" + existingRoutine.getName() + "' 루틴을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            DataManager.getInstance().deleteRoutine(existingRoutine.getId());
                            DataManager.getInstance().saveToPrefs(requireContext());
                            if (listener != null)
                                listener.onDataChanged();
                            dismiss();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = etRoutineName.getText().toString().trim();
            String detail = etRoutineDetail.getText().toString().trim();
            int categoryId = getSelectedCategoryId();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "루틴 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (categoryId == -1) {
                Toast.makeText(getContext(), "카테고리를 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build days list
            int[] dayMapping = { 2, 3, 4, 5, 6, 7, 1 }; // Mon=2, Tue=3, ..., Sun=1
            List<Integer> selectedDays = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (daySelected[i]) {
                    selectedDays.add(dayMapping[i]);
                }
            }

            if (selectedDays.isEmpty()) {
                Toast.makeText(getContext(), "반복 요일을 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingRoutine != null) {
                // Edit mode: update existing
                existingRoutine.setName(name);
                existingRoutine.setDetail(detail);
                existingRoutine.setCategoryId(categoryId);
                existingRoutine.setDaysOfWeek(selectedDays);
            } else {
                // Add mode: create new
                int newId = DataManager.getInstance().getNextRoutineId();
                Routine newRoutine = new Routine(newId, name, detail, categoryId, selectedDays);
                DataManager.getInstance().addRoutine(newRoutine);
            }

            DataManager.getInstance().saveToPrefs(requireContext());
            if (listener != null)
                listener.onDataChanged();
            dismiss();
        });
    }
}
