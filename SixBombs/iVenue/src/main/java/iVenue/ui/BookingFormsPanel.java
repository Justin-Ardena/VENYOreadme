package iVenue.ui;

import iVenue.config.MongoDb;
import iVenue.models.*;
import iVenue.services.AmenityAdmin;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;

public class BookingFormsPanel {

    public static Pane get(Venue venue, Customer customer, Runnable onBack) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ECF0F1;");

        // ---------------- TOP BAR ----------------
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button btnBack = new Button("← Back");
        btnBack.setStyle("-fx-background-color:#34495E; -fx-text-fill:white; -fx-padding:8 15;");
        btnBack.setOnAction(e -> onBack.run());
        Text title = new Text("Book: " + venue.getName());
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-fill:#2C3E50;");
        topBar.getChildren().addAll(btnBack, title);
        root.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0,0,10,0));

        // ---------------- LEFT FORM ----------------
        VBox form = new VBox(12);
        form.setPadding(new Insets(10));
        form.setPrefWidth(520);

        Label lblCustomer = new Label("Customer: " + (customer != null ? customer.getUsername() : "Guest"));
        Label lblPurpose = new Label("Purpose of Booking");
        TextField txtPurpose = new TextField();
        txtPurpose.setPromptText("Ex: Wedding, Birthday, Conference...");

        Label lblGuests = new Label("Number of Guests (optional)");
        TextField txtGuests = new TextField();
        txtGuests.setPromptText("e.g. 50");

        form.getChildren().addAll(lblCustomer, lblPurpose, txtPurpose, lblGuests, txtGuests);

        // ---------------- RIGHT COLUMN ----------------
        VBox rightCol = new VBox(12);
        rightCol.setPadding(new Insets(10));
        rightCol.setPrefWidth(420);

        Label lblCalendar = new Label("Select Date");
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(300);
        Set<LocalDate> booked = getBookedDatesFromDb(venue.getVenueId());
        datePicker.setDayCellFactory(getDateCellFactory(booked));
        datePicker.setValue(LocalDate.now());

        Label lblAmenities = new Label("Select Amenities");
        AmenityAdmin amenityAdmin = new AmenityAdmin();
        List<Amenity> amenitiesList = amenityAdmin.getAll();

        VBox amenityBox = new VBox(8);
        ObservableList<CheckBox> amenityChecks = FXCollections.observableArrayList();
        for (Amenity a : amenitiesList) {
            CheckBox cb = new CheckBox(a.getName() + " — ₱" + a.getPrice());
            cb.setUserData(a);
            cb.setStyle("-fx-font-size:13;");
            amenityChecks.add(cb);
            amenityBox.getChildren().add(cb);
        }

        Label lblTotal = new Label("Total: ₱" + formatPrice(venue.getPrice()));
        lblTotal.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        amenityChecks.forEach(cb -> cb.setOnAction(ev -> {
            double total = venue.getPrice();
            for (CheckBox c : amenityChecks) if (c.isSelected()) total += ((Amenity)c.getUserData()).getPrice();
            lblTotal.setText("Total: ₱" + formatPrice(total));
        }));

        rightCol.getChildren().addAll(lblCalendar, datePicker, lblAmenities, amenityBox, lblTotal);

        HBox center = new HBox(30, form, rightCol);
        center.setPadding(new Insets(10));
        root.setCenter(center);

        // ---------------- PAYMENT / BOOK BUTTONS ----------------
        Button btnFullPay = new Button("Fully Pay");
        Button btnDownPay = new Button("Down Pay");
        Button btnBookFree = new Button("Book (Free)");

        btnFullPay.setStyle("-fx-background-color:#27AE60; -fx-text-fill:white; -fx-padding:10 20;");
        btnDownPay.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white; -fx-padding:10 20;");
        btnBookFree.setStyle("-fx-background-color:#3498DB; -fx-text-fill:white; -fx-padding:10 20;");

        // Enable buttons conditionally
        btnFullPay.setDisable(venue.getPrice() <= 0);
        btnDownPay.setDisable(venue.getPrice() <= 0);
        btnBookFree.setDisable(venue.getPrice() > 0);

        // Booking logic
        Consumer<PaymentStatus> bookVenue = payment -> {
            if (datePicker.getValue() == null) { alert("Please select a date."); return; }
            if (txtPurpose.getText() == null || txtPurpose.getText().trim().isEmpty()) { alert("Purpose is required."); return; }

            List<Booking.AmenitySelection> selections = new ArrayList<>();
            for (CheckBox c : amenityChecks) if (c.isSelected()) {
                Amenity a = (Amenity)c.getUserData();
                selections.add(new Booking.AmenitySelection(a.getAmenityId(), 1, a.getPrice()));
            }

            Booking booking = Booking.createBookingGUI(customer, venue, selections, txtPurpose.getText().trim(),
                    payment == PaymentStatus.PAID || payment == PaymentStatus.DOWNPAID,
                    payment == PaymentStatus.DOWNPAID);

            if (booking == null) { alert("Booking failed."); return; }

            // Update booking date & payment status in DB
            try {
                LocalDate chosen = datePicker.getValue();
                Date asDate = Date.from(chosen.atStartOfDay(ZoneId.systemDefault()).toInstant());
                MongoCollection<Document> coll = MongoDb.getDatabase().getCollection("bookings");
                coll.updateOne(new Document("bookingId", booking.getBookingId()),
                        new Document("$set", new Document("date", asDate)
                                .append("paymentStatus", payment.name())));
            } catch (Exception ex) { ex.printStackTrace(); }

            showBookingSummary(booking, selections, venue);
            onBack.run();
        };

        // Assign actions
        btnFullPay.setOnAction(e -> bookVenue.accept(PaymentStatus.PAID));
        btnDownPay.setOnAction(e -> bookVenue.accept(PaymentStatus.DOWNPAID));
        btnBookFree.setOnAction(e -> bookVenue.accept(PaymentStatus.PAID)); // treat free as fully paid

        // Add buttons to bottom HBox
        HBox bottom = new HBox(20);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(12,0,0,0));
        if (venue.getPrice() > 0) {
            bottom.getChildren().addAll(btnFullPay, btnDownPay);
        } else {
            bottom.getChildren().add(btnBookFree);
        }
        root.setBottom(bottom);

        return root;
    }

    // ---------------- HELPER METHODS ----------------
    private static Set<LocalDate> getBookedDatesFromDb(int venueId) {
        Set<LocalDate> out = new HashSet<>();
        try {
            MongoCollection<Document> coll = MongoDb.getDatabase().getCollection("bookings");
            for (Document d : coll.find(new Document("venueId", venueId)
                    .append("bookingStatus", BookingStatus.BOOKED.name())
                    .append("paymentStatus", new Document("$in",
                            Arrays.asList(PaymentStatus.PAID.name(), PaymentStatus.DOWNPAID.name()))))) {
                if (d.containsKey("date")) {
                    Object o = d.get("date");
                    if (o instanceof Date date) out.add(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    else if (o instanceof String s) {
                        try { out.add(LocalDate.parse(s.substring(0, Math.min(10, s.length())))); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }

    private static Callback<DatePicker, DateCell> getDateCellFactory(Set<LocalDate> bookedDates) {
        return dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setDisable(true); return; }
                if (date.isBefore(LocalDate.now()) || date.isAfter(LocalDate.now().plusDays(30))) {
                    setDisable(true); setStyle("-fx-background-color:#EEEEEE;"); return;
                }
                if (bookedDates.contains(date)) { setDisable(true); setStyle("-fx-background-color:#FF6666; -fx-text-fill:white;"); }
            }
        };
    }

    private static void showBookingSummary(Booking booking, List<Booking.AmenitySelection> selections, Venue venue) {
        Stage stage = new Stage();
        stage.setTitle("Booking Summary");
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(16));

        layout.getChildren().add(new Text("Booking ID: " + booking.getBookingId()));
        layout.getChildren().add(new Text("Venue: " + venue.getName()));
        layout.getChildren().add(new Text("Purpose: " + booking.getPurpose()));
        layout.getChildren().add(new Text("Status: " + (booking.getBookingStatus() != null ? booking.getBookingStatus() : "N/A")));
        layout.getChildren().add(new Text("Payment: " + (booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "N/A")));

        if (!selections.isEmpty()) {
            layout.getChildren().add(new Text("Amenities:"));
            VBox list = new VBox(6);
            AmenityAdmin aAdmin = new AmenityAdmin();
            for (Booking.AmenitySelection s : selections) {
                Amenity a = aAdmin.getById(s.getAmenityId());
                String label = (a != null ? a.getName() : ("Amenity#" + s.getAmenityId())) + " x" + s.getQuantity() + " - ₱" + s.getPrice();
                list.getChildren().add(new Text(label));
            }
            layout.getChildren().add(list);
        }

        Scene scene = new Scene(layout, 420, 350);
        stage.setScene(scene);
        stage.show();
    }

    private static void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private static String formatPrice(double p) {
        if (p == (long) p) return String.format("%d", (long) p);
        return String.format("%.2f", p);
    }
}
