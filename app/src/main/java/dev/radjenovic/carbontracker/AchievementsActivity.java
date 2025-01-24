package dev.radjenovic.carbontracker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView achievementsRecyclerView;
    private AchievementsAdapter adapter;
    private List<Achievement> achievementsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate achievements
        achievementsList = getAchievements();

        // Set up adapter
        adapter = new AchievementsAdapter(achievementsList, this);
        achievementsRecyclerView.setAdapter(adapter);
    }

    private List<Achievement> getAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        achievements.add(new Achievement("First Trip Logged", "Log your first trip.", true));
        achievements.add(new Achievement("Eco Saver", "Save 10 kg of CO₂ emissions.", false));
        achievements.add(new Achievement("Cycling Champ", "Travel 50 km by bicycle.", false));
        achievements.add(new Achievement("Walking Hero", "Walk 20 km in total.", true));

        return achievements;
    }
}
