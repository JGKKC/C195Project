package C195Project;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Controller.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class C195project extends Application {
    
    // create the log file if it does not already exist.
    private static void createFile(String path) {
        try {
            File logFile = new File(path);
            if (!logFile.exists()) {
                logFile.createNewFile();
                System.out.println("Log file: " + "\"" + path + "\"" + " has been created.");
            } else {
                System.out.println("Log file: " + "\"" + path + "\"" + " already exists.");
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/View/LoginScreen.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        createFile("user_event_log.txt");
        launch(args);
    }
}
