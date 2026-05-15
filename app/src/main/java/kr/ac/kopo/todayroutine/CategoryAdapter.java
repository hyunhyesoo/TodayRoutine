package kr.ac.kopo.todayroutine;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kopo.todayroutine.data.Category;
import kr.ac.kopo.todayroutine.data.DataManager;
import kr.ac.kopo.todayroutine.data.Routine;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private List<Routine> allRoutines;
    private String[] folderColors = { "#FFD54F", "#FFEB3B", "#FFF59D", "#FFB74D", "#FFE082", "#FFCC80" };
    private OnCategoryEditListener editListener;

    public interface OnCategoryEditListener {
        void onCategoryEdit(Category category);

        void onRoutineLongClick(Routine routine);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryEditListener editListener) {
        this.categories = categories;
        this.allRoutines = DataManager.getInstance().getRoutineList();
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Refresh the routine list reference (in case data changed)
        allRoutines = DataManager.getInstance().getRoutineList();

        // Filter routines for this category
        List<Routine> categoryRoutines = new ArrayList<>();
        for (Routine r : allRoutines) {
            if (r.getCategoryId() == category.getId()) {
                categoryRoutines.add(r);
            }
        }

        holder.bind(category, categoryRoutines, position);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTab, tvCount;
        ConstraintLayout layoutBody;
        LinearLayout layoutExpandedContent;
        ImageView ivEdit;
        RecyclerView rvCategoryRoutines;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTab = itemView.findViewById(R.id.tvTab);
            tvCount = itemView.findViewById(R.id.tvCount);
            layoutBody = itemView.findViewById(R.id.layoutBody);
            layoutExpandedContent = itemView.findViewById(R.id.layoutExpandedContent);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            rvCategoryRoutines = itemView.findViewById(R.id.rvCategoryRoutines);

            // Tab Click toggles accordion
            tvTab.setOnClickListener(v -> toggleExpansion());
            layoutBody.setOnClickListener(v -> toggleExpansion());
        }

        private void toggleExpansion() {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Category clicked = categories.get(pos);
                boolean currentlyExpanded = clicked.isExpanded();

                // Collapse all
                for (Category c : categories) {
                    c.setExpanded(false);
                }

                // Toggle clicked
                if (!currentlyExpanded) {
                    clicked.setExpanded(true);
                }

                notifyDataSetChanged();
            }
        }

        public void bind(Category category, List<Routine> categoryRoutines, int position) {
            // First item: no negative margin (prevent overlap with hint text above)
            // Other items: keep -16dp for the overlapping tab design
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            if (position == 0) {
                lp.topMargin = 0;
            } else {
                lp.topMargin = (int) (-16 * itemView.getResources().getDisplayMetrics().density);
            }
            itemView.setLayoutParams(lp);

            tvTab.setText(category.getName());
            tvCount.setText("총 " + categoryRoutines.size() + "개");

            // Apply Dynamic Folder Colors
            String hexColor = folderColors[position % folderColors.length];
            int color = Color.parseColor(hexColor);

            GradientDrawable tabBg = new GradientDrawable();
            tabBg.setColor(color);
            tabBg.setCornerRadii(new float[] { 36, 36, 36, 36, 0, 0, 0, 0 });
            tvTab.setBackground(tabBg);

            GradientDrawable bodyBg = new GradientDrawable();
            bodyBg.setColor(color);
            bodyBg.setCornerRadii(new float[] { 0, 0, 36, 36, 36, 36, 36, 36 });
            layoutBody.setBackground(bodyBg);

            // Set accordion visibility
            layoutExpandedContent.setVisibility(category.isExpanded() ? View.VISIBLE : View.GONE);

            // Edit button -> open CategoryEditDialog
            ivEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onCategoryEdit(category);
                }
            });

            // Setup inner RecyclerView with long-click support
            rvCategoryRoutines.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            CategoryRoutineAdapter routineAdapter = new CategoryRoutineAdapter(categoryRoutines, editListener);
            rvCategoryRoutines.setAdapter(routineAdapter);
        }
    }
}
