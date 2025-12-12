// RegistrationsPanel.java
package iVenue.ui;

import iVenue.models.Customer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrationsPanel {

    public static void openRegistration(Stage owner) {
        Stage regStage = new Stage();
        regStage.setTitle("Register Customer");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button submit = new Button("Register");
        submit.setOnAction(e -> {
            String usernameVal = usernameField.getText().trim();
            String passwordVal = passwordField.getText().trim();
            String firstNameVal = firstNameField.getText().trim();
            String lastNameVal = lastNameField.getText().trim();
            String contactVal = contactField.getText().trim();
            if (contactVal.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Contact number is required").show();
                return;
            }

            // Contact Num Validate - Only allow digits and optional leading "+"
            if (!contactVal.matches("^\\+?\\d+$")) {
                new Alert(Alert.AlertType.ERROR, "Contact number can only contain digits and an optional leading '+'").show();
                return;
            }

            String emailVal = emailField.getText().trim();
            if (emailVal.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Email is required").show();
                return;
            }

            // Validate Gmail format
            if (!emailVal.matches("^[\\w._%+-]+@gmail\\.com$")) {
                new Alert(Alert.AlertType.ERROR, "Only Gmail addresses are accepted (must end with @gmail.com)").show();
                return;
            }

            // Username, password, first/last name required
            if (usernameVal.isEmpty() || passwordVal.isEmpty() || firstNameVal.isEmpty() || lastNameVal.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Please fill all required fields").show();
                return;
            }

            // --- Register customer ---
            var customer = iVenue.models.Customer.registerCustomerGUI(
                    usernameVal, passwordVal, firstNameVal, lastNameVal, contactVal, emailVal
            );

            Alert alert = new Alert(customer != null ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setContentText(customer != null ? "Registration successful!" : "Registration failed. Check input.");
            alert.initOwner(regStage);
            alert.showAndWait();

            if (customer != null) regStage.close();
        });

        root.getChildren().addAll(usernameField, passwordField, firstNameField, lastNameField, contactField, emailField, submit);

        Scene scene = new Scene(root, 400, 400);
        regStage.setScene(scene);
        regStage.initOwner(owner);
        regStage.show();
    }
}
