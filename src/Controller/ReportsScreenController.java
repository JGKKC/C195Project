package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReportsScreenController implements Initializable {
    
    @FXML private BarChart monthlyBarChart;
    @FXML private BarChart consultBarChart;
    @FXML private BarChart customerBarChart;
    
    @FXML private void populateMonthlyReport() throws SQLException {
        ObservableList<XYChart.Data<String, Integer>> monthlyData = FXCollections.observableArrayList();
        XYChart.Series<String, Integer> monthlySeries = new XYChart.Series<>();
        PreparedStatement stm = null;
        String query = "SELECT MONTHNAME(start) AS MONTH, COUNT(appointmentId) as TOTAL "
                + "FROM appointment "
                + "GROUP BY MONTH";
        try {
            stm = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = stm.executeQuery();
            while (results.next()) {
                String month = results.getString("MONTH");
                Integer total = results.getInt("TOTAL");
                monthlyData.add(new Data<>(month, total));
            }
        } catch (SQLException ex) {
            System.out.println("Error populating Monthly Reports: " + ex.getMessage());
        } finally {
            if (stm != null) {
                stm.close();
            }
        }
        monthlySeries.getData().addAll(monthlyData);
        monthlyBarChart.getData().add(monthlySeries);
    }
    
    @FXML private void populateConsultantList() throws SQLException {
        ObservableList<XYChart.Data<String, Integer>> consultData = FXCollections.observableArrayList();
        XYChart.Series<String, Integer> consultSeries = new XYChart.Series<>();
        PreparedStatement stm = null;
        String query = "SELECT createdBy AS CONSULTANT, COUNT(appointmentId) as TOTAL "
                + "FROM appointment "
                + "GROUP BY CONSULTANT";
        try {
            stm = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = stm.executeQuery();
            while (results.next()) {
                String consultant = results.getString("CONSULTANT");
                Integer total = results.getInt("TOTAL");
                consultData.add(new Data<>(consultant, total));
            }
        } catch (SQLException ex) {
            System.out.println("Error getting Consultant Reports: " + ex.getMessage());
        } finally {
            if (stm != null) {
                stm.close();
            }
        }
        consultSeries.getData().addAll(consultData);
        consultBarChart.getData().add(consultSeries);
    }
    
    @FXML private void populateCustomerList() throws SQLException {
        ObservableList<XYChart.Data<String, Integer>> customerData = FXCollections.observableArrayList();
        XYChart.Series<String, Integer> customerSeries = new XYChart.Series<>();
        PreparedStatement stm = null;
        String query = "SELECT COUNT(appointment.appointmentId) AS TOTAL, customer.customerName AS CUSTOMER FROM appointment INNER JOIN customer ON appointment.customerId = customer.customerId GROUP BY CUSTOMER";       
        try {
            stm = LoginScreenController.dbConnect.prepareStatement(query);
            ResultSet results = stm.executeQuery();
            while (results.next()) {
                String customer = results.getString("CUSTOMER");
                Integer total = results.getInt("TOTAL");
                customerData.add(new Data<>(customer, total));
            }
        } catch (SQLException ex) {
            System.out.println("Error populating Customer Reports: " + ex.getMessage());
        } finally {
            if (stm != null) {
                stm.close();
            }
        }
        customerSeries.getData().addAll(customerData);
        customerBarChart.getData().add(customerSeries);
    }
    
    @FXML private void exit(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Exit Report Screen?");
        alert.setContentText("Would you like to leave the Reports screen?");
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
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            populateMonthlyReport();
            populateConsultantList();
            populateCustomerList();
        } catch (SQLException ex) {
            Logger.getLogger(ReportsScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
