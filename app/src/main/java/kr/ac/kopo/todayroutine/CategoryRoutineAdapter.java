package kr.ac.kopo.todayroutine;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.ac.kopo.todayroutine.data.Routine;

public class CategoryRoutineAdapter extends RecyclerView.Adapter<CategoryRoutineAdapter.ViewHolder> {

    private List<Routine> routines;
    private CategoryAdapter.OnCategoryEditListener editListener;

    public CategoryRoutineAdapter(List<Routine> routines, CategoryAdapter.OnCategoryEditListener editListener) {
        this.routines = routines;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_routine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine);
    }

    @Override
    public int getItemCount() {
        return routines != null ? routines.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoutineName, tvPercent;
        TextView[] days;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoutineName = itemView.findViewById(R.id.tvRoutineName);
            tvPercent = itemView.findViewById(R.id.tvPercent);

            days = new TextView[] {
                    itemView.findViewById(R.id.daySun),
                    itemView.findViewById(R.id.dayMon),
                    itemView.findViewById(R.id.dayTue),
                    itemView.findViewById(R.id.dayWed),
                    itemView.findViewById(R.id.dayThu),
                    itemView.findViewById(R.id.dayFri),
                    itemView.findViewById(R.id.daySat)
            };

            // Long click listener -> open RoutineEditDialog
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && editListener != null) {
                    editListener.onRoutineLongClick(routines.get(pos));
                }
                return true;
            });
        }

        public void bind(Routine routine) {
            tvRoutineName.setText(routine.getName());
            tvPercent.setText(routine.getAchievementRate() + "%");

            for (int i = 0; i < 7; i++) {
                int dayOfWeek = i + 1; // 1:Sun, 2:Mon...
                if (routine.getDaysOfWeek().contains(dayOfWeek)) {
                    days[i].setBackgroundResource(R.drawable.bg_circle_day_active);
                    days[i].setTextColor(Color.WHITE);
                } else {
                    days[i].setBackgroundResource(R.drawable.bg_circle_day_inactive);
                    days[i].setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_sub));
                }
            }
        }
    }
}
