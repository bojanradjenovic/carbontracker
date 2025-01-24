package dev.radjenovic.carbontracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private final List<Achievement> achievements;
    private final Context context;

    public AchievementsAdapter(List<Achievement> achievements, Context context) {
        this.achievements = achievements;
        this.context = context;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);

        holder.titleTextView.setText(achievement.getTitle());
        holder.descriptionTextView.setText(achievement.getDescription());

        if (achievement.isUnlocked()) {
            holder.statusImageView.setImageResource(R.drawable.ic_unlock);
            holder.statusImageView.setContentDescription("Unlocked");
        } else {
            holder.statusImageView.setImageResource(R.drawable.ic_lock);
            holder.statusImageView.setContentDescription("Locked");
        }

        holder.itemView.setOnClickListener(v -> {
            if (achievement.isUnlocked()) {
                Toast.makeText(context, "Achievement already unlocked!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Keep going to unlock this achievement!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;
        ImageView statusImageView;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.achievementTitle);
            descriptionTextView = itemView.findViewById(R.id.achievementDescription);
            statusImageView = itemView.findViewById(R.id.achievementStatus);
        }
    }
}
