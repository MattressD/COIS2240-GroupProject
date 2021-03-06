package productivityplanner.ui.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

import static productivityplanner.ui.main.Main.getMainController;

// Reference: https://github.com/SirGoose3432/javafx-calendar
public class Calendar {
    private static ArrayList<Day> calendarDays = new ArrayList<>(42); // 6 rows * 7 columns = all the visible months
    private VBox view;
    private Text calendarTitle;
    private YearMonth currentYearMonth;
    private static LocalDate currentDate;
    public static Day selectedDay;

    Calendar(YearMonth yearMonth){
        currentYearMonth = yearMonth; // Get the current year and month.
        currentDate = LocalDate.now();
        System.out.println(currentDate);

        // Create a new grid for the calendar.
        GridPane calendar = new GridPane();
        calendar.setMinSize(350, 550);
        int CALENDAR_WIDTH = 910;
        int CALENDAR_HEIGHT = 720;
        calendar.setPrefSize(CALENDAR_WIDTH, CALENDAR_HEIGHT);
        calendar.getStyleClass().add("calendar-grid");
        calendar.setGridLinesVisible(true);
        calendar.setAlignment(Pos.CENTER);

        // For every day in the calendar
        // 910/7 = 130
        int DAY_WIDTH = CALENDAR_WIDTH / 7;
        int MIN_DAY_WIDTH = 50;
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 7; j++){
                Day day = new Day();
                int MIN_DAY_HEIGHT = 75;
                day.setMinSize(MIN_DAY_WIDTH, MIN_DAY_HEIGHT);
                // 720/6 = 120
                int DAY_HEIGHT = CALENDAR_HEIGHT / 6;
                day.setPrefSize(DAY_WIDTH, DAY_HEIGHT);

                if (j == 0 || j == 6) // Day is a weekend
                    day.getStyleClass().add("weekend");
                else
                    day.getStyleClass().add("weekday");

                calendar.add(day, j, i);    // Add to the grid.
                calendarDays.add(day);      // Add the day to a list so we can reference specific days quickly or iterate through days which are currently displayed.
            }
        }

        calendar.setMinWidth(350);

        // Create labels for each day of the week.
        HBox weekdayLabelBox = new HBox(DAY_WIDTH); // This is the container which holds 7 more HBoxes. One for each weekday label.
        weekdayLabelBox.setMinSize(MIN_DAY_WIDTH, 30);
        weekdayLabelBox.setId("weekdayLabelBox");
        weekdayLabelBox.setSpacing(0); // Must be 0, the default value causes issues.
        weekdayLabelBox.setAlignment(Pos.CENTER);

        Text[] Weekdays = {new Text("Sunday"), new Text("Monday"), new Text("Tuesday"), new Text("Wednesday"), new Text("Thursday"), new Text("Friday"), new Text("Saturday")};

        for (Text text: Weekdays){
            text.setFont(new Font(16)); // Set weekday font size.

            HBox pane = new HBox(); // Use HBox here to have the labels easily centered (Panes/AnchorPanes do not have alignment properties).
            pane.setAlignment(Pos.CENTER);
            pane.getStyleClass().add("weekday-pane");
            pane.setMinSize(MIN_DAY_WIDTH, 30);
            pane.setPrefSize(DAY_WIDTH, 30);
            pane.getChildren().add(text);               // Add the text to the day box.
            weekdayLabelBox.getChildren().add(pane);    // Add the pane to the weekday box.
        }

        // Create calendarTitle and buttons to change current month.
        calendarTitle = new Text();
        calendarTitle.getStyleClass().add("calendar-title");

        // Create the arrow images which will be used to change the current month.
        int arrowButtonSize = 30;

        ImageView nextArrow = new ImageView("productivityplanner/ui/icons/outline_arrow_forward_ios_white_48dp.png");
        nextArrow.setFitHeight(arrowButtonSize);
        nextArrow.setPreserveRatio(true);

        ImageView previousArrow = new ImageView("productivityplanner/ui/icons/outline_arrow_back_ios_white_48dp.png");
        previousArrow.setFitHeight(arrowButtonSize);
        previousArrow.setPreserveRatio(true);

        // Create new buttons.
        Button nextMonth = new Button("", nextArrow);
        Button previousMonth = new Button("", previousArrow);

        // If a button is pressed, call a function to change the calendar.
        previousMonth.setOnAction(e -> previousMonth());
        nextMonth.setOnAction(e -> nextMonth());

        // Create new panes for the arrows and the current month/year.
        HBox previousPane = new HBox(previousMonth);
        HBox nextPane = new HBox(nextMonth);
        HBox titlePane = new HBox(calendarTitle);

        // Set alignment of each
        previousPane.setAlignment(Pos.CENTER_RIGHT);
        nextPane.setAlignment(Pos.CENTER_LEFT);
        titlePane.setAlignment(Pos.CENTER);

        HBox titleBar = new HBox(previousPane, titlePane, nextPane);
        titleBar.setSpacing(50);
        titleBar.getStyleClass().add("calendar-title-pane");
        titleBar.setAlignment(Pos.BOTTOM_CENTER);

        // Populate calendar with the appropriate day numbers.
        updateCalendar(yearMonth);
        selectedDay = FindDay(currentDate);

        // Create the calendar view (which is added to the calendarPane AnchorPane which you can see in Scenebuilder).
        view = new VBox(titleBar, weekdayLabelBox, calendar);
        view.setMinSize(700, 600);

        // Set the titlePane's minimum width so that the the month arrows don't move.
        titlePane.setMinSize((view.getMinWidth()/1.8), titleBar.getMinHeight());
    }

    // Sets up each day that is currently visible, including setting their dates.
    public void updateCalendar(YearMonth yearMonth) {
        System.out.println("Calendar Updated");
        // Get the date we want to start with on the calendar
        LocalDate calendarDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);

        // Go back one day at a time until it is SUNDAY (unless the month starts on a sunday)
        while (!calendarDate.getDayOfWeek().toString().equals("SUNDAY") ) {
            calendarDate = calendarDate.minusDays(1);
        }

        // Populate the days on the calendar with numbers and information (task and journal data).
        for (Day currentDay : calendarDays) {
            updateDay(currentDay, calendarDate);

            calendarDate = calendarDate.plusDays(1); // Iterate to next day (Equivalent to: "days++;").
        }

        // Change the title of the calendar
        calendarTitle.setText(yearMonth.getMonth().toString() + " " + (yearMonth.getYear()));
    }

    // Used to update the calendar for a single day, rather than all the days.
    void updateDay(Day day, LocalDate date){
        if (date == null){
            date = day.getDate();
        }
        // Then we can change it in the same way we do updateDays().
        if (day.getChildren().size() > 0){
            day.getChildren().clear();
        }
        // Individual day's number
        Text txt = new Text(String.valueOf(date.getDayOfMonth()));
        HBox banner = new HBox(txt);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(0,3,0,3));
        day.setBanner(banner);

        day.setDate(date);
        day.getChildren().add(banner);

        // Individual day's tasks if they exist.
        day.updateTasks();
    }

    // Checks to see if the given date is currently being displayed on the calendar and returns that day, otherwise null.
    public static Day FindDay(LocalDate newDay)
    {
        for(Day day: calendarDays) { // Find the current day.
            if (day.getDate().compareTo(newDay) == 0) {
                return day;
            }
        }
        System.out.println("FindDay: Could not find day.");
        return null;
    }

    // Switch the calendar to the previous month and update the calendar.
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar(currentYearMonth);
        getMainController().updateSelectedDate(FindDay(LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonthValue(), 1))); // Highlight the current date.
    }

    // Switch the calendar to the next month and update the calendar.
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar(currentYearMonth);
        getMainController().updateSelectedDate(FindDay(LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonthValue(), 1))); // Highlight the current date.
    }

    public VBox getView() {
        return view;
    }

    // Prevents each day from going outside of its grid square.
    public void setDayClips() {
        for (Day day: calendarDays) {
            day.setClip(new Rectangle(day.getWidth(), day.getHeight()));
        }
    }
}
