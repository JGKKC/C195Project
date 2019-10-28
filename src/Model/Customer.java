package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Customer {
    
    private SimpleIntegerProperty customerId;
    private SimpleStringProperty customerName;
    private Address address;
    private SimpleStringProperty countryName;
    
    // main customer constructor
    public Customer(int customerId, String customerName, Address address, String country) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.address = address;
        this.countryName = new SimpleStringProperty(country);
    }
    
    // temp customer constructor
    public Customer(String customerName, Address address) {
        this.customerName = new SimpleStringProperty(customerName);
        this.address = address;
    }
    
    // partial customer constructor
    public Customer(int customerId, String customerName) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
    }
    
    // empty constructor
    public Customer() {
        
    }

    // getters & setters
    public int getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public String getCustomerName() {
        return customerName.get();
    }

    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
    
    public String getCityName() {
        return address.getCityName();
    }
    
    public void setCityName(String cityName) {
        this.address.setCityName(cityName);
    }

    public String getCountryName() {
        return countryName.get();
    }

    public void setCountryName(String countryName) {
        this.countryName.set(countryName);
    }
    
    public String getAddressName() {
        return this.address.getAddress();
    }
    
    public String getAddressPhone() {
        return this.address.getPhoneNumber();
    }
    
    public String getAddressZipCode() {
        return this.address.getZipCode();
    }
    
    public static boolean validateInput(String input, String filter){
        for(int i = 0; i<input.length(); i++){
            if(filter.indexOf(input.charAt(i))== -1){
                return false;
            }
        }
        return true;
    }
}
