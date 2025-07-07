package com.example.tutorial;
public class Helper1 {
    private String taskName;
    private String taskContent;

    public Helper1() {} // Needed for Firebase

    public Helper1(String taskName, String taskContent) {
        this.taskName = taskName;
        this.taskContent = taskContent;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskContent() {
        return taskContent;
    }
}
