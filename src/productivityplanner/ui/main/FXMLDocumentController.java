package productivityplanner.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import productivityplanner.data.JournalEntry;
import productivityplanner.data.Task;
import productivityplanner.database.DatabaseHandler;
import productivityplanner.database.DatabaseHelper;
import productivityplanner.ui.taskcell.TaskCellController;
import productivityplanner.utility.Utility;

import java.io.IOException;
import java.net.URL;
import java.time.YearMonth;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {
    // Generally you don't need to instantiate buttons here because you reference their actions, not the button itself.
    @FXML
    private VBox calendarPane;
    @FXML
    private JFXButton btnUndoDelete;
    @FXML
    private Tab tabTasks;
    @FXML
    private JFXListView<Task> tasks;
    @FXML
    private Label lblJournalDate;
    @FXML
    private TextArea txtJournal;
    @FXML
    private JFXButton btnToggleCompleted;
    @FXML
    private JFXButton btnToggleUncompleted;

    static public Boolean toggleComplete = true;
    static public Boolean toggleUncomplete = true;

    public ObservableList<Task> taskList = FXCollections.observableArrayList();
    ObservableList<JournalEntry> entryList = FXCollections.observableArrayList();

    DatabaseHandler databaseHandler = DatabaseHandler.getInstance(); // DO NOT REMOVE (This instantiates the database handler on startup and is required, regardless of if we use it in this class or not)

    Calendar calendar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        YearMonth date = YearMonth.now();

        this.calendar = new Calendar(date);
        Node calendarView = calendar.getView();
        calendarPane.getChildren().add(calendarView);  // Generate the calendar GUI.

        updateSelectedDate(Calendar.selectedDay);   // Highlight the current date.
        updateJournalTitle();                       // Update the journal view.
        loadTasks();                                // Update the task lists.

        tasks.setCellFactory(completedListView -> new TaskCellController()); // Cell factory creates new task cells for each task.
    }

    @FXML
    // Sets the title of the journal to the current date.
    private void updateJournalTitle() {
        lblJournalDate.setText(Calendar.selectedDay.getFormattedDate());
    }

    public void setSelectedDay(Day date){
        if (date != Calendar.selectedDay) { // The data should only be loaded if a new day is selected to prevent reloading the same day.
            updateSelectedDate(date);
        }
    }

    // Changes the currently selected day and updates task and journal data to the new day.
    public void updateSelectedDate(Day currentSelectedDay){
        if (currentSelectedDay != null)
        {
            // HIGHLIGHT/BORDER:
            // Clear the border from the current selected date.
            //Calendar.selectedDay.getBanner().setStyle("-fx-background-color: #FFFFFF");
            Calendar.selectedDay.setBorder(new Border(new BorderStroke(Color.web("TRANSPARENT"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));

            // Change the currently selected day to the one that's just been clicked on.
            Calendar.selectedDay = currentSelectedDay;

            // Add a border to the new selected date.
            Calendar.selectedDay.setBorder(new Border(new BorderStroke(Color.web("#292929"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
            //Calendar.selectedDay.getBanner().setStyle("-fx-background-color: #FF9800");

            // UPDATE JOURNAL:
            DatabaseHelper.loadJournal(entryList);
            updateJournal();
            // UPDATE TASKS:
            loadTasks();
        }
        else {
            System.out.println("Unable to update selected date.");
        }
    }

    @FXML
    // Loads the stage for adding a new task
    public void loadAddNewTask(ActionEvent actionEvent) {
        Utility.loadWindow(getClass().getResource("/productivityplanner/ui/addtask/AddNewTask.fxml"), "Add New Task", null);
    }

    @FXML
    // Loads the stage for editing an existing task
    public void loadEditTask(Task task) {
        updateSelectedTask(task);
        Utility.loadWindow(getClass().getResource("/productivityplanner/ui/edittask/EditTask.fxml"), "Edit Task", null);
    }

    //holds the most recently deleted task so it can be undone
    Task savedTask;
    @FXML
    // Loads the stage for deleting a task
    public void loadDeleteTask(Task task){
        savedTask = task;
        updateSelectedTask(task);
        Utility.loadWindow(getClass().getResource("/productivityplanner/ui/deletetask/DeleteTask.fxml"), "Delete Confirmation", null);
    }

    private void updateSelectedTask(Task task) {
        for(Task currentTask : taskList){
            if (currentTask.equals(task))
                tasks.getSelectionModel().select(currentTask);
        }
    }

    // Loads all tasks for the current day and sorts them into the appropriate task list.
    public void loadTasks() {
        tasks.getItems().clear();

        try {
            if (DatabaseHelper.loadTasks(taskList)){            // If the tasks were successfully loaded..
                for(Task task : taskList){
                    if (task.getCompleted()){
                        if (toggleComplete)
                            tasks.getItems().add(task);
                    }
                    else {
                        if (toggleUncomplete)
                            tasks.getItems().add(task);
                    }
                }

                calendar.updateDay(Calendar.selectedDay, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to load tasks.");
            alert.showAndWait();
        }
    }

    // Save the journal entry to the database. (Requires an update/rewrite, because only one entry is allowed per day)
    public void saveJournal(ActionEvent actionEvent) {
        // Get journal text from the form.
        JournalEntry entry = new JournalEntry(txtJournal.getText(), Calendar.selectedDay.getDate());

        // Check if the database has a journal entry yet.
        if (DatabaseHelper.loadJournal(entryList)){     // If a journal entry already exists:
            DatabaseHelper.updateJournalEntry(entry);   // Update the existing entry.
        } else {                                        // OTHERWISE
            DatabaseHelper.insertJournalEntry(entry);   // Create new journal entry.
        }
    }

    // Update the journal title, and text.
    private void updateJournal(){
        updateJournalTitle();
        if (!entryList.isEmpty()){
            txtJournal.setText(entryList.get(0).getText()); // entryList is the journal entry list, which should only be length 1 (index 0)
        } else{
            txtJournal.clear();
        }
    }

    @FXML
    // Refresh the task lists by reloading the date's tasks.
    public void refreshTaskList(ActionEvent actionEvent) {
        loadTasks();
    }

    @FXML
    void undoDelete(ActionEvent event) {
        // Insert the deleted task.
        if(savedTask == null){

        }else if (DatabaseHelper.insertTask(savedTask)) {  // Insert the task into the database.
            savedTask = null;
            loadTasks();    // Reload the task lists so the new task is displayed.
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to add new task.");
            alert.showAndWait();
            System.out.println("Error: Unable to add task.");
        }
    }

    public void toggleCompleted(ActionEvent actionEvent) {
        taskList.clear();

        if (toggleComplete) {
            toggleComplete = false;
            ImageView image = new ImageView("productivityplanner/ui/icons/outline_check_box_outline_blank_white_48dp.png");
            image.setFitHeight(30);
            image.setFitWidth(30);
            btnToggleCompleted.setGraphic(image);
        }
        else
        {
            toggleComplete = true;
            ImageView image = new ImageView("productivityplanner/ui/icons/outline_check_box_white_48dp.png");
            image.setFitHeight(30);
            image.setFitWidth(30);
            btnToggleCompleted.setGraphic(image);
        }

        refreshTaskList(null);
    }
    //TODO: Change the imageviews to be created ONCE and then ACCESSED, rather than created each time the button is pressed.
    public void toggleUncompleted(ActionEvent actionEvent) {
        taskList.clear();

        ImageView image = new ImageView();
        image.setFitHeight(30);
        image.setFitHeight(30);

        if (toggleUncomplete) {
            toggleUncomplete = false;
            image = new ImageView("productivityplanner/ui/icons/outline_check_box_outline_blank_white_48dp.png");
        }
        else {
            toggleUncomplete = true;
            image = new ImageView("productivityplanner/ui/icons/outline_check_box_white_48dp.png");
        }

        btnToggleUncompleted.setGraphic(image);
        refreshTaskList(null);
    }
}
