package kr.ac.kopo.todayroutine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kopo.todayroutine.data.Category;
import kr.ac.kopo.todayroutine.data.DataManager;
import kr.ac.kopo.todayroutine.data.Routine;

public class CategoryFragment extends Fragment implements CategoryAdapter.OnCategoryEditListener {

    private AutoCompleteTextView autoCompleteSearch;
    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private List<Category> categoryList;

    private FloatingActionButton fabMain;
    private TextView fabAddRoutine;
    private TextView fabAddCategory;
    private boolean isFabExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        autoCompleteSearch = view.findViewById(R.id.autoCompleteSearch);
        rvCategories = view.findViewById(R.id.rvCategories);
        fabMain = view.findViewById(R.id.fabMain);
        fabAddRoutine = view.findViewById(R.id.fabAddRoutine);
        fabAddCategory = view.findViewById(R.id.fabAddCategory);

        setupSearch();
        setupRecyclerView();
        setupFab();

        return view;
    }

    private void setupSearch() {
        List<Routine> allRoutines = DataManager.getInstance().getRoutineList();
        List<String> routineNames = new ArrayList<>();
        for (Routine r : allRoutines) {
            routineNames.add(r.getName());
        }

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, routineNames);
        autoCompleteSearch.setAdapter(searchAdapter);

        autoCompleteSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            int targetCategoryId = -1;

            List<Routine> allR = DataManager.getInstance().getRoutineList();
            for (Routine r : allR) {
                if (r.getName().equals(selectedName)) {
                    targetCategoryId = r.getCategoryId();
                    break;
                }
            }

            if (targetCategoryId != -1) {
                int targetIndex = -1;
                for (int i = 0; i < categoryList.size(); i++) {
                    Category c = categoryList.get(i);
                    if (c.getId() == targetCategoryId) {
                        c.setExpanded(true);
                        targetIndex = i;
                    } else {
                        c.setExpanded(false);
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (targetIndex != -1) {
                    rvCategories.smoothScrollToPosition(targetIndex);
                }
            }
        });
    }

    private void setupRecyclerView() {
        categoryList = DataManager.getInstance().getCategoryList();

        if (!categoryList.isEmpty()) {
            categoryList.get(0).setExpanded(true);
        }

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryAdapter(categoryList, this);
        rvCategories.setAdapter(adapter);
    }

    private void refreshData() {
        categoryList = DataManager.getInstance().getCategoryList();
        adapter = new CategoryAdapter(categoryList, this);
        rvCategories.setAdapter(adapter);

        // Also refresh the search autocomplete data
        setupSearch();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshStatistics();
        }
    }

    private void setupFab() {
        fabMain.setOnClickListener(v -> {
            isFabExpanded = !isFabExpanded;
            if (isFabExpanded) {
                fabAddRoutine.setVisibility(View.VISIBLE);
                fabAddCategory.setVisibility(View.VISIBLE);
                fabMain.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                fabAddRoutine.setVisibility(View.GONE);
                fabAddCategory.setVisibility(View.GONE);
                fabMain.setImageResource(android.R.drawable.ic_input_add);
            }
        });

        // FAB -> Add Routine
        fabAddRoutine.setOnClickListener(v -> {
            closeFab();
            RoutineEditDialog dialog = RoutineEditDialog.newInstance(null); // Add mode
            dialog.setOnDataChangeListener(() -> refreshData());
            dialog.show(getParentFragmentManager(), "RoutineEditDialog");
        });

        // FAB -> Add Category
        fabAddCategory.setOnClickListener(v -> {
            closeFab();
            CategoryEditDialog dialog = CategoryEditDialog.newInstance(null); // Add mode
            dialog.setOnDataChangeListener(() -> refreshData());
            dialog.show(getParentFragmentManager(), "CategoryEditDialog");
        });
    }

    private void closeFab() {
        isFabExpanded = false;
        fabAddRoutine.setVisibility(View.GONE);
        fabAddCategory.setVisibility(View.GONE);
        fabMain.setImageResource(android.R.drawable.ic_input_add);
    }

    // ===== OnCategoryEditListener callbacks =====

    @Override
    public void onCategoryEdit(Category category) {
        CategoryEditDialog dialog = CategoryEditDialog.newInstance(category);
        dialog.setOnDataChangeListener(() -> refreshData());
        dialog.show(getParentFragmentManager(), "CategoryEditDialog");
    }

    @Override
    public void onRoutineLongClick(Routine routine) {
        RoutineEditDialog dialog = RoutineEditDialog.newInstance(routine);
        dialog.setOnDataChangeListener(() -> refreshData());
        dialog.show(getParentFragmentManager(), "RoutineEditDialog");
    }
}
