package kr.ac.kopo.todayroutine;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.ac.kopo.todayroutine.data.Routine;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<Routine> routines;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCheckboxClicked(int position, boolean isChecked);

        void onEmptyAreaClicked(Routine routine);
    }

    public RoutineAdapter(List<Routine> routines, OnItemClickListener listener) {
        this.routines = routines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine, position);
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    class RoutineViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvRoutineName, tvRoutineDetail, tvConsecutive, tvRoutineMemo;
        TextView[] days;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvRoutineName = itemView.findViewById(R.id.tvRoutineName);
            tvRoutineDetail = itemView.findViewById(R.id.tvRoutineDetail);
            tvConsecutive = itemView.findViewById(R.id.tvConsecutive);
            tvRoutineMemo = itemView.findViewById(R.id.tvRoutineMemo);

            days = new TextView[] {
                    itemView.findViewById(R.id.daySun),
                    itemView.findViewById(R.id.dayMon),
                    itemView.findViewById(R.id.dayTue),
                    itemView.findViewById(R.id.dayWed),
                    itemView.findViewById(R.id.dayThu),
                    itemView.findViewById(R.id.dayFri),
                    itemView.findViewById(R.id.daySat)
            };

            // Remove internal listener before setting state to avoid unwanted triggers
            cbCompleted.setOnCheckedChangeListener(null);

            cbCompleted.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    boolean isChecked = cbCompleted.isChecked();
                    listener.onCheckboxClicked(pos, isChecked);
                }
            });

            // "Empty area" click for memo - basically clicking the card itself
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onEmptyAreaClicked(routines.get(pos));
                }
            });
        }

        public void bind(Routine routine, int position) {
            tvRoutineName.setText(routine.getName());
            tvRoutineDetail.setText(routine.getDetail());
            tvConsecutive.setText(routine.getConsecutiveDays() + "일 연속 성공");

            String memo = routine.getMemo();
            if (memo != null && !memo.trim().isEmpty()) {
                tvRoutineMemo.setText(memo);
                tvRoutineMemo.setVisibility(View.VISIBLE);
            } else {
                tvRoutineMemo.setVisibility(View.GONE);
            }

            cbCompleted.setChecked(routine.isCompleted());

            // Dim text if completed and Highlight Consecutive Days
            if (routine.isCompleted()) {
                tvRoutineName.setTextColor(Color.LTGRAY);
                tvRoutineDetail.setTextColor(Color.LTGRAY);
                tvRoutineMemo.setTextColor(Color.LTGRAY);

                tvConsecutive.setBackgroundResource(R.drawable.bg_rounded_primary);
                tvConsecutive.setTextColor(Color.WHITE);
            } else {
                tvRoutineName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_main));
                tvRoutineDetail.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_sub));
                tvRoutineMemo.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_main));

                tvConsecutive.setBackgroundResource(R.drawable.bg_rounded_gray);
                tvConsecutive.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_main));
            }

            // Update Days UI
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
