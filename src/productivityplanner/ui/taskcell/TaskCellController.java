package productivityplanner.ui.taskcell;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import productivityplanner.data.Task;
import productivityplanner.database.DatabaseHandler;

import java.awt.*;
import java.io.IOException;

import static productivityplanner.ui.main.Main.getFXMLController;

// References:  https://stackoverflow.com/questions/47511132/javafx-custom-listview
//              https://www.turais.de/how-to-custom-listview-cell-in-javafx/

public class TaskCellController extends ListCell<Task> {
    private static TaskCellController selected;

    public static TaskCellController getSelected() {
        return selected;
    }

    public Task getTask() {
        return task;
    }

    @FXML
    Label lblTaskName;
    @FXML
    CheckBox cbComplete;
    @FXML
    HBox cell;
    @FXML
    JFXButton btnDelete;
    @FXML
    JFXButton btnEdit;

    Task task;
    DatabaseHandler databaseHandler;

    public TaskCellController() {
        loadFXML();
        databaseHandler = DatabaseHandler.getInstance();

        btnDelete.setOnAction(e -> databaseHandler.deleteTask(this.task));
        btnEdit.setOnAction((e -> getFXMLController().loadEditTask(new ActionEvent())));
        cbComplete.setOnAction(e -> databaseHandler.toggleComplete(this.task));
        cell.setOnMouseClicked(e ->{
            System.out.println("cell clicked");
            selected = this;
            getFXMLController().loadTasks();
            getFXMLController().updateSelectedTask();
        });
    }

    private void setCellSelected(boolean bool) {
        btnDelete.setVisible(bool);
        btnEdit.setVisible(bool);
        setGraphic(cell);
    }

    private void loadFXML(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TaskCell.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(Task item, boolean empty){
        super.updateItem(item, empty);
        task = item;

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            String taskColour = item.getColor().toString().substring(2,8); // Substring makes 0xff66cc33 = ff66cc
            lblTaskName.setText(item.getName());
            cell.setStyle("-fx-background-color: #" + taskColour);
            cbComplete.setSelected(item.getCompleted());

            if (selected == this)
                setCellSelected(true);
            else
                setCellSelected(false);

            setGraphic(cell);
        }
    }
}
