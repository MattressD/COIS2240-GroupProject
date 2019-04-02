package productivityplanner.database;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import productivityplanner.data.JournalEntry;
import productivityplanner.data.Task;
import productivityplanner.ui.calendar.CalendarController;
import productivityplanner.ui.main.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static productivityplanner.ui.main.Main.getFXMLController;

public class DatabaseHelper {

    public static boolean insertTask(Task task){
        try{
            String query = "INSERT INTO TASK VALUES (?,?,?,?)";
            PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query);
            statement.setString(1, task.getName());
            statement.setString(2, task.getColor().toString());
            statement.setString(3, "false"); // Task completion variable will always be false when a new task is created/added.
            statement.setString(4, task.getDate().toString());
            int result = statement.executeUpdate();
            return (result > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to add new task.");
            alert.showAndWait();
        }
        return false;
    }

    public static boolean deleteTask(Task task) {
        try{
            String query = "DELETE FROM TASK WHERE (NAME=? AND COLOUR=? AND DATE=?)";
            PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query);
            statement.setString(1, task.getName());
            statement.setString(2, task.getColor().toString());
            statement.setString(3, task.getDate().toString());
            int result = statement.executeUpdate();
            getFXMLController().loadTasks();
            return (result > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateTask(String taskName, String taskColour, Task task){
        String previousName = task.getName();
        Color previousColor = task.getColor();
        try{
            String query = "UPDATE TASK SET NAME = ?, COLOUR = ? WHERE (NAME = ? AND COLOUR = ? AND DATE = ?)";
            PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query);
            statement.setString(1, taskName);
            statement.setString(2, taskColour);
            statement.setString(3,previousName);
            statement.setString(4,previousColor.toString());
            statement.setString(5, CalendarController.selectedDay.getDate().toString());
            int result = statement.executeUpdate();
            if (result > 0) // If it was successful, reload the task list.
            {
                getFXMLController().loadTasks();
            }
            return (result > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean loadTasks(ObservableList<Task> taskList) {
        taskList.clear();
        try{
            String query = "SELECT * FROM TASK WHERE date=\'" + CalendarController.selectedDay.getDate() + "\'";
            ResultSet results = DatabaseHandler.executeQuery(query);

            while(results.next()){
                String name = results.getString("name");
                String color = results.getString("colour");
                Boolean complete = results.getBoolean("isComplete");
                String date = results.getString("date");

                taskList.add(new Task(LocalDate.parse(date), name, Color.web(color), complete));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to load tasks.");
            alert.showAndWait();
        }
        return false;
    }

    public static boolean toggleComplete(Task task) {
        Boolean complete = task.getCompleted();

        // TOGGLE COMPLETE
        if (complete)
            complete = false;
        else
            complete = true;
        try{
            String query = "UPDATE TASK SET ISCOMPLETE = ? WHERE (NAME = ? AND COLOUR = ? AND DATE = ?)";
            PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query);
            statement.setString(1, complete.toString());
            statement.setString(2, task.getName());
            statement.setString(3, task.getColor().toString());
            statement.setString(4, CalendarController.selectedDay.getDate().toString());
            int result = statement.executeUpdate();
            if (result > 0){
                getFXMLController().loadTasks();
            }
            return (result > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean insertJournalEntry(JournalEntry entry){
        try {
            String action = "INSERT INTO JOURNAL VALUES (?, ?)";
            PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(action);
            statement.setString(1, entry.getDate().toString());
            statement.setString(2, entry.getText());
            int result = statement.executeUpdate();
            return (result > 1);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to add new entry.");
            alert.showAndWait();
        }
        return false;
    }

    public static boolean updateJournalEntry(JournalEntry entry){
        try{
            String query = "UPDATE JOURNAL SET TEXT = ? WHERE DATE=?";
            PreparedStatement statement = DatabaseHandler.getConnection().prepareStatement(query);
            statement.setString(1, entry.getText());
            statement.setString(2, entry.getDate().toString());
            int result = statement.executeUpdate();
            return (result > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to update journal entry.");
            alert.showAndWait();
        }
        return false;
    }

    public static boolean loadJournal(ObservableList<JournalEntry> entryList){
        entryList.clear();
        try{
            String query = "SELECT * FROM JOURNAL WHERE date=\'" + CalendarController.selectedDay.getDate() + "\'";
            ResultSet results = DatabaseHandler.executeQuery(query);
            while(results.next()) {
                String text = results.getString("text");
                String date = results.getString("date");
                entryList.add(new JournalEntry(text, LocalDate.parse(date)));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to load journal entry.");
            alert.showAndWait();
        }
        return false;
    }
}