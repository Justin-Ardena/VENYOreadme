package iVenue.ui;

import iVenue.models.Admin;
import iVenue.models.Customer;
import iVenue.services.CustomerAdmin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomersPanel {

    private static TableView<Customer> table;
    private static ObservableList<Customer> customerList;

    // Reuse ONE instance of CustomerAdmin
    private static final CustomerAdmin adminService = new CustomerAdmin();

    public static VBox get(Admin admin) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("MANAGE CUSTOMERS - iVenue Customer List");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // TABLE
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 16px;");

        // USER ID COLUMN
        TableColumn<Customer, Integer> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // USERNAME
        TableColumn<Customer, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        // PASSWORD
        TableColumn<Customer, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));

        // FIRST NAME
        TableColumn<Customer, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        // LAST NAME
        TableColumn<Customer, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        // CONTACT NUMBER
        TableColumn<Customer, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        // EMAIL
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        // ACTIONS COLUMN
        TableColumn<Customer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                // Styling buttons
                editBtn.setStyle(
                        "-fx-background-color: #003f91; -fx-text-fill: white;" +
                                "-fx-font-size: 14px; -fx-padding: 10 20;"
                );
                deleteBtn.setStyle(
                        "-fx-background-color: #c1121f; -fx-text-fill: white;" +
                                "-fx-font-size: 14px; -fx-padding: 10 20;"
                );

                editBtn.setOnAction(e ->
                        openEditDialog(admin, getTableView().getItems().get(getIndex()))
                );

                deleteBtn.setOnAction(e ->
                        confirmDelete(getTableView().getItems().get(getIndex()))
                );
            }

            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                HBox.setHgrow(editBtn, Priority.ALWAYS);
                HBox.setHgrow(deleteBtn, Priority.ALWAYS);
                editBtn.setMaxWidth(Double.MAX_VALUE);
                deleteBtn.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(
                userIdCol, usernameCol, passwordCol, firstNameCol,
                lastNameCol, contactCol, emailCol, actionCol
        );

        refreshList();

        // Make table grow to fill available space
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, table);
        return root;
    }

    // =====================================================
    // REFRESH TABLE DATA
    // =====================================================
    private static void refreshList() {
        customerList = FXCollections.observableArrayList(adminService.getAll());
        table.setItems(customerList);
    }

    // =====================================================
    // EDIT DIALOG
    // =====================================================
    private static void openEditDialog(Admin admin, Customer customer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Customer");

        TextField username = new TextField(customer.getUsername());
        TextField password = new TextField(customer.getPassword());
        TextField firstName = new TextField(customer.getFirstName());
        TextField lastName = new TextField(customer.getLastName());
        TextField contact = new TextField(customer.getContactNumber());
        TextField email = new TextField(customer.getEmail());

        VBox layout = new VBox(10, username, password, firstName, lastName, contact, email);
        layout.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {

                // Validate Gmail format
                String emailVal = email.getText().trim();
                if (emailVal.isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Email is required").show();
                    return;
                }

                if (!emailVal.matches("^[\\w._%+-]+@gmail\\.com$")) {
                    new Alert(Alert.AlertType.ERROR, "Only Gmail addresses are accepted (must end with @gmail.com)").show();
                    return;
                }

                // Contact Validation - Only allow digits and optional leading "+"
                String contactVal = contact.getText().trim();
                if (contactVal.isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Contact number is required").show();
                    return;
                }

                if (!contactVal.matches("^\\+?\\d+$")) {
                    new Alert(Alert.AlertType.ERROR, "Contact number can only contain digits and an optional leading '+'").show();
                    return;
                }

                if (username.getText().trim().isEmpty() || password.getText().trim().isEmpty() ||
                        firstName.getText().trim().isEmpty() || lastName.getText().trim().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "All fields must be filled").show();
                    return;
                }

                // --- Update customer object ---
                customer.setUsername(username.getText().trim());
                customer.setPassword(password.getText().trim());
                customer.setFirstName(firstName.getText().trim());
                customer.setLastName(lastName.getText().trim());
                customer.setContactNumber(contactVal);
                customer.setEmail(emailVal);

                // Save to DB via adminService
                boolean success = admin.updateCustomer(customer);
                if (!success) {
                    new Alert(Alert.AlertType.ERROR, "Update failed!").show();
                    return;
                }
                refreshList();
            }
        });
    }

    // =====================================================
    // DELETE CUSTOMER
    // =====================================================
    private static void confirmDelete(Customer customer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Customer");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete customer: " + customer.getUsername() + "?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                adminService.delete(customer.getUserId());
                refreshList();
            }
        });
    }
}
