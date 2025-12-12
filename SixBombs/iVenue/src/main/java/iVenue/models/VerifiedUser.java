package iVenue.models;

import iVenue.repositories.UserStore;
import iVenue.config.MongoDb;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * VerifiedUser handles authentication for admins and other non-customer users.
 */
public class VerifiedUser extends User {

    /** No-arg constructor for GUI login helper */
    public VerifiedUser() {
        super("", "", 0); // dummy values for GUI usage
    }

    public VerifiedUser(String username, String password, int userId) {
        super(username, password, userId);
    }

    /**
     * Loads the default admin from MongoDB.
     */
    public static VerifiedUser loadAdminFromDb() {
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("users");
        Document doc = collection.find(new Document("userType", "admin")).first();
        if (doc == null) return null;

        String username = doc.getString("username");
        String password = doc.getString("password");
        int userId = doc.getInteger("userId");

        return new VerifiedUser(username, password, userId);
    }

    /**
     * Shows a GUI login dialog and returns the authenticated User (Admin or Customer) or null.
     */
    public User loginGui(Stage owner) {
        Dialog<User> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Login");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return UserStore.findByCredentials(
                        usernameField.getText().trim(),
                        passwordField.getText()
                );
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}
