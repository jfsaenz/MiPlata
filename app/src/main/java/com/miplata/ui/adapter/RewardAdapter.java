package com.miplata.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.miplata.R;
import com.miplata.data.Reward;

public class RewardAdapter extends ListAdapter<Reward, RewardAdapter.RewardViewHolder> {

    public RewardAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Reward> DIFF_CALLBACK = new DiffUtil.ItemCallback<Reward>() {
        @Override
        public boolean areItemsTheSame(@NonNull Reward oldItem, @NonNull Reward newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reward oldItem, @NonNull Reward newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getCostInPoints() == newItem.getCostInPoints();
        }
    };

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward currentReward = getItem(position);
        holder.tvPartnerName.setText(currentReward.getPartnerName());
        holder.tvRewardTitle.setText(currentReward.getTitle());
        holder.tvRewardCost.setText(String.format("%d pts", currentReward.getCostInPoints()));
        // Aquí iría la lógica para cargar el logo con una librería como Glide o Picasso
        // holder.ivPartnerLogo.setImageResource(...);
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPartnerName;
        private final TextView tvRewardTitle;
        private final TextView tvRewardCost;
        private final ImageView ivPartnerLogo;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPartnerName = itemView.findViewById(R.id.tv_partner_name);
            tvRewardTitle = itemView.findViewById(R.id.tv_reward_title);
            tvRewardCost = itemView.findViewById(R.id.tv_reward_cost);
            ivPartnerLogo = itemView.findViewById(R.id.iv_partner_logo);
        }
    }
}
