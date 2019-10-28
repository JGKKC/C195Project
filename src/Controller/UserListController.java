package Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Model.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;

public class UserListController implements Initializable {

    @FXML private TableView<Users> userTable;
    @FXML private TableColumn<Users, String> userNameCol;
    @FXML private TableColumn<Users, Integer> activeCol;
    
    @FXML private TextField userSearchTextField;
    @FXML private TextField userNameTextField;
    @FXML private TextField pWordTextField;
    @FXML private TextField confirmPWordTextField;
    
    // temporary list for existing users
    private static ObservableList<Users> userList = FXCollections.observableArrayList();
    private ObservableList<Users> tempUserList = FXCollections.observableArrayList();
    private static FilteredList<Users> filteredUsers;
    private static int userID;
    
    // search for specific user
    @FXML private void searchUser() {
        boolean userFound = false;
        String searchText = userSearchTextField.getText();
        for (int i = 0; i < userList.size(); ++i) {
            String temp = userList.get(i).getUserName();
            if (temp.equals(searchText)) {
                userFound = true;
                tempUserList.setAll(userList.get(i));
                this.userTable.setItems(tempUserList);
            }
        }
        if (userFound == false) {
            initializeUserTable();
        }
    }
    
    @FXML private void addUser(ActionEvent event) throws IOException, SQLException {
        // confirmation before updating any values
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save User?");
        alert.setContentText("Are you sure you want to create this user? Click OK to have your changes "
                + "processed and return to the main User List screen.");
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            if (validateData().equals("OK")) {                
                PreparedStatement statement = null;
                String query = "INSERT INTO user (user.userId, user.userName, user.password, user.active, "
                        + "user.createDate, user.createBy, user.lastUpdate, user.lastUpdatedBy) "
                        + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
                try {
                    Users blankUser = new Users();
                    statement = LoginScreenController.dbConnect.prepareStatement(query);
                    statement.setInt(1, userID);
                    statement.setString(2, userNameTextField.getText());
                    statement.setString(3, pWordTextField.getText());
                    statement.setInt(4, 1);
                    statement.setString(5, LoginScreenController.getUser().getUserName());
                    statement.setString(6, LoginScreenController.getUser().getUserName());
                    statement.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
                loadScene(event, "/View/UserList.fxml");
            // error alerts
            } else if (validateData().equals("empty user")) {
                loadError("Empty User Name", "User name cannot be empty.");
            } else if (validateData().equals("long user")) {
                loadError("Invalid User Name", "User name cannot be over 12 characters.");
            } else if (validateData().equals("empty password")) {
                loadError("Empty Password", "The password cannot be empty.");
            } else if (validateData().equals("password mismatch")) {
                loadError("Password Mismatch", "The password and confirmation password do not match.\n"
                        + "Please try again.");
            } else if (validateData().equals("long password")) {
                loadError("Invalid Password", "The password cannot be over 14 characters.");
            } else {
                loadError("Unknown Error", "An unknown error occurred. Changes will not be processed and the program "
                        + "will return to the main User List screen.");
                loadScene(event, "/View/Userlist.fxml");
            }
        }
    }
     
    // validate form fields
    private String validateData() {
        String errorMsg;
        String uNameCheck = userNameTextField.getText();
        String pWordCheck = pWordTextField.getText();
        String confirmPWord = confirmPWordTextField.getText();
        if (uNameCheck.equals("")) {
            errorMsg = "empty user";
        } else if (uNameCheck.length() > 12) {
            errorMsg = "long user";
        } else if (pWordCheck.equals("")) {
            errorMsg = "empty password";
        } else if (pWordCheck.length() > 14) {
            errorMsg = "long password";
        } else if (pWordCheck.equals(confirmPWord) == false) {
            errorMsg = "password mismatch";
        } else {
            errorMsg = "OK";
        }
        return errorMsg;
    }
    
    private void loadError(String title, String content) {
        Alert newAlert = new Alert(Alert.AlertType.ERROR);
        newAlert.setTitle(title);
        newAlert.setContentText(content);
        newAlert.initModality(Modality.APPLICATION_MODAL);
        newAlert.showAndWait();
    }
    
    // return to main screen
    @FXML private void exitScreen(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Return?");
        alert.setContentText("Are you sure you want to return to the main screen?");
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            loadScene(event, "/View/MainScreen.fxml");
        }
    }

    // populate user list while leaving out test account
    private void populateTempUserList() throws SQLException {
        PreparedStatement statement = null;
        String query = "SELECT user.userId, user.userName, user.active, user.password FROM user "
                + "ORDER BY user.userName";
        try {
            statement = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = statement.executeQuery();

            if (results.next() == false) {
                System.out.println("No users found.");
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("User List Empty");
                alert.setContentText("No users were found. Please create a new user.");
                alert.showAndWait();
            } else {
                while (results.next()) {
                    int userId = results.getInt("user.userId");
                    int active = results.getInt("user.active");
                    String userName = results.getString("user.userName");
                    String password = results.getString("user.password");
                    Users tmpUser = new Users(userName, password, userId, active);
                    // add user to User List if does not exist
                    // logic in place to prevent duplicate users from displaying in list
                    boolean userFound = false;
                    for (int i = 0; i < userList.size(); ++i) {
                        String checkName = userList.get(i).getUserName();
                        if (checkName.equals(tmpUser.getUserName())) {
                            userFound = true;
                        }
                    }
                    if (userFound == false) {
                        userList.add(tmpUser);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
    
    private void initializeUserTable() {
        filteredUsers = new FilteredList<>(userList, p -> true);
        filteredUsers.setPredicate(user -> {
            return (user.getUserName().isEmpty() == false);
        });
        userTable.setItems(filteredUsers);
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
        
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        try {
            populateTempUserList();
        } catch (SQLException ex) {
            Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // set generated user ID initial value
        try {
            PreparedStatement stm = LoginScreenController.dbConnect.prepareStatement("SELECT MAX(userId) AS maxId FROM user");
            ResultSet results = stm.executeQuery();
            if (results.next() == false) {
                userID = 0;
            } else {
                userID = results.getInt("maxId") + 1;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(CustomerListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Current generated User ID: " + userID);
        
        initializeUserTable();
    }    
}
