package iVenue.ui;

import iVenue.models.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class CustomerDashboard {

    public static void display(Customer customer, Stage loginStage) {
        Stage window = new Stage();
        window.setTitle("iVenue - Customer Dashboard");

        // =======================
        // TOP NAVIGATION BAR
        // =======================
        HBox navBar = new HBox(20);
        navBar.setPadding(new Insets(15));
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setStyle("-fx-background-color: #2C3E50;");

        Button btnExplore = createNavButton("Explore Venues");
        Button btnBookNow = createNavButton("Book Now");
        Button btnMyBookings = createNavButton("My Bookings");
        Button btnLogout = createNavButton("Logout");

        navBar.getChildren().addAll(btnExplore, btnBookNow, btnMyBookings, btnLogout);

        // =======================
        // MAIN CONTENT AREA
        // =======================
        BorderPane contentArea = new BorderPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #ECF0F1;");

        // -----------------------
        // Single instance of ExplorePanel
        // -----------------------
        AtomicReference<ExplorePanel> explorePanelRef = new AtomicReference<>();
        explorePanelRef.set(new ExplorePanel(customer, venue -> {
            // Open BookingFormsPanel with callback back to the same ExplorePanel instance
            contentArea.setCenter(BookingFormsPanel.get(venue, customer,
                    () -> contentArea.setCenter(explorePanelRef.get())));
        }));

        // Default content
        contentArea.setCenter(explorePanelRef.get());

        // =======================
        // NAVIGATION BUTTON EVENTS
        // =======================
        btnExplore.setOnAction(e -> contentArea.setCenter(explorePanelRef.get()));

        btnBookNow.setOnAction(e -> {
            Label placeholder = new Label("Select a venue first from the Explore tab.");
            placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #2C3E50;");
            contentArea.setCenter(placeholder);
        });

        btnMyBookings.setOnAction(e -> contentArea.setCenter(new BookingStatusPanel(customer)));

        btnLogout.setOnAction(e -> {
            window.close();
            if (loginStage != null) loginStage.show();
        });

        // =======================
        // FINAL LAYOUT
        // =======================
        BorderPane layout = new BorderPane();
        layout.setTop(navBar);
        layout.setCenter(contentArea);

        Scene scene = new Scene(layout, 1200, 700);
        window.setScene(scene);
        window.setMaximized(true);
        window.show();
    }

    // =======================
    // BUTTON STYLE
    // =======================
    private static Button createNavButton(String text) {
        Button btn = new Button(text);
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
