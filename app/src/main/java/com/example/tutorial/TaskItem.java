package com.example.tutorial;

public class TaskItem {
    private String title;
    private String url;

    public TaskItem() {
        // Default constructor required for calls to DataSnapshot.getValue(TaskItem.class)
    }

    public TaskItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}

