package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import Model.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ModCustomerController implements Initializable {

    @FXML TextField nameTextField;
    @FXML TextField phoneTextField;
    @FXML TextField streetTextField;
    @FXML TextField zipTextField;
    
    @FXML ComboBox<Country> countryComboBox;
    @FXML ComboBox<City> cityComboBox;
    
    private static Customer tempCustomer;
    private static ObservableList<City> masterCityList = FXCollections.observableArrayList();
    private static ObservableList<Country> countryList = FXCollections.observableArrayList();
    private static ObservableList<City> tempCityList = FXCollections.observableArrayList();
    
    @FXML
    private void save(ActionEvent event) throws IOException, SQLException {
        // validation check
        String errorMsg = validateCustomer();
        // prompt for confirmation
        if (errorMsg.equals("None")) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Changes");
            alert.setHeaderText("Please Confirm Changes.");
            alert.setContentText("Please confirm your changes to the customer profile.\n"
                    + "You will return to the previous screen after changes are saved.");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                // update customer table with new values
                PreparedStatement custStm = null;
                String custUpdate = "UPDATE customer "
                        + "SET customerName = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ?"
                        + "WHERE customerId = ?";
                try {
                    custStm = LoginScreenController.dbConnect.prepareStatement(custUpdate);
                    custStm.setString(1, nameTextField.getText());
                    custStm.setString(2, LoginScreenController.getUser().getUserName());
                    custStm.setInt(3, tempCustomer.getCustomerId());
                    int sqlresult = custStm.executeUpdate();
                    if (sqlresult == 1) {
                        System.out.println("Customer update successfull.");
                    } else {
                        System.out.println("Customer update not successful.");
                    }
                } catch (SQLException ex) {
                    System.out.println("Customer table update failed: " + ex.getMessage());
                } finally {
                    if (custStm != null) {
                        custStm.close();
                    }
                }
                // update address table with new values
                PreparedStatement addrStm = null;
                String addrUpdate = "UPDATE address "
                        + "SET address = ?, postalCode = ?, phone = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ? "
                        + "WHERE addressId = ?";
                try {
                    addrStm = LoginScreenController.dbConnect.prepareStatement(addrUpdate);
                    addrStm.setString(1, streetTextField.getText());
                    addrStm.setString(2, zipTextField.getText());
                    addrStm.setString(3, phoneTextField.getText());
                    addrStm.setString(4, LoginScreenController.getUser().getUserName());
                    addrStm.setInt(5, tempCustomer.getAddress().getAddressId());
                } catch (SQLException ex) {
                    System.out.println("Address table update failed: " + ex.getMessage());
                } finally {
                    if (addrStm != null) {
                        addrStm.close();
                    }
                }
                // return to main customer management screen
                Parent root = FXMLLoader.load(getClass().getResource("/View/CustomerList.fxml"));
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
        } else {
            generateError(errorMsg);
        }
    }
    
    // method used to populate country list
    private void populateCountryList() throws SQLException {
        countryList.removeAll();
        PreparedStatement countryStm = null;
        String query = "SELECT countryId, country FROM country "
                + "ORDER BY countryId";
        try {
            countryStm = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = countryStm.executeQuery();
            while (results.next()) {
                int countryId = results.getInt("countryId");
                String countryName = results.getString("country");

                Country country = new Country(countryId, countryName);
                System.out.println("Country Name: " + countryName + " Country ID: " + countryId);
                countryList.add(country);
            }
            countryComboBox.setItems(countryList);
        } catch (SQLException ex) {
            System.out.println("Failed to query City table: " + ex.getMessage());
        } finally {
            if (countryStm != null) {
                countryStm.close();
            }
        }
    }
    
    // method used to populate city list
    private void populateCityList() throws SQLException {
        masterCityList.removeAll();
        PreparedStatement cityStm = null;
        String query = "SELECT city, cityId, city.countryId FROM city "
                + "ORDER BY cityId";
        try {
            cityStm = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = cityStm.executeQuery();
            while (results.next()) {
                String cityName = results.getString("city");
                int cityId = results.getInt("cityId");
                int countryId = results.getInt("city.countryId");

                City city = new City(cityId, cityName, countryId);
                System.out.println("City: " + cityName + " City ID: " + cityId + " Country ID: " + countryId);
                masterCityList.add(city);
            }
        } catch (SQLException ex) {
            System.out.println("Failed to query City table: " + ex.getMessage());
        } finally {
            if (cityStm != null) {
                cityStm.close();
            }
        }
    }
    
    @FXML
    private void enableCityBox() {
        // clear temp city list to populate it only with relevant cities
        tempCityList.clear();
        // create temporary country object from selection
        Country tempCountry = countryComboBox.getValue();
        // if selection is null/empty, reset disabled state on city combobox and do nothing
        if(tempCountry == null) {
            if (cityComboBox.disableProperty().getValue() == false) {
                cityComboBox.setDisable(true);
            }
        // if selection is id of 1 (United States), populate city combobox with relevant cities
        } else if (tempCountry.getCountryId() == 1) {
            cityComboBox.setDisable(false);
             for (int i = 0; i < masterCityList.size(); ++i) {
                City tempCity = masterCityList.get(i);
                if (tempCity.getCountryId() == 1) {
                    tempCityList.add(tempCity);
                }
            }
            cityComboBox.setItems(tempCityList);
        // if selection is id of 2 (England), populate city combobox with relevant cities   
        } else if (tempCountry.getCountryId() == 3) {
            cityComboBox.setDisable(false);
            for (int i = 0; i < masterCityList.size(); ++i) {
                City tempCity = masterCityList.get(i);
                if (tempCity.getCountryId() == 3) {
                    tempCityList.add(tempCity);
                }
            }
            cityComboBox.setItems(tempCityList);
        // unknown or syntax error catch
        } else {
            System.out.println("Something went wrong with the comboboxes. Check your code.");
        }
    }
    
    @FXML
    private void exit(ActionEvent event) throws IOException {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setContentText("Are you sure you want to exit without making any changes?");
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.get() == ButtonType.OK) {
            // return to main customer management screen
            Parent root = FXMLLoader.load(getClass().getResource("/View/CustomerList.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
    
    // validating customer data and generating error message
    private String validateCustomer() {
        String error;
        City validCity = cityComboBox.getValue();
        Country validCountry = countryComboBox.getValue();
        if (nameTextField.getText().equals("")) {
            error = "Customer name cannot be blank.";
        } else if (phoneTextField.getText().equals("")) {
            error = "Phone number cannot be blank.";
        } else if (phoneTextField.getText().length() > 8) {
            error = "Phone number is invalid. The correct format is: 555-1234.";
        } else if (streetTextField.getText().equals("")) {
            error = "Street address cannot be blank.";
        } else if (zipTextField.getText().equals("")) {
            error = "ZipCode cannot be empty.";
        } else if (validCity == null) {
            error = "A City must be chosen from the drop down box.";
        } else if (validCountry == null) {
            error = "A Country must be chosen from the drop down box.";
        } else if (!Customer.validateInput(phoneTextField.getText(), "0123456789-")){
            error = "Phone number can only contain numerics.";
        } else if (!Customer.validateInput(zipTextField.getText(), "0123456789-")){
            error = "Zip code can only contain numerics.";
        } else {
            error = "None";
        }
        return error;
    }
    
     // error message template for validation
    private void generateError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error Submitting Customer");
        alert.setContentText("Please correct the following error: " + errorMessage);
        alert.showAndWait();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tempCustomer = CustomerListController.getTransitionCustomer();
        try {
            populateCityList();
            populateCountryList();
        } catch (SQLException ex) {
            Logger.getLogger(ModCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        nameTextField.setText(tempCustomer.getCustomerName());
        phoneTextField.setText(tempCustomer.getAddressPhone());
        streetTextField.setText(tempCustomer.getAddressName());
        zipTextField.setText(tempCustomer.getAddressZipCode());
        
        // initialize country combobox with customer's predefined country
        for(Country country : countryList) {
            if (country.getCountryName().equals(tempCustomer.getCountryName())) {
                countryComboBox.getSelectionModel().select(country);
            }
        }
        
        // initialize city combobox with customer's predefined city
        for (City city : masterCityList) {
            if (tempCustomer.getCityName().equals(city.getCityName())) {
                cityComboBox.getSelectionModel().select(city);
            }  
        }
        
        // override toString and fromString method for comboboxes to clear visual error
        countryComboBox.setConverter(new StringConverter<Country>() {
            @Override
            public String toString(Country object) {
                return object.getCountryName();
            }
            
            @Override
            public Country fromString(String string) {
                // lambda utilized to efficiently code the override string method
                return countryComboBox.getItems().stream().filter(ap -> 
                ap.getCountryName().equals(string)).findFirst().orElse(null);
            }
        });
        
        // override toString and fromString method for comboboxes to clear visual error
        cityComboBox.setConverter(new StringConverter<City>() {
            @Override
            public String toString(City object) {
                return object.getCityName();
            }
            
            @Override
            public City fromString(String string) {
                // lambda utilized to efficiently code the override string method
                return cityComboBox.getItems().stream().filter(ap -> 
                ap.getCityName().equals(string)).findFirst().orElse(null);
            }
        });
    }    
}
