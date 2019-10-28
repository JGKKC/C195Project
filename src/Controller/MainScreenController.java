package Controller;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Model.*;

public class MainScreenController implements Initializable {
        
     private static Users mainUser = LoginScreenController.getUser();
    
    
    @FXML private void manageUsers(ActionEvent event) throws IOException {
        loadScene(event, "/View/UserList.fxml");
    }
    
    @FXML private void manageAppt(ActionEvent event) throws IOException {
        loadScene(event, "/View/ScheduleScreen.fxml");
    }
    
    @FXML private void manageCustomers(ActionEvent event) throws IOException {
        loadScene(event, "/View/CustomerList.fxml");
    }
    
    @FXML private void apptReports(ActionEvent event) throws IOException {
        loadScene(event, "/View/ReportsScreen.fxml");
    }
    
    @FXML private void logOff(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Off");
        alert.setHeaderText("Log Off " + mainUser.getUserName());
        alert.setContentText("Do you want to log off the current user?");
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.get() == ButtonType.OK) {
            loadScene(event, "/View/LoginScreen.fxml");
        }
    }
    
    private void loadScene(ActionEvent event, String path) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(path));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML private void exitProgram() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Exit Program?");
        alert.setContentText("Exit the program?");
        alert.initModality(Modality.WINDOW_MODAL);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // initialize alert button
        // apply username to welcome message
        //userTextLabel.setText(mainUser.getUserName());
    }
}
