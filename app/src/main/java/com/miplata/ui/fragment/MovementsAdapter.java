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

    private List<Transaction> movements;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public MovementsAdapter(List<Transaction> movements) {
        this.movements = movements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = movements.get(position);
        holder.tvDescription.setText(transaction.getDescription());
        holder.tvDate.setText(sdf.format(transaction.getDateMillis()));

        String formattedAmount = String.format(Locale.getDefault(), "$%,.2f", transaction.getAmount());
        if ("DEBIT".equals(transaction.getType())) {
            holder.tvAmount.setText("- " + formattedAmount);
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
        } else {
            holder.tvAmount.setText("+ " + formattedAmount);
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
        }
    }

    @Override
    public int getItemCount() {
        return movements.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.movements = transactions;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
