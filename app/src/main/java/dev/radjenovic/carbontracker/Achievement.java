package dev.radjenovic.carbontracker;

public class Achievement {
    private String title;
    private String description;
    private boolean isUnlocked;

    public Achievement(String title, String description, boolean isUnlocked) {
        this.title = title;
        this.description = description;
        this.isUnlocked = isUnlocked;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}
