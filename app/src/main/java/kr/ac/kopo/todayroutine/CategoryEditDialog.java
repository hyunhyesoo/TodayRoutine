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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import kr.ac.kopo.todayroutine.data.Category;
import kr.ac.kopo.todayroutine.data.DataManager;

public class CategoryEditDialog extends DialogFragment {

    private Category existingCategory; // null = Add Mode, non-null = Edit Mode
    private OnDataChangeListener listener;

    private TextView tvDialogTitle;
    private EditText etCategoryName;
    private TextView btnCancel, btnDelete, btnSave;

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public static CategoryEditDialog newInstance(Category category) {
        CategoryEditDialog dialog = new CategoryEditDialog();
        dialog.existingCategory = category;
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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View view = inflater.inflate(R.layout.dialog_edit_category, container, false);

        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        etCategoryName = view.findViewById(R.id.etCategoryName);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnSave = view.findViewById(R.id.btnSave);

        setupMode();
        setupButtons();

        return view;
    }

    private void setupMode() {
        if (existingCategory != null) {
            // Edit Mode
            tvDialogTitle.setText("카테고리 수정");
            etCategoryName.setText(existingCategory.getName());
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // Add Mode
            tvDialogTitle.setText("새 카테고리 추가");
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnDelete.setOnClickListener(v -> {
            if (existingCategory != null) {
                int routineCount = DataManager.getInstance().getRoutinesByCategory(existingCategory.getId()).size();
                String message = "'" + existingCategory.getName() + "' 카테고리를 삭제하시겠습니까?";
                if (routineCount > 0) {
                    message += "\n\n⚠️ 이 카테고리에 포함된 " + routineCount + "개의 루틴도 함께 삭제됩니다.";
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("카테고리 삭제")
                        .setMessage(message)
                        .setPositiveButton("삭제", (dialog, which) -> {
                            DataManager.getInstance().deleteCategoryWithRoutines(existingCategory.getId());
                            DataManager.getInstance().saveToPrefs(requireContext());
                            if (listener != null) listener.onDataChanged();
                            dismiss();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "카테고리 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingCategory != null) {
                // Edit mode: update existing
                existingCategory.setName(name);
            } else {
                // Add mode: create new
                int newId = DataManager.getInstance().getNextCategoryId();
                Category newCat = new Category(newId, name);
                DataManager.getInstance().addCategory(newCat);
            }

            DataManager.getInstance().saveToPrefs(requireContext());
            if (listener != null) listener.onDataChanged();
            dismiss();
        });
    }
}
