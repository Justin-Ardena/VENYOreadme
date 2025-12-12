package iVenue;

import iVenue.config.MongoDb;
import iVenue.models.User;
import iVenue.models.Admin;
import iVenue.models.Customer;
import iVenue.models.VerifiedUser;
import iVenue.repositories.UserStore;
import iVenue.ui.AdminDashboard;
import iVenue.ui.CustomerDashboard;
import iVenue.ui.CustomersPanel;

import com.mongodb.client.MongoDatabase;
import iVenue.ui.RegistrationsPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private MongoDatabase db;

    @Override
    public void start(Stage stage) {
        // Connect to MongoDB
        System.out.println("Connecting to MongoDB...");
        db = MongoDb.getDatabase();
        System.out.println("MongoDB connected successfully.\n");
        UserStore.ensureAdminExists();

        Label title = new Label("iVenue BOOKING SYSTEM");
        title.setFont(new Font("Arial", 32));
        title.setTextFill(Color.BLACK);

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register Customer");

        loginBtn.setStyle("-fx-background-color: #2C2C2C; -fx-text-fill: white; -fx-background-radius: 20px; -fx-font-size: 16px;");
        registerBtn.setStyle("-fx-background-color: #2C2C2C; -fx-text-fill: white; -fx-background-radius: 20px; -fx-font-size: 16px;");

        loginBtn.setPrefSize(150, 50);
        registerBtn.setPrefSize(180, 50);

        HBox buttonRow = new HBox(30, loginBtn, registerBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(20, title, buttonRow);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setBackground(new Background(new BackgroundFill(Color.web("#F8F8F8"), new CornerRadii(20), null)));

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #D3D3D3; -fx-padding: 40px;");

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("iVenue Booking System");
        stage.show();
        stage.setMaximized(true);

        // LOGIN BUTTON: choose Admin or Customer
        loginBtn.setOnAction(e -> showLoginChoice(stage));

        // REGISTER BUTTON: open customer registration UI
        registerBtn.setOnAction(e -> RegistrationsPanel.openRegistration(stage));
    }

    private void showLoginChoice(Stage stage) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Customer", "Admin", "Customer");
        dialog.setTitle("Login Type");
        dialog.setHeaderText("Choose login type:");
        dialog.setContentText("Type:");

        dialog.showAndWait().ifPresent(type -> {
            VerifiedUser auth = new VerifiedUser();
            User user;
            try {
                user = auth.loginGui(stage); // GUI login dialog
            } catch (UnsupportedOperationException ex) {
                Alert fallback = new Alert(Alert.AlertType.INFORMATION, "GUI login not implemented yet.");
                fallback.initOwner(stage);
                fallback.showAndWait();
                return;
            }

            if (user == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid credentials.");
                alert.initOwner(stage);
                alert.showAndWait();
                return;
            }

            Stage mainStage = stage; // reference to main login stage

            if ("Admin".equalsIgnoreCase(type)) {
                if (user instanceof Admin) {
                    AdminDashboard.display((Admin) user, mainStage);
                    mainStage.hide();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You are not an admin.");
                    alert.initOwner(stage);
                    alert.showAndWait();
                }
            } else { // Customer
                if (user instanceof Customer) {
                    CustomerDashboard.display((Customer) user, mainStage);
                    mainStage.hide();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You are not a customer.");
                    alert.initOwner(stage);
                    alert.showAndWait();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
