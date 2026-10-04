package com.taskflow;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final Path file = Path.of(System.getProperty("user.home"), ".taskflow", "tasks.dat");

    @SuppressWarnings("unchecked")
    public List<Task> load() {
        if (Files.exists(file)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
                return (List<Task>) in.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException ignored) { }
        }
        return starterTasks();
    }

    public void save(List<Task> tasks) {
        try {
            Files.createDirectories(file.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
                out.writeObject(new ArrayList<>(tasks));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save tasks", e);
        }
    }

    private List<Task> starterTasks() {
        LocalDate today = LocalDate.now();
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Review Q3 product roadmap", "Align milestones with the team before the planning session.", "Work", "High", today));
        tasks.add(new Task("Send the launch notes", "Share the final release notes with the wider team.", "Work", "Medium", today.plusDays(1)));
        tasks.add(new Task("Book a table for Friday", "Somewhere relaxed, close to the office.", "Personal", "Low", today.plusDays(2)));
        tasks.add(new Task("Update portfolio case study", "Add the latest product screenshots and outcomes.", "Personal", "Medium", today.plusDays(4)));
        tasks.add(new Task("Prepare design handoff", "Check assets, spacing, and responsive states.", "Work", "High", today.plusDays(5)));
        return tasks;
    }
}
