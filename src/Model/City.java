package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class City {
    
    private SimpleIntegerProperty cityId;
    private SimpleStringProperty cityName;
    private SimpleIntegerProperty countryId;
    
    // main city constructor
    public City(int cityId, String cityName, int countryId) {
        this.cityId = new SimpleIntegerProperty(cityId);
        this.cityName = new SimpleStringProperty(cityName);
        this.countryId = new SimpleIntegerProperty(countryId);
    }
    
    // emtpy constructor
    public City() {
        
    }
    
    // getters & setters
    public int getCityId() {
        return cityId.get();
    }

    public void setCityId(int cityId) {
        this.cityId.set(cityId);
    }

    public String getCityName() {
        return cityName.get();
    }

    public void setCityName(String cityName) {
        this.cityName.set(cityName);
    }

    public int getCountryId() {
        return countryId.get();
    }

    public void setCountryId(int countryId) {
        this.countryId.set(countryId);
    }
}
