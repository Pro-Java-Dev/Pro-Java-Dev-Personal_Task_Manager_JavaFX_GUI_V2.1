package com.taskflow;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private LocalDate dueDate;
    private boolean completed;

    public Task(String title, String description, String category, String priority, LocalDate dueDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
