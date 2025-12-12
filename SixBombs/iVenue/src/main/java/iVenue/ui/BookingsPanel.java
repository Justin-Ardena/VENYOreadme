package iVenue.ui;

import iVenue.models.Admin;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class BookingsPanel {
    public static VBox get(Admin admin) {
        VBox box = new VBox(10);
        box.getChildren().add(new Text("BOOKINGS MANAGEMENT PANEL - implement UI here"));
        return box;
    }
}