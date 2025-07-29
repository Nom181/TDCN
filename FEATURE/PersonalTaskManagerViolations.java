import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManagerKISS {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JSONObject addTask(String title, String description, String dueDateStr, String priority) {
        if (!isValidInput(title, dueDateStr, priority)) return null;

        JSONArray tasks = loadTasks();
        if (isDuplicate(tasks, title, dueDateStr)) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại.");
            return null;
        }

        JSONObject task = createTask(title, description, dueDateStr, priority);
        tasks.add(task);
        saveTasks(tasks);
        System.out.println("Đã thêm nhiệm vụ mới: " + task.get("id"));
        return task;
    }

    // Phương thức trợ giúp để lưu dữ liệu
    private boolean isValidInput(String title, String dueDateStr, String priority) {
        if (title == null || title.isBlank()) {
            System.out.println("Lỗi: Tiêu đề không hợp lệ.");
            return false;
        }

        if (!isValidDate(dueDateStr)) {
            System.out.println("Lỗi: Ngày đến hạn không đúng định dạng.");
            return false;
        }

        if (!List.of("Thấp", "Trung bình", "Cao").contains(priority)) {
            System.out.println("Lỗi: Ưu tiên không hợp lệ.");
            return false;
        }

        return true;
    }
    
   private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isDuplicate(JSONArray tasks, String title, String dueDateStr) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (title.equalsIgnoreCase((String) task.get("title")) &&
                dueDateStr.equals(task.get("due_date"))) {
                return true;
            }
        }
        return false;
    }

        private JSONObject createTask(String title, String description, String dueDateStr, String priority) {
        JSONObject task = new JSONObject();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        task.put("id", UUID.randomUUID().toString());
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dueDateStr);
        task.put("priority", priority);
        task.put("status", "Chưa hoàn thành");
        task.put("created_at", now);
        task.put("last_updated_at", now);
        return task;
    }

     private JSONArray loadTasks() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = new JSONParser().parse(reader);
            return (JSONArray) obj;
        } catch (IOException | ParseException e) {
            return new JSONArray();
        }
    }

    private void saveTasks(JSONArray tasks) {
        try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
            writer.write(tasks.toJSONString());
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file: " + e.getMessage());
        }
    }
}
