package iVenue.ui;

import iVenue.models.Admin;
import iVenue.models.Amenity;
import iVenue.services.AmenityAdmin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AmenitiesPanel {

    private static TableView<Amenity> table;
    private static ObservableList<Amenity> amenityList;

    // Single instance of AmenityAdmin
    private static final AmenityAdmin adminService = new AmenityAdmin();

    public static VBox get(Admin admin) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("AMENITIES MANAGEMENT - iVenue Amenity List");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // TABLE
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 16px;");

        // --- TABLE COLUMNS ----------------------------------------------------
        TableColumn<Amenity, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("amenityId"));
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Amenity, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Amenity, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Amenity, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Amenity, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Amenity, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #003f91; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
                deleteBtn.setStyle("-fx-background-color: #c1121f; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

                editBtn.setOnAction(e -> openEditDialog(admin, getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }

            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        actionCol.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(idCol, nameCol, descCol, qtyCol, priceCol, actionCol);

        refreshList();

        // Make table grow to fill space
        VBox.setVgrow(table, Priority.ALWAYS);

        // ADD NEW AMENITY BUTTON
        Button addBtn = new Button("Add Amenity");
        addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> openAddDialog(admin));

        HBox topControls = new HBox(addBtn);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.setPadding(new Insets(10, 0, 10, 0));

        root.getChildren().addAll(title, topControls, table);
        return root;
    }

    // =====================================================
    // REFRESH TABLE DATA
    // =====================================================
    private static void refreshList() {
        amenityList = FXCollections.observableArrayList(adminService.getAll());
        table.setItems(amenityList);
    }

    // =====================================================
    // ADD NEW AMENITY
    // =====================================================
    private static void openAddDialog(Admin admin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Amenity");

        TextField name = new TextField();
        TextField desc = new TextField();
        TextField qty = new TextField();
        TextField price = new TextField();

        VBox layout = new VBox(10,
                new Label("Name:"), name,
                new Label("Description:"), desc,
                new Label("Quantity:"), qty,
                new Label("Price:"), price
        );
        layout.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    Amenity a = new Amenity(
                            0,
                            name.getText().trim(),
                            desc.getText().trim(),
                            Integer.parseInt(qty.getText().trim()),
                            Double.parseDouble(price.getText().trim())
                    );
                    boolean success = adminService.create(a);
                    if (!success) new Alert(Alert.AlertType.ERROR, "Creation failed!").show();
                    refreshList();
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Quantity must be integer and Price must be a number").show();
                }
            }
        });
    }

    // =====================================================
    // EDIT AMENITY
    // =====================================================
    private static void openEditDialog(Admin admin, Amenity amenity) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Amenity");

        TextField name = new TextField(amenity.getName());
        TextField desc = new TextField(amenity.getDescription());
        TextField qty = new TextField(String.valueOf(amenity.getQuantity()));
        TextField price = new TextField(String.valueOf(amenity.getPrice()));

        VBox layout = new VBox(10,
                new Label("Name:"), name,
                new Label("Description:"), desc,
                new Label("Quantity:"), qty,
                new Label("Price:"), price
        );
        layout.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    amenity.setName(name.getText().trim());
                    amenity.setDescription(desc.getText().trim());
                    amenity.setQuantity(Integer.parseInt(qty.getText().trim()));
                    amenity.setPrice(Double.parseDouble(price.getText().trim()));

                    boolean success = adminService.update(amenity);
                    if (!success) new Alert(Alert.AlertType.ERROR, "Update failed!").show();
                    refreshList();
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Quantity must be integer and Price must be a number").show();
                }
            }
        });
    }

    // =====================================================
    // DELETE AMENITY
    // =====================================================
    private static void confirmDelete(Amenity amenity) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Amenity");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete amenity: " + amenity.getName() + "?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                adminService.delete(amenity.getAmenityId());
                refreshList();
            }
        });
    }
}
