
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Task {
    int id;
    String description;
    String status;
    String createdAt;
    String updatedAt;

    Task(int id, String description, String status,
         String createdAt, String updatedAt) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

public class TaskCLI {
    static final Path FILE = Path.of("tasks.json");
    static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void main(String[] args) {
        try {
            if (!Files.exists(FILE)) {
                Files.writeString(FILE, "[]", StandardCharsets.UTF_8);
            }

            if (args.length == 0) {
                showUsage();
                return;
            }

            List<Task> tasks = loadTasks();
            String command = args[0];

            switch (command) {
                case "add":
                    if (args.length < 2 || args[1].trim().isEmpty()) {
                        System.out.println("Please enter a task description.");
                        return;
                    }

                    int newId = 1;
                    for (Task t : tasks) {
                        if (t.id >= newId) {
                            newId = t.id + 1;
                        }
                    }

                    String now = currentTime();
                    tasks.add(new Task(newId, args[1], "todo", now, now));
                    saveTasks(tasks);
                    System.out.println("Task added successfully (ID: " + newId + ")");
                    break;

                case "update":
                    if (args.length < 3 || args[2].trim().isEmpty()) {
                        System.out.println("Usage: java TaskCLI update <id> \"description\"");
                        return;
                    }

                    Task updateTask = findTask(tasks, parseId(args[1]));
                    if (updateTask == null) {
                        System.out.println("Task not found.");
                        return;
                    }

                    updateTask.description = args[2];
                    updateTask.updatedAt = currentTime();
                    saveTasks(tasks);
                    System.out.println("Task updated successfully.");
                    break;

                case "delete":
                    if (args.length < 2) {
                        System.out.println("Usage: java TaskCLI delete <id>");
                        return;
                    }

                    Task deleteTask = findTask(tasks, parseId(args[1]));
                    if (deleteTask == null) {
                        System.out.println("Task not found.");
                        return;
                    }

                    tasks.remove(deleteTask);
                    saveTasks(tasks);
                    System.out.println("Task deleted successfully.");
                    break;

                case "mark-in-progress":
                case "mark-done":
                    if (args.length < 2) {
                        System.out.println("Usage: java TaskCLI " + command + " <id>");
                        return;
                    }

                    Task markTask = findTask(tasks, parseId(args[1]));
                    if (markTask == null) {
                        System.out.println("Task not found.");
                        return;
                    }

                    if (command.equals("mark-done")) {
                        markTask.status = "done";
                    } else {
                        markTask.status = "in-progress";
                    }

                    markTask.updatedAt = currentTime();
                    saveTasks(tasks);
                    System.out.println("Task status updated to " + markTask.status);
                    break;

                case "list":
                    String filter = args.length > 1 ? args[1] : "all";

                    if (args.length > 2) {
                        System.out.println("Usage: java TaskCLI list [todo|in-progress|done]");
                        return;
                    }

                    if (!filter.equals("all") && !isValidStatus(filter)) {
                        System.out.println("Invalid status. Use todo, in-progress, or done.");
                        return;
                    }

                    listTasks(tasks, filter);
                    break;

                default:
                    System.out.println("Unknown command.");
                    showUsage();
            }
        } catch (NumberFormatException e) {
            System.out.println("Task ID must be a valid integer.");
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static String currentTime() {
        return LocalDateTime.now().format(FORMAT);
    }

    static int parseId(String value) {
        int id = Integer.parseInt(value);
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive.");
        }
        return id;
    }

    static boolean isValidStatus(String status) {
        return status.equals("todo")
                || status.equals("in-progress")
                || status.equals("done");
    }

    static Task findTask(List<Task> tasks, int id) {
        for (Task t : tasks) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    static void listTasks(List<Task> tasks, String filter) {
        boolean found = false;

        for (Task t : tasks) {
            if (filter.equals("all") || t.status.equals(filter)) {
                System.out.println("ID: " + t.id);
                System.out.println("Description: " + t.description);
                System.out.println("Status: " + t.status);
                System.out.println("Created At: " + t.createdAt);
                System.out.println("Updated At: " + t.updatedAt);
                System.out.println("-------------------------");
                found = true;
            }
        }

        if (!found) {
            System.out.println("No tasks found.");
        }
    }

    static List<Task> loadTasks() throws IOException {
        String json = Files.readString(FILE, StandardCharsets.UTF_8).trim();
        List<Task> tasks = new ArrayList<>();

        if (json.isEmpty() || json.equals("[]")) {
            return tasks;
        }

        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IOException("Invalid JSON task file.");
        }

        Pattern pattern = Pattern.compile("\\{([^{}]*)\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String object = matcher.group(1);

            int id = Integer.parseInt(readNumber(object, "id"));
            String description = readString(object, "description");
            String status = readString(object, "status");
            String createdAt = readString(object, "createdAt");
            String updatedAt = readString(object, "updatedAt");

            if (!isValidStatus(status)) {
                throw new IOException("Invalid task status.");
            }

            tasks.add(new Task(id, description, status, createdAt, updatedAt));
        }

        if (tasks.isEmpty()) {
            throw new IOException("Could not read tasks from JSON.");
        }

        return tasks;
    }

    static String readNumber(String object, String key) throws IOException {
        Pattern p = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(object);

        if (!m.find()) {
            throw new IOException("Missing field: " + key);
        }

        return m.group(1);
    }

    static String readString(String object, String key) throws IOException {
        Pattern p = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(object);

        if (!m.find()) {
            throw new IOException("Missing field: " + key);
        }

        return unescape(m.group(1));
    }

    static String escape(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default: result.append(c);
            }
        }

        return result.toString();
    }

    static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (escaped) {
                switch (c) {
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    default: result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                result.append(c);
            }
        }

        if (escaped) {
            result.append('\\');
        }

        return result.toString();
    }

    static void saveTasks(List<Task> tasks) throws IOException {
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);

            json.append("  {\n");
            json.append("    \"id\": ").append(t.id).append(",\n");
            json.append("    \"description\": \"").append(escape(t.description)).append("\",\n");
            json.append("    \"status\": \"").append(escape(t.status)).append("\",\n");
            json.append("    \"createdAt\": \"").append(escape(t.createdAt)).append("\",\n");
            json.append("    \"updatedAt\": \"").append(escape(t.updatedAt)).append("\"\n");
            json.append("  }");

            if (i < tasks.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");
        Files.writeString(FILE, json.toString(), StandardCharsets.UTF_8);
    }

    static void showUsage() {
        System.out.println("Task Tracker Commands:");
        System.out.println("java TaskCLI add \"description\"");
        System.out.println("java TaskCLI update <id> \"description\"");
        System.out.println("java TaskCLI delete <id>");
        System.out.println("java TaskCLI mark-in-progress <id>");
        System.out.println("java TaskCLI mark-done <id>");
        System.out.println("java TaskCLI list");
        System.out.println("java TaskCLI list todo");
        System.out.println("java TaskCLI list in-progress");
        System.out.println("java TaskCLI list done");
    }
}
