package iVenue.ui;

import iVenue.models.Admin;
import iVenue.models.Venue;
import iVenue.services.VenueAdmin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class VenuesPanel {

    private static final VenueAdmin adminService = new VenueAdmin();
    private static VBox root;
    private static FlowPane cardsContainer;

    public static VBox get(Admin admin) {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("VENUES MANAGEMENT - iVenue Venue List");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // --- Controls ---
        Button addBtn = new Button("Add Venue");
        addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> openAddDialog(admin));

        Button showFreeBtn = new Button("Show Free");
        showFreeBtn.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        showFreeBtn.setOnAction(e -> filterFreePaid(true));

        Button showPaidBtn = new Button("Show Paid");
        showPaidBtn.setStyle("-fx-background-color: #c1121f; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        showPaidBtn.setOnAction(e -> filterFreePaid(false));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");
        searchField.setPrefWidth(200);

        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #ff6d00; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        searchBtn.setOnAction(e -> searchVenue(searchField.getText().trim()));

        HBox topControls = new HBox(10, addBtn, showFreeBtn, showPaidBtn, searchField, searchBtn);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.setPadding(new Insets(10, 0, 10, 0));

        // --- Cards Container ---
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        cardsContainer = new FlowPane();
        cardsContainer.setHgap(15);
        cardsContainer.setVgap(15);
        cardsContainer.setPadding(new Insets(10));
        cardsContainer.setPrefWidth(Double.MAX_VALUE);
        scrollPane.setContent(cardsContainer);

        root.getChildren().addAll(title, topControls, scrollPane);

        refreshCards(admin);

        return root;
    }

    // --- Refresh cards ---
    private static void refreshCards(Admin admin) {
        refreshCards(admin, adminService.getAll());
    }

    private static void refreshCards(Admin admin, List<Venue> venues) {
        cardsContainer.getChildren().clear();

        for (Venue venue : venues) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");
            card.setAlignment(Pos.TOP_CENTER);

            // Uniform responsive size
            card.prefWidthProperty().bind(cardsContainer.widthProperty().subtract(30).divide(2.1));
            card.setPrefHeight(300); // same height for all cards

            Text name = new Text("Name: " + venue.getName());
            name.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            name.wrappingWidthProperty().bind(card.prefWidthProperty().subtract(20));

            Text description = new Text("Description: " + venue.getDescription());
            description.wrappingWidthProperty().bind(card.prefWidthProperty().subtract(20));

            Text capacity = new Text("Capacity: " + venue.getCapacity());
            Text availability = new Text("Availability: " + (venue.isAvailability() ? "Available" : "Booked"));
            Text location = new Text("Location: " + venue.getLocation());
            Text price = new Text("Price: " + (venue.isFree() ? "FREE" : "₱" + venue.getPrice()));

            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            editBtn.setStyle("-fx-background-color: #003f91; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 100;");
            deleteBtn.setStyle("-fx-background-color: #c1121f; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 100;");

            editBtn.setOnAction(e -> openEditDialog(admin, venue));
            deleteBtn.setOnAction(e -> confirmDelete(venue));

            HBox buttonBox = new HBox(10, editBtn, deleteBtn);
            buttonBox.setAlignment(Pos.CENTER);

            // Spacer to push buttons to bottom
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            card.getChildren().addAll(name, description, capacity, availability, location, price, spacer, buttonBox);

            cardsContainer.getChildren().add(card);
        }
    }

    // --- ADD VENUE DIALOG ---
    private static void openAddDialog(Admin admin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Venue");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");

        TextField locationField = new TextField();
        locationField.setPromptText("Location");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        CheckBox freeCheck = new CheckBox("Free Venue");

        VBox layout = new VBox(10, nameField, descField, capacityField, locationField, priceField, freeCheck);
        layout.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    String desc = descField.getText().trim();
                    int capacity = Integer.parseInt(capacityField.getText().trim());
                    String location = locationField.getText().trim();
                    double price = freeCheck.isSelected() ? 0 : Double.parseDouble(priceField.getText().trim());
                    boolean isFree = freeCheck.isSelected();

                    if (name.isEmpty() || desc.isEmpty() || location.isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Please fill all fields.").show();
                        return;
                    }

                    Venue venue = new Venue(0, name, desc, capacity, true, location, price);
                    boolean success = adminService.create(venue);

                    if (!success) {
                        new Alert(Alert.AlertType.ERROR, "Failed to add venue!").show();
                    } else {
                        refreshCards(admin);
                    }
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Capacity and Price must be numbers!").show();
                }
            }
        });
    }

    // --- EDIT VENUE DIALOG ---
    private static void openEditDialog(Admin admin, Venue venue) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Venue");

        TextField nameField = new TextField(venue.getName());
        TextArea descField = new TextArea(venue.getDescription());
        descField.setPrefRowCount(3);

        TextField capacityField = new TextField(String.valueOf(venue.getCapacity()));
        TextField locationField = new TextField(venue.getLocation());
        TextField priceField = new TextField(String.valueOf(venue.getPrice()));

        CheckBox freeCheck = new CheckBox("Free Venue");
        freeCheck.setSelected(venue.isFree());

        VBox layout = new VBox(10, nameField, descField, capacityField, locationField, priceField, freeCheck);
        layout.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    String desc = descField.getText().trim();
                    int capacity = Integer.parseInt(capacityField.getText().trim());
                    String location = locationField.getText().trim();
                    double price = freeCheck.isSelected() ? 0 : Double.parseDouble(priceField.getText().trim());
                    boolean isFree = freeCheck.isSelected();

                    if (name.isEmpty() || desc.isEmpty() || location.isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Please fill all fields.").show();
                        return;
                    }

                    venue.setName(name);
                    venue.setDescription(desc);
                    venue.setCapacity(capacity);
                    venue.setLocation(location);
                    venue.setPrice(price);
                    venue.setFree(isFree);

                    boolean success = adminService.update(venue);

                    if (!success) {
                        new Alert(Alert.AlertType.ERROR, "Failed to update venue!").show();
                    } else {
                        refreshCards(admin);
                    }
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Capacity and Price must be numbers!").show();
                }
            }
        });
    }

    // --- DELETE VENUE CONFIRMATION ---
    private static void confirmDelete(Venue venue) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Venue");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete venue: " + venue.getName() + "?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean success = adminService.delete(venue.getVenueId());
                if (!success) {
                    new Alert(Alert.AlertType.ERROR, "Failed to delete venue!").show();
                } else {
                    refreshCards(null);
                }
            }
        });
    }

    // --- Filter Free/Paid ---
    private static void filterFreePaid(boolean freeOnly) {
        List<Venue> venues = adminService.getAll();
        if (freeOnly) {
            venues = venues.stream().filter(Venue::isFree).collect(Collectors.toList());
        } else {
            venues = venues.stream().filter(v -> !v.isFree()).collect(Collectors.toList());
        }
        refreshCards(null, venues);
    }

    // --- Search by name ---
    private static void searchVenue(String query) {
        List<Venue> venues = adminService.getAll();
        if (!query.isEmpty()) {
            venues = venues.stream()
                    .filter(v -> v.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
        refreshCards(null, venues);
    }
}
