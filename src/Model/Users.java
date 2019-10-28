package Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Users {
    
    private SimpleStringProperty userName;
    private SimpleStringProperty password;
    private SimpleIntegerProperty userId;
    private SimpleIntegerProperty active;
       
    // main constructor
    public Users(String userName, String password, int userId, int active) {
        this.userName = new SimpleStringProperty(userName);
        this.password = new SimpleStringProperty(password);
        this.userId = new SimpleIntegerProperty(userId);
        this.active = new SimpleIntegerProperty(active);
    }
    
    // empty constructor
    public Users() {
        
    }

    public String getUserName() {
        return userName.get();
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public int getActive() {
        return active.get();
    }

    public void setActive(int active) {
        this.active.set(active);
    }    
}
