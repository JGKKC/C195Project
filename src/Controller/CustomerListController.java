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
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class CustomerListController implements Initializable {

    @FXML private TableView<Customer> customerTableView;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> phoneCol;
    @FXML private TableColumn<Address, String> addressCol;
    @FXML private TableColumn<Address, String> zipCodeCol; 
    @FXML private TableColumn<Customer, String> countryCol;
    @FXML private TableColumn<Customer, String> cityCol;
    @FXML private TextField nameTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField streetTextField;
    @FXML private TextField zipTextField;
    @FXML private ComboBox<Country> countryComboBox;
    @FXML private ComboBox<City> cityComboBox;
    
    private static ObservableList<City> masterCityList = FXCollections.observableArrayList();
    private static Customer transitionCustomer;
    

    
    @FXML private void newCustomer() throws SQLException {
        String errorMsg = validateCustomer();
        if (errorMsg.equals("None")) {
            City cityObj = new City(cityComboBox.getValue().getCityId(), cityComboBox.getValue().getCityName(), cityComboBox.getValue().getCountryId());
            Address addressObj = new Address(streetTextField.getText(), cityObj, zipTextField.getText(), phoneTextField.getText());
            PreparedStatement addressStatement = null;
            Customer customerObj = new Customer(nameTextField.getText(), addressObj);
            PreparedStatement custStatement = null;
            String insertAddress = "INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, "
                    + "createdBy, lastUpdate, lastUpdateBy) "
                    + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
            
            String insertCustomer = "INSERT INTO customer (customerName, active, addressId, createDate, "
                    + "createdBy, lastUpdate, lastUpdateBy) "
                    + "SELECT ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?";
            try {
                addressStatement = LoginScreenController.dbConnect.prepareStatement(insertAddress, Statement.RETURN_GENERATED_KEYS);
                addressStatement.setString(1, addressObj.getAddress());
                addressStatement.setString(2, "");
                addressStatement.setInt(3, addressObj.getCity().getCityId());
                addressStatement.setString(4, addressObj.getZipCode());
                addressStatement.setString(5, addressObj.getPhoneNumber());
                addressStatement.setString(6, LoginScreenController.getUser().getUserName());
                addressStatement.setString(7, LoginScreenController.getUser().getUserName());
                addressStatement.execute();
                ResultSet gkResults = addressStatement.getGeneratedKeys();
                int addressId = 0;
                if(gkResults.next()) {
                    addressId = gkResults.getInt(1);
                    System.out.println("Generated Address ID: " + addressId);
                }
                
                custStatement = LoginScreenController.dbConnect.prepareStatement(insertCustomer);
                custStatement.setString(1, customerObj.getCustomerName());
                custStatement.setInt(2, 1);
                custStatement.setInt(3, addressId);
                custStatement.setString(4, LoginScreenController.getUser().getUserName());
                custStatement.setString(5, LoginScreenController.getUser().getUserName());
                custStatement.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (custStatement != null) {
                    custStatement.close();
                }
                if (addressStatement != null ) {
                    addressStatement.close();
                }
            }
            initializeTable();
        } else {
            generateError(errorMsg);
        }
    }
    
    @FXML private void removeCustomer() throws SQLException {
        // no customer selected error
        if(customerTableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error.");
            alert.setContentText("You have not selected a customer to delete.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setContentText("Please confirm deletion of customer: " + customerTableView.getSelectionModel().getSelectedItem().getCustomerName());
            alert.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Customer tempCustomer = customerTableView.getSelectionModel().getSelectedItem();
                Address tempAddress = tempCustomer.getAddress();
                PreparedStatement removeCust = null;
                String apRemove = "DELETE FROM appointment "
                        + "WHERE customerId = ?";
                String cRemove = "DELETE FROM customer "
                        + "WHERE customerId = ?";
                String aRemove = "DELETE FROM address "
                        + "WHERE addressId = ?";
                try {
                    removeCust = LoginScreenController.dbConnect.prepareStatement(apRemove);
                    removeCust.setInt(1, tempCustomer.getCustomerId());
                    removeCust.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Error while deleting related appointments from appointment table: " + ex.getMessage());
                } finally {
                    if (removeCust != null) {
                        removeCust.close();
                    }
                }
                try {
                    removeCust = LoginScreenController.dbConnect.prepareStatement(cRemove);
                    removeCust.setInt(1, tempCustomer.getCustomerId());
                    removeCust.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Error while deleting customer from customer table: " + ex.getMessage());
                } finally {
                    if (removeCust != null) {
                        removeCust.close();
                    }
                }
                try {
                    removeCust = LoginScreenController.dbConnect.prepareStatement(aRemove);
                    removeCust.setInt(1, tempAddress.getAddressId());
                    removeCust.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Error while deleting address from address table: " + ex.getMessage());
                } finally {
                    if (removeCust != null) {
                        removeCust.close();
                    }
                }
                initializeTable();
            } else {
                System.out.println("Delete cancelled");
            }
        }
    }
    
    @FXML
    private void modCustomer(ActionEvent event) throws IOException {
        // catch error if no customer selected
        if(customerTableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error.");
            alert.setContentText("No customer selected.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        } else {
            // push selected item to transferrable customer object
            transitionCustomer = customerTableView.getSelectionModel().getSelectedItem();
            Parent root = FXMLLoader.load(getClass().getResource("/View/ModCustomer.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
    
    public static Customer getTransitionCustomer() {
        return transitionCustomer;
    }
    
    public static void setTransitionCustomer(Customer customer) {
        transitionCustomer = customer;
    }
    
    // enable city combobox after country selection
    @FXML private void enableCityBox() {
        // populate city list with relevant cities
        ObservableList<City> tempCityList = FXCollections.observableArrayList();
        cityComboBox.getItems().removeAll(cityComboBox.getItems());
        // create temporary country object
        Country tempCountry = countryComboBox.getValue();
        if(tempCountry == null) {
            if (cityComboBox.disableProperty().getValue() == false) {
                cityComboBox.setDisable(true);
            }
        // populate city combobox with relevant cities
        } else if (tempCountry.getCountryId() == 1) {
            cityComboBox.setDisable(false);
             for (City city : masterCityList) {
                City tempCity = city;
                if (tempCity.getCountryId() == 1) {
                    tempCityList.add(tempCity);
                }
            }
            cityComboBox.setItems(tempCityList);
        // populate city combobox with relevant cities   
        } else if (tempCountry.getCountryId() == 3) {
            cityComboBox.setDisable(false);
            for (City city : masterCityList) {
                City tempCity = city;
                if (tempCity.getCountryId() == 3) {
                    tempCityList.add(tempCity);
                }
            }
            cityComboBox.setItems(tempCityList);
        // unknown or syntax error catch
        } else {
            System.out.println("Something went wrong.");
        }
    }
    
    
    // validating customer data
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
    
    private void populateCountryList() throws SQLException {
        countryComboBox.getItems().removeAll(countryComboBox.getItems());
        ObservableList<Country> countryList = FXCollections.observableArrayList();
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
            System.out.println("Failed to query Country table: " + ex.getMessage());
        } finally {
            if (countryStm != null) {
                countryStm.close();
            }
        }
    }
    
    private void populateCityList() throws SQLException {
        masterCityList.clear();
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
    
    @FXML private void back(ActionEvent event) throws IOException {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Return?");
        alert.setContentText("Return to the main screen?");
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            Parent root = FXMLLoader.load(getClass().getResource("/View/MainScreen.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
    
    // error message for validation
    private void generateError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error Submitting Customer");
        alert.setContentText("Please correct error: " + errorMessage);
        alert.showAndWait();
    }
    
    private void initializeTable() throws SQLException {
        streetTextField.setText("");
        phoneTextField.setText("");
        nameTextField.setText("");
        zipTextField.setText("");
        countryComboBox.getSelectionModel().select(0);
        enableCityBox();
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        PreparedStatement getCustomers = null;
        String query = "SELECT customer.customerId, customer.customerName, address.addressId, address.address, "
                + "address.postalCode, address.phone, address.cityId, country.country, city.countryId, city.city FROM address, customer, country, city "
                + "WHERE address.addressId = customer.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId "
                + "ORDER BY customer.customerId";
        try {
            getCustomers = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = getCustomers.executeQuery();
            if (results.next() ==  false) {
                customerList.clear();
                customerTableView.setItems(customerList);
                System.out.println("No customers to populate table view.");
            } else {
                results.beforeFirst();
                customerList.clear();
                while(results.next()) {
                    // generate address for Customer object
                    int tmpAddressId = results.getInt("address.addressId");
                    String tmpStreetAddress = results.getString("address.address");
                    int tmpCityId = results.getInt("address.cityId");
                    String tmpCityName = results.getString("city.city");
                    int tmpCountryId = results.getInt("city.countryId");
                    String tmpZipCode = results.getString("address.postalCode");
                    String tmpPhone = results.getString("address.phone");
                    
                    City tmpCity = new City(tmpCityId, tmpCityName, tmpCountryId);
                    
                    Address tmpAddress = new Address(tmpAddressId, tmpStreetAddress, tmpCity, tmpZipCode, tmpPhone);
                    
                    // Customer object
                    int tmpCustId = results.getInt("customer.customerId");
                    String tmpCustName = results.getString("customer.customerName");
                    String tmpCountryName = results.getString("country.country");
                    
                    Customer tmpCustomer = new Customer(tmpCustId, tmpCustName, tmpAddress, tmpCountryName);
                    
                    customerList.add(tmpCustomer);
                }
                customerTableView.setItems(customerList);
            }
        } catch (SQLException ex) {
            System.out.println("SQLException while initializing table: " + ex.getMessage());
        } finally {
            if (getCustomers != null) {
                getCustomers.close();
            }
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("addressPhone"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("addressName"));
        zipCodeCol.setCellValueFactory(new PropertyValueFactory<>("addressZipCode"));
        countryCol.setCellValueFactory(new PropertyValueFactory<>("countryName"));
        cityCol.setCellValueFactory(new PropertyValueFactory<>("cityName"));
          
        // override toString and fromString method for comboboxes
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
        
        // override toString and fromString methods
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
                
        try {
            populateCityList();
            populateCountryList();
            initializeTable();
        } catch (SQLException ex) {
            Logger.getLogger(CustomerListController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
