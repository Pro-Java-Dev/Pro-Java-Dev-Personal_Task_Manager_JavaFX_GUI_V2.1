package com.taskflow;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TaskManagerApp extends Application {
    private final TaskRepository repository = new TaskRepository();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ListView<Task> taskList = new ListView<>();
    private final TextField searchField = new TextField();
    private final Label countLabel = new Label();
    private final Label greetingDate = new Label();
    private final ComboBox<String> sortBox = new ComboBox<>(FXCollections.observableArrayList("Due date", "Priority", "Newest"));
    private String currentFilter = "All tasks";
    private String currentCategory = "All";

    @Override public void start(Stage stage) {
        tasks.setAll(repository.load());
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("app-shell");
        shell.setLeft(buildSidebar());
        shell.setCenter(buildMainContent());
        Scene scene = new Scene(shell, 1240, 800);
        scene.getStylesheets().add(getClass().getResource("/com/taskflow/theme.css").toExternalForm());
        stage.setTitle("Taskflow — your day, in focus");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
        refresh();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);
        HBox brand = new HBox(11, new Label("✦"), new Label("taskflow"));
        brand.getStyleClass().add("brand");
        brand.getChildren().get(0).getStyleClass().add("brand-mark");
        sidebar.getChildren().add(brand);

        Label workspace = new Label("WORKSPACE"); workspace.getStyleClass().add("section-caption");
        sidebar.getChildren().add(workspace);
        sidebar.getChildren().add(navButton("▦", "All tasks", "All tasks"));
        sidebar.getChildren().add(navButton("◷", "Today", "Today"));
        sidebar.getChildren().add(navButton("▤", "Upcoming", "Upcoming"));
        sidebar.getChildren().add(navButton("✓", "Completed", "Completed"));

        Label lists = new Label("YOUR LISTS"); lists.getStyleClass().add("section-caption");
        lists.setPadding(new Insets(29, 0, 10, 18));
        sidebar.getChildren().add(lists);
        sidebar.getChildren().add(categoryButton("●", "Work", "dot-work"));
        sidebar.getChildren().add(categoryButton("●", "Personal", "dot-personal"));

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS); sidebar.getChildren().add(spacer);
        VBox profile = new VBox(3, new Label("YOUR SPACE"), new Label("A little more clarity, every day."));
        profile.getStyleClass().add("sidebar-footer"); sidebar.getChildren().add(profile);
        return sidebar;
    }

    private Button navButton(String icon, String text, String filter) {
        Button button = new Button(icon + "    " + text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> { currentFilter = filter; currentCategory = "All"; refresh(); updateActiveNav(); });
        button.setUserData(filter);
        return button;
    }

    private Button categoryButton(String icon, String text, String dotClass) {
        Button button = new Button(icon + "    " + text);
        button.getStyleClass().addAll("nav-button", "category-nav", dotClass);
        button.setMaxWidth(Double.MAX_VALUE); button.setAlignment(Pos.CENTER_LEFT);
        button.setUserData("category:" + text);
        button.setOnAction(e -> { currentCategory = text; currentFilter = "All tasks"; refresh(); updateActiveNav(); });
        return button;
    }

    private void updateActiveNav() {
        VBox sidebar = (VBox) ((BorderPane) taskList.getScene().getRoot()).getLeft();
        for (var node : sidebar.getChildren()) if (node instanceof Button b) {
            boolean active = b.getUserData() != null && (b.getUserData().equals(currentFilter)
                    || b.getUserData().equals("category:" + currentCategory) && !currentCategory.equals("All"));
            b.getStyleClass().remove("nav-active"); if (active) b.getStyleClass().add("nav-active");
        }
    }

    private VBox buildMainContent() {
        VBox main = new VBox(0); main.getStyleClass().add("main-content");
        HBox topbar = new HBox(); topbar.setAlignment(Pos.CENTER_RIGHT); topbar.getStyleClass().add("topbar");
        Label date = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)));
        date.getStyleClass().add("top-date");
        Label avatar = new Label("O"); avatar.getStyleClass().add("avatar");
        topbar.getChildren().addAll(date, avatar);

        VBox heading = new VBox(7);
        greetingDate.getStyleClass().add("eyebrow");
        greetingDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)).toUpperCase(Locale.ROOT));
        Label title = new Label("Make room for\nwhat matters."); title.getStyleClass().add("page-title");
        Label subtitle = new Label("A clear mind starts with a clear plan."); subtitle.getStyleClass().add("page-subtitle");
        heading.getChildren().addAll(greetingDate, title, subtitle);

        HBox actions = new HBox(12); actions.setAlignment(Pos.CENTER_LEFT); actions.getStyleClass().add("toolbar");
        searchField.setPromptText("⌕   Search tasks..."); searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((o, old, value) -> refresh());
        Button add = new Button("＋  New task"); add.getStyleClass().add("primary-button"); add.setOnAction(e -> editTask(null));
        Region push = new Region(); HBox.setHgrow(push, Priority.ALWAYS);
        actions.getChildren().addAll(searchField, push, add);

        HBox listHead = new HBox(); listHead.setAlignment(Pos.CENTER_LEFT); listHead.getStyleClass().add("list-heading");
        Label listTitle = new Label("Your tasks"); listTitle.getStyleClass().add("list-title");
        countLabel.getStyleClass().add("task-count"); Region headPush = new Region(); HBox.setHgrow(headPush, Priority.ALWAYS);
        sortBox.setValue("Due date"); sortBox.getStyleClass().add("sort-box");
        sortBox.valueProperty().addListener((o, old, value) -> refresh());
        listHead.getChildren().addAll(listTitle, countLabel, headPush, new Label("Sort by"), sortBox);

        taskList.setCellFactory(list -> new TaskCell()); taskList.getStyleClass().add("task-list"); VBox.setVgrow(taskList, Priority.ALWAYS);
        VBox.setMargin(taskList, new Insets(0, 0, 0, 0));
        main.getChildren().addAll(topbar, heading, actions, listHead, taskList);
        return main;
    }

    private void refresh() {
        if (taskList.getScene() != null) {
            String sort = String.valueOf(sortBox.getValue());
            List<Task> filtered = tasks.stream().filter(this::matches).filter(t -> {
                String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
                return q.isEmpty() || t.getTitle().toLowerCase(Locale.ROOT).contains(q) || t.getDescription().toLowerCase(Locale.ROOT).contains(q);
            }).sorted(switch (sort) {
                case "Priority" -> Comparator.comparingInt(t -> priorityRank(t.getPriority()));
                case "Newest" -> Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.reverseOrder()));
                default -> Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
            }).toList();
            taskList.setItems(FXCollections.observableArrayList(filtered));
            countLabel.setText(filtered.size() + (filtered.size() == 1 ? " task" : " tasks"));
            updateActiveNav();
        }
    }

    private boolean matches(Task task) {
        if (!currentCategory.equals("All") && !task.getCategory().equals(currentCategory)) return false;
        return switch (currentFilter) {
            case "Today" -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().equals(LocalDate.now());
            case "Upcoming" -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().isAfter(LocalDate.now());
            case "Completed" -> task.isCompleted();
            default -> true;
        };
    }

    private int priorityRank(String priority) { return switch (priority) { case "High" -> 0; case "Medium" -> 1; default -> 2; }; }

    private void editTask(Task existing) {
        Dialog<Task> dialog = new Dialog<>(); dialog.setTitle(existing == null ? "Create a task" : "Edit task");
        dialog.setHeaderText(existing == null ? "A small step toward a clearer day." : "Make a quick update.");
        ButtonType saveType = new ButtonType(existing == null ? "Create task" : "Save changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        TextField title = new TextField(existing == null ? "" : existing.getTitle()); title.setPromptText("e.g. Plan the week");
        TextArea description = new TextArea(existing == null ? "" : existing.getDescription()); description.setPromptText("Add a few details..."); description.setPrefRowCount(3); description.setWrapText(true);
        ComboBox<String> category = new ComboBox<>(FXCollections.observableArrayList("Work", "Personal")); category.setValue(existing == null ? "Work" : existing.getCategory());
        ComboBox<String> priority = new ComboBox<>(FXCollections.observableArrayList("Low", "Medium", "High")); priority.setValue(existing == null ? "Medium" : existing.getPriority());
        DatePicker due = new DatePicker(existing == null ? LocalDate.now() : existing.getDueDate()); due.setPromptText("No due date");
        GridPane grid = new GridPane(); grid.getStyleClass().add("task-form"); grid.setHgap(14); grid.setVgap(14); grid.setPadding(new Insets(12, 2, 4, 2));
        grid.add(new Label("Task name"), 0, 0); grid.add(title, 1, 0);
        grid.add(new Label("Details"), 0, 1); grid.add(description, 1, 1);
        grid.add(new Label("List"), 0, 2); grid.add(category, 1, 2);
        grid.add(new Label("Priority"), 0, 3); grid.add(priority, 1, 3);
        grid.add(new Label("Due date"), 0, 4); grid.add(due, 1, 4);
        ColumnConstraints first = new ColumnConstraints(); first.setMinWidth(80); ColumnConstraints second = new ColumnConstraints(); second.setHgrow(Priority.ALWAYS); second.setPrefWidth(270); grid.getColumnConstraints().addAll(first, second);
        dialog.getDialogPane().setContent(grid); dialog.getDialogPane().getStyleClass().add("task-dialog");
        dialog.getDialogPane().lookupButton(saveType).disableProperty().bind(title.textProperty().isEmpty());
        dialog.setResultConverter(button -> {
            if (button != saveType) return null;
            if (existing == null) return new Task(title.getText().trim(), description.getText().trim(), category.getValue(), priority.getValue(), due.getValue());
            existing.setTitle(title.getText().trim()); existing.setDescription(description.getText().trim()); existing.setCategory(category.getValue()); existing.setPriority(priority.getValue()); existing.setDueDate(due.getValue());
            return existing;
        });
        dialog.showAndWait().ifPresent(task -> { if (!tasks.contains(task)) tasks.add(task); persist(); refresh(); });
    }

    private void persist() { repository.save(tasks); }

    private class TaskCell extends ListCell<Task> {
        @Override protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) { setGraphic(null); setText(null); return; }
            HBox card = new HBox(14); card.setAlignment(Pos.CENTER_LEFT); card.getStyleClass().add("task-card");
            CheckBox done = new CheckBox(); done.setSelected(task.isCompleted()); done.getStyleClass().add("task-check");
            done.setOnAction(e -> { task.setCompleted(done.isSelected()); persist(); refresh(); });
            VBox content = new VBox(6); HBox.setHgrow(content, Priority.ALWAYS);
            Label title = new Label(task.getTitle()); title.getStyleClass().add("task-name"); if (task.isCompleted()) title.getStyleClass().add("task-done");
            Label description = new Label(task.getDescription().isBlank() ? "No details added" : task.getDescription()); description.getStyleClass().add("task-description");
            HBox meta = new HBox(8); meta.setAlignment(Pos.CENTER_LEFT); meta.getChildren().add(categoryPill(task.getCategory()));
            Label priority = new Label("●  " + task.getPriority()); priority.getStyleClass().addAll("priority-pill", "priority-" + task.getPriority().toLowerCase(Locale.ROOT)); meta.getChildren().add(priority);
            content.getChildren().addAll(title, description, meta);
            VBox due = new VBox(3); due.setAlignment(Pos.CENTER_RIGHT); due.getStyleClass().add("due-box");
            Label dueLabel = new Label(task.getDueDate() == null ? "No date" : task.getDueDate().format(DateTimeFormatter.ofPattern("MMM d")));
            dueLabel.getStyleClass().add("due-date");
            Label dueHint = new Label(task.getDueDate() == null ? "" : task.getDueDate().isBefore(LocalDate.now()) && !task.isCompleted() ? "OVERDUE" : task.getDueDate().equals(LocalDate.now()) ? "TODAY" : "DUE DATE");
            dueHint.getStyleClass().add("due-hint"); if (dueHint.getText().equals("OVERDUE")) dueHint.getStyleClass().add("overdue");
            due.getChildren().addAll(dueLabel, dueHint);
            Button edit = new Button("···"); edit.getStyleClass().add("more-button");
            MenuItem editItem = new MenuItem("Edit task"); editItem.setOnAction(e -> editTask(task));
            MenuItem deleteItem = new MenuItem("Delete task"); deleteItem.setOnAction(e -> { tasks.remove(task); persist(); refresh(); });
            edit.setOnAction(e -> new ContextMenu(editItem, deleteItem).show(edit, javafx.geometry.Side.BOTTOM, 0, 0));
            card.getChildren().addAll(done, content, due, edit);
            setGraphic(card);
        }
        private Label categoryPill(String category) { Label label = new Label(category); label.getStyleClass().addAll("category-pill", category.equals("Work") ? "work-pill" : "personal-pill"); return label; }
    }

    @Override public void stop() { persist(); }
    public static void main(String[] args) { launch(args); }
}
