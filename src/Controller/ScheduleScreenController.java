package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import Model.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ScheduleScreenController implements Initializable {
    
    // time zone setup 
    private final ZoneId zoneId = ZoneId.systemDefault();
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    @FXML private TableView<Appointment> scheduleTableView;
    @FXML private TableColumn<Appointment, ZonedDateTime> startTimeCol;
    @FXML private TableColumn<Appointment, LocalDateTime> endTimeCol;
    @FXML private TableColumn<Appointment, String> titleCol;
    @FXML private TableColumn<Appointment, String> typeCol;
    @FXML private TableColumn<Appointment, String> customerCol;
    @FXML private TableColumn<Appointment, String> descripCol;
    
    private static Appointment transitionAppt;
    private static ObservableList<String> typeList = FXCollections.observableArrayList();
        
    @FXML private RadioButton weeklyRadioBtn;
    @FXML private RadioButton monthlyRadioBtn;
   
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker apptDatePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private ComboBox<String> endTimeComboBox;
    @FXML private TextField titleTextField;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private TextArea descripTextArea;
    
    public static Appointment getTransitionAppt() {
        return transitionAppt;
    }
    
    public static ObservableList<String> getTypeList() {
        return typeList;
    }
    
    private void setTypeList() {
        typeList.addAll("Telephone", "Video Call", "Consult", "General", "Continuing");
        typeComboBox.setItems(typeList);
    }
    
    @FXML private void removeAppt() throws SQLException {
        // prevent action if no appointment is selected
        if (scheduleTableView.getSelectionModel().getSelectedItem() == null) {
            selectError();
        } else {
            // confirmation alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setContentText("Please confirm deleting appointment: " + scheduleTableView.getSelectionModel().getSelectedItem().getTitle());
            alert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                PreparedStatement statement = null;
                String query = "DELETE FROM appointment "
                        + "WHERE appointment.appointmentId = ?";
                try {
                    statement = LoginScreenController.dbConnect.prepareStatement(query);
                    statement.setInt(1, scheduleTableView.getSelectionModel().getSelectedItem().getAppointmentId());
                    int delete = statement.executeUpdate();
                    if (delete == 1) {
                        System.out.println("Appointment successfully removed.");
                    } else {
                        System.out.println("Appointment removal failed.");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
                clearFields();
                // refresh tableview
                initializeTableView();
            }
        }
    }
    
    // initialize & refresh tableview
    @FXML private void initializeTableView() {
        try {
            populateApptsList();
        } catch (SQLException ex) {
            Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (weeklyRadioBtn.selectedProperty().getValue() == true) {
            try {
                weeklySchedule();
            } catch (SQLException ex) {
                Logger.getLogger(ScheduleScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (monthlyRadioBtn.selectedProperty().getValue() == true) {
            try {
                monthlySchedule();
            } catch (SQLException ex) {
                Logger.getLogger(ScheduleScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Error detecting which schedule radio button was chosen.");
        }
    }
    
    private void selectError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Appointment Selection Error");
        alert.setContentText("You have not selected an appointment to modify.");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
    
    private void monthlySchedule() throws SQLException {
        LocalDate now = LocalDate.now();
        LocalDate nowPlusMonth = now.plusMonths(1);
        FilteredList<Appointment> monthlyFilteredAppts = new FilteredList<>(populateApptsList());
        // multi-line lambda to parse datetime format and return sortable date values
        monthlyFilteredAppts.setPredicate(date -> {
            LocalDate apptDate = LocalDate.parse(date.getStartDate(), dateFormat);
            return apptDate.isAfter(now.minusDays(1)) && apptDate.isBefore(nowPlusMonth);
        });
        scheduleTableView.setItems(monthlyFilteredAppts);
    }
    
    private void weeklySchedule() throws SQLException {
        LocalDate now = LocalDate.now();
        LocalDate nowPlusDays = now.plusDays(7);
        FilteredList<Appointment> weeklyFilteredAppts = new FilteredList<>(populateApptsList());
        // multi-line lambda for datetime format and to return date values
        weeklyFilteredAppts.setPredicate(date -> {
            LocalDate apptDate = LocalDate.parse(date.getStartDate(), dateFormat);
            return apptDate.isAfter(now.minusDays(1)) && apptDate.isBefore(nowPlusDays);
        });
        scheduleTableView.setItems(weeklyFilteredAppts);
    }
    
    @FXML private void newAppt() throws SQLException {
        PreparedStatement statement = null;
        String insert = "INSERT INTO appointment (customerId, title, description, type, location, contact, "
                + "url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy, userId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)";
        String errorMsg = validateData();
        if (errorMsg.equals("None")) {
            try {
                // building & formatting timestamp to insert into SQL
                LocalDate date = apptDatePicker.getValue();
                LocalTime tmpStart = LocalTime.parse(startTimeComboBox.getValue(), timeFormat);
                LocalTime tmpEnd = LocalTime.parse(endTimeComboBox.getValue(), timeFormat);
                LocalDateTime startDate = LocalDateTime.of(date, tmpStart);
                LocalDateTime endDate = LocalDateTime.of(date, tmpEnd);
                ZonedDateTime startTZone = startDate.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
                ZonedDateTime endTZone = endDate.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
                Timestamp startDateInsert = Timestamp.valueOf(startTZone.toLocalDateTime());
                Timestamp endDateInsert = Timestamp.valueOf(endTZone.toLocalDateTime());

                statement = LoginScreenController.dbConnect.prepareStatement(insert);
                statement.setInt(1, customerComboBox.getValue().getCustomerId());
                statement.setString(2, titleTextField.getText());
                statement.setString(3, descripTextArea.getText());
                statement.setString(4, typeComboBox.getValue());
                statement.setString(5, "");
                statement.setString(6, "");
                statement.setString(7, "");
                statement.setTimestamp(8, startDateInsert);
                statement.setTimestamp(9, endDateInsert);
                statement.setString(10, LoginScreenController.getUser().getUserName());
                statement.setString(11, LoginScreenController.getUser().getUserName());
                statement.setInt(12, LoginScreenController.getUser().getUserId());
                int result = statement.executeUpdate();
                if (result == 1) {
                    System.out.println("New appointment added.");
                } else {
                    System.out.println("Appointment was NOT added");
                }
            } catch (SQLException ex) {
                Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
            clearFields();
            initializeTableView();
        } else {
            generateError(errorMsg);
        }
    }
    
    // list for available times of appts
    private void buildApptTimeValues() {
        LocalTime opHours = LocalTime.of(8, 0);
        ObservableList<String> startTimeList = FXCollections.observableArrayList();
        ObservableList<String> endTimeList = FXCollections.observableArrayList();
        do {
            startTimeList.add(opHours.format(timeFormat));
            endTimeList.add(opHours.format(timeFormat));
            opHours = opHours.plusMinutes(15);
        } while (!opHours.equals(LocalTime.of(17, 15)));
        startTimeList.remove(startTimeList.size() - 1);
        endTimeList.remove(0);
        
        apptDatePicker.setValue(LocalDate.now());
        startTimeComboBox.setItems(startTimeList);
        endTimeComboBox.setItems(endTimeList);
    }
    
    @FXML private void modAppt(ActionEvent event) throws IOException {
        transitionAppt = null;
        transitionAppt = scheduleTableView.getSelectionModel().getSelectedItem();
        // error if no object assigned
        if (transitionAppt == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("You have not selected an appointment to modify.");
            alert.showAndWait();
        } else {
            loadScene(event, "/View/ModAppointment.fxml");
        }
    }
        
    @FXML private void exit(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Exit Appointment Scheduler?");
        alert.setContentText("Would you like to leave the appointment scheduler screen?");
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            loadScene(event, "/View/MainScreen.fxml");
        }
    }
    
    // logged in user appts
    private ObservableList<Appointment> populateApptsList() throws SQLException {
        PreparedStatement statement = null;
        ObservableList<Appointment> apptsList = FXCollections.observableArrayList();
        String query = "SELECT appointment.appointmentId, appointment.customerId, appointment.title, appointment.type, appointment.description, "
                + "appointment.start, appointment.end, customer.customerId, customer.customerName, appointment.createdBy, appointment.userId "
                + "FROM appointment, customer "
                + "WHERE appointment.customerId = customer.customerId "
                + "ORDER BY appointment.start";
        try {
            statement = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                int apptId = results.getInt("appointment.appointmentId");
                Timestamp timeApptStart = results.getTimestamp("appointment.start");
                Timestamp timeApptEnd = results.getTimestamp("appointment.end");
                String apptTitle = results.getString("appointment.title");
                String apptType = results.getString("appointment.type");
                String apptUser = results.getString("appointment.createdBy");
                String apptDescrip = results.getString("appointment.description");
                Customer apptCustomer = new Customer(results.getInt("appointment.customerId"), results.getString("customer.customerName"));
                int apptUserId = results.getInt("appointment.userId");
                // time & date to readable format
                ZonedDateTime zoneStart = timeApptStart.toLocalDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime apptStart = zoneStart.withZoneSameInstant(zoneId);

                ZonedDateTime zoneEnd = timeApptEnd.toLocalDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime apptEnd = zoneEnd.withZoneSameInstant(zoneId);
                // add appointments to list
                Appointment appt = new Appointment(apptId, apptStart.format(dateFormat), apptEnd.format(dateFormat), apptTitle, apptType, apptDescrip, apptCustomer, apptUser, apptUserId);
                apptsList.add(appt);
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return apptsList;
    }
    
    // populate Customer combobox
    private void populateCustomers() throws SQLException {
        PreparedStatement statement = null;
        String query = "SELECT customer.customerId, customer.customerName FROM customer "
                + "ORDER BY customer.customerId";
        try {
            statement = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = statement.executeQuery();
            ObservableList<Customer> customerList = FXCollections.observableArrayList();
            while (results.next()) {
                int custId = results.getInt("customer.customerId");
                String custName = results.getString("customer.customerName");
                System.out.println("Customer Name: " + custName + " Customer ID: " + custId);
                
                Customer tmpCustomer = new Customer(custId, custName);
                customerList.add(tmpCustomer);
            }
            customerComboBox.setItems(customerList);
        } catch (SQLException ex) {
            System.out.println("Error retrieving customers: " + ex.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
    
    private String validateData() throws SQLException {
        String error;
        
        LocalDate date = apptDatePicker.getValue();
        LocalTime start = LocalTime.parse(startTimeComboBox.getValue(), timeFormat);
        LocalTime end = LocalTime.parse(endTimeComboBox.getValue(), timeFormat);
        LocalDateTime startLDT = LocalDateTime.of(date, start);
        LocalDateTime endLDT = LocalDateTime.of(date, end);
        ZonedDateTime startTime = startLDT.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endTime = endLDT.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
        
        if (apptDatePicker.getValue() == null) {
            error = "Start Date field cannot be empty.";
        } else if (startTimeComboBox.getValue() == null) {
            error = "Appointment must have a start time.";
        } else if (endTimeComboBox.getValue() == null) {
            error = "Appointment must have an end time.";
        } else if (titleTextField.getText().equals("")) {
            error = "Title field cannot be empty.";
        } else if (typeComboBox.getValue() == null) {
            error = "You must choose an appointment type.";
        } else if (customerComboBox.getValue() == null){
            error = "You must choose a customer for this appointment. If there are none "
                    + "to choose from, please create a customer by returning to the main menu "
                    + "and selecting the \"Manage Customers\" button.";
        // sqlexception can occur with checkConflict method
        } else if (checkConflict(startTime, endTime)) {
            error = "Appointment scheduling conflict. Overlapping appointment times with existing appointments.";
        } else if (startTime.equals(endTime)) {
            error = "Appointment start and end times cannot be the same time.";
        } else if (endTime.isBefore(startTime)) {
            error = "Appointment end time cannot be scheduled before the appointment start time.";
        } else if (descripTextArea.getText().equals("")) {
            error = "Please enter a description for this appointment.";
        } else {
            error = "None";
        }
        return error;
    }
    
    // check for scheduling conflict
    private boolean checkConflict(ZonedDateTime start, ZonedDateTime end) {
        String userName = LoginScreenController.getUser().getUserName();
        PreparedStatement stm = null;
        String query = "SELECT * FROM appointment "
                + "WHERE ? BETWEEN start AND end "
                + "OR ? BETWEEN start AND end "
                + "OR ? < start AND ? > end "
                + "AND createdBy = ?";
        try {
            stm = LoginScreenController.dbConnect.prepareStatement(query);
            stm.setTimestamp(1, Timestamp.valueOf(start.toLocalDateTime()));
            stm.setTimestamp(2, Timestamp.valueOf(end.toLocalDateTime()));
            stm.setTimestamp(3, Timestamp.valueOf(start.toLocalDateTime()));
            stm.setTimestamp(4, Timestamp.valueOf(end.toLocalDateTime()));
            stm.setString(5, userName);
            ResultSet results = stm.executeQuery();
            if (results.next()) {
                System.out.println("Schedule overlap found");
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("Appointment overlap check failed: " + ex.getMessage());
        } 
        return false;
    }
    
    private void clearFields() {
        apptDatePicker.setValue(LocalDate.now());
        startTimeComboBox.setValue("");
        endTimeComboBox.setValue("");
        titleTextField.setText("");
        typeComboBox.setValue("");
        customerComboBox.setValue(null);
        descripTextArea.setText("");
    }
    
    private void generateError(String errorMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error Submitting Appointment");
        alert.setContentText("Please correct the following error: " + errorMessage);
        alert.showAndWait();
    }
    
    private void loadScene(ActionEvent event, String path) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(path));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        descripCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // override toString and fromString method for customer combobox 
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer object) {
                return object.getCustomerName();
            }
            
            @Override
            public Customer fromString(String string) {
                // lambda to code the override string method
                return customerComboBox.getItems().stream().filter(value -> 
                value.getCustomerName().equals(string)).findFirst().orElse(null);
            }
        });
        
        weeklyRadioBtn.setSelected(true);
        try {
            populateCustomers();
        } catch (SQLException ex) {
            Logger.getLogger(ScheduleScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        setTypeList();
        buildApptTimeValues();
        initializeTableView();
    }    
}
