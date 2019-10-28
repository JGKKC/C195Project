package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Country {
    
    private SimpleIntegerProperty countryId;
    private SimpleStringProperty countryName;
    
    // main country constructor
    public Country(int countryId, String countryName) {
        this.countryId = new SimpleIntegerProperty(countryId);
        this.countryName = new SimpleStringProperty(countryName);
    }
    
    // empty constructor
    public Country() {
        
    }
    
    // getters & setters
    public int getCountryId() {
        return countryId.get();
    }

    public void setCountryId(int countryId) {
        this.countryId.set(countryId);
    }

    public String getCountryName() {
        return countryName.get();
    }

    public void setCountryName(String countryName) {
        this.countryName.set(countryName);
    }
}
