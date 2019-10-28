package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Address {
    
    private SimpleIntegerProperty addressId;
    private SimpleStringProperty address;
    private City city;
    private SimpleStringProperty zipCode;
    private SimpleStringProperty phoneNumber;
    
    // main address constructor
    public Address(int addressId, String address, City city, String zipCode, String phoneNumber) {
        this.addressId = new SimpleIntegerProperty(addressId);
        this.address = new SimpleStringProperty(address);
        this.city = city;
        this.zipCode = new SimpleStringProperty(zipCode);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
    }
    // temp address constructor
    public Address(String address, City city, String zipCode, String phoneNumber) {
        this.address = new SimpleStringProperty(address);
        this.zipCode = new SimpleStringProperty(zipCode);
        this.city = city;
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
    }
    
    // emtpy constructor
    public Address() {
        
    }

    // getters & setters
    public int getAddressId() {
        return addressId.get();
    }

    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }
    
    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public City getCity() {
        return city;
    }
    
    public void setCity(City city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode.get();
    }

    public void setZipCode(String zipCode) {
        this.zipCode.set(zipCode);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }
    
    public String getCityName() {
        return city.getCityName();
    }
    
    public void setCityName(String cityName) {
        this.city.setCityName(cityName);
    }
}
