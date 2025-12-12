package iVenue.ui;

import iVenue.models.Admin;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Text;

public class AdminDashboard {

    public static void display(Admin admin, Stage loginStage) { // receive login stage
        Stage window = new Stage();
        window.setTitle("iVenue - Admin Management System");

        // LEFT PANEL (Navigation)
        VBox navBar = new VBox(10);
        navBar.setPadding(new Insets(20));
        navBar.setPrefWidth(250);
        navBar.setStyle("-fx-background-color: #2C3E50;");

        Button btnBookings = createNavButton("Manage Bookings");
        Button btnVenues = createNavButton("Manage Venues");
        Button btnAmenities = createNavButton("Manage Amenities");
        Button btnCustomers = createNavButton("Manage Customers");
        Button btnExit = createNavButton("Exit");

        navBar.getChildren().addAll(btnBookings, btnVenues, btnAmenities, btnCustomers, btnExit);

        // RIGHT PANEL (Dynamic Content)
        BorderPane contentArea = new BorderPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #ECF0F1;");
        contentArea.setCenter(new Text("Select an option from the left."));

        // EVENT HANDLERS (Swap the content area)
        btnBookings.setOnAction(e -> contentArea.setCenter(BookingsPanel.get(admin)));
        btnVenues.setOnAction(e -> contentArea.setCenter(VenuesPanel.get(admin)));
        btnAmenities.setOnAction(e -> contentArea.setCenter(AmenitiesPanel.get(admin)));
        btnCustomers.setOnAction(e -> contentArea.setCenter(CustomersPanel.get(admin)));

        // EXIT BUTTON: close dashboard and show login again
        btnExit.setOnAction(e -> {
            window.close();       // close dashboard
            loginStage.show();    // show login stage again
        });

        // MAIN LAYOUT
        BorderPane layout = new BorderPane();
        layout.setLeft(navBar);
        layout.setCenter(contentArea);

        Scene scene = new Scene(layout, 1200, 700);
        window.setScene(scene);
        window.show();
        window.setMaximized(true);
    }

    // Hover and Button Styling
    private static Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("""
            -fx-background-color: #34495E;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-padding: 10 20 10 20;
        """);
        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #3E5871;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-padding: 10 20 10 20;
        """));
        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: #34495E;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-padding: 10 20 10 20;
        """));
        return btn;
    }
}
