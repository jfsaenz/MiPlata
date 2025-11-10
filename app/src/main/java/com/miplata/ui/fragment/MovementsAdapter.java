package com.miplata.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.miplata.R;
import com.miplata.data.Transaction;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MovementsAdapter extends RecyclerView.Adapter<MovementsAdapter.ViewHolder> {

    public interface OnMovementClickListener {
        void onMovementClick(Transaction transaction);
        void onMovementLongClick(Transaction transaction);
    }

    private List<Transaction> movements;
    private final OnMovementClickListener clickListener;

    public MovementsAdapter(List<Transaction> movements, OnMovementClickListener clickListener) {
        this.movements = movements;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(movements.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return movements.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.movements = transactions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvAmount, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }

        public void bind(final Transaction transaction, final OnMovementClickListener clickListener) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDescription.setText(transaction.getDescription());
            tvDate.setText(sdf.format(transaction.getDateMillis()));

            if (transaction.getCategory() != null && !transaction.getCategory().isEmpty()) {
                tvCategory.setText(transaction.getCategory());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            String formattedAmount = String.format(Locale.getDefault(), "$%,.2f", transaction.getAmount());
            if ("DEBIT".equals(transaction.getType())) {
                tvAmount.setText("- " + formattedAmount);
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
            } else {
                tvAmount.setText("+ " + formattedAmount);
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
            }

            itemView.setOnClickListener(v -> clickListener.onMovementClick(transaction));
            itemView.setOnLongClickListener(v -> {
                clickListener.onMovementLongClick(transaction);
                return true;
            });
        }
    }
}
