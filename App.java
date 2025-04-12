import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
 
public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("customer_view.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch(IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for better debugging
        }
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
