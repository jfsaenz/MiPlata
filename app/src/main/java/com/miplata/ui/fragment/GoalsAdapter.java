package com.miplata.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.miplata.R;
import com.miplata.data.Goal;
import java.util.List;
import java.util.Locale;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder> {

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }

    private List<Goal> goals;
    private final OnGoalClickListener listener;

    public GoalsAdapter(List<Goal> goals, OnGoalClickListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(goals.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvGoalProgress;
        ProgressBar pbGoalProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tv_goal_name);
            tvGoalProgress = itemView.findViewById(R.id.tv_goal_progress);
            pbGoalProgress = itemView.findViewById(R.id.pb_goal_progress);
        }

        public void bind(final Goal goal, final OnGoalClickListener listener) {
            tvGoalName.setText(goal.getName());
            String progressText = String.format(Locale.getDefault(), "$%,.2f / $%,.2f", goal.getCurrentAmount(), goal.getTargetAmount());
            tvGoalProgress.setText(progressText);

            int progress = 0;
            if (goal.getTargetAmount() > 0) {
                progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
            }
            pbGoalProgress.setProgress(progress);

            itemView.setOnClickListener(v -> listener.onGoalClick(goal));
        }
    }
}
