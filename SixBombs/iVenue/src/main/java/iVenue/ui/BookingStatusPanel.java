package iVenue.ui;

import iVenue.models.*;
import iVenue.services.BookingAdmin;
import iVenue.services.VenueAdmin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class BookingStatusPanel extends VBox {

    private final BookingAdmin bookingAdmin = new BookingAdmin();
    private final TableView<Booking> table = new TableView<>();
    private final Customer customer;

    public BookingStatusPanel(Customer customer) {
        this.customer = customer;
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #ECF0F1;");

        Label title = new Label("My Bookings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        setupTable();

        getChildren().addAll(title, table);
        loadBookings();
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 14;");

        TableColumn<Booking, Integer> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getBookingId()).asObject());

        TableColumn<Booking, String> venueCol = new TableColumn<>("Venue");
        venueCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getVenue() != null ? d.getValue().getVenue().getName() : "N/A"
                ));

        TableColumn<Booking, String> purposeCol = new TableColumn<>("Purpose");
        purposeCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getPurpose())
        );

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getBookingStatus() != null ? d.getValue().getBookingStatus().name() : "N/A"
                )
        );

        TableColumn<Booking, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getPaymentStatus() != null ? d.getValue().getPaymentStatus().name() : "N/A"
                )
        );

        TableColumn<Booking, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnCancel = new Button("Cancel");

            {
                btnCancel.setOnAction(e -> {
                    Booking b = getTableView().getItems().get(getIndex());
                    if (b.getPaymentStatus() == PaymentStatus.DOWNPAID || b.getPaymentStatus() == PaymentStatus.PAID) {
                        Alert a = new Alert(Alert.AlertType.WARNING, "Cannot cancel a booking with down payment or paid.");
                        a.showAndWait();
                        return;
                    }
                    // Cancel booking using Booking class logic
                    boolean success = Booking.cancelBookingGUI(customer, b.getBookingId());
                    if (success) {
                        loadBookings(); // refresh table
                    } else {
                        Alert a = new Alert(Alert.AlertType.ERROR, "Failed to cancel booking.");
                        a.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking b = getTableView().getItems().get(getIndex());
                    // Disable the cancel button if booking is downpaid or fully paid
                    btnCancel.setDisable(b.getPaymentStatus() == PaymentStatus.DOWNPAID || b.getPaymentStatus() == PaymentStatus.PAID);
                    setGraphic(btnCancel);
                }
            }
        });

        table.getColumns().addAll(idCol, venueCol, purposeCol, statusCol, paymentCol, actionCol);
    }

    private void loadBookings() {
        List<Document> docs = Booking.viewBookingsGUI(customer);
        VenueAdmin venueAdmin = new VenueAdmin();

        List<Booking> bookings = docs.stream().map(doc -> {
            int venueId = doc.getInteger("venueId");
            Venue venue = venueAdmin.getById(venueId); // fetch the Venue object
            return new Booking(
                    doc.getInteger("bookingId"),
                    venue,
                    null, // date if needed
                    PaymentStatus.valueOf(doc.getString("paymentStatus")),
                    BookingStatus.valueOf(doc.getString("bookingStatus")),
                    doc.getString("purpose"),
                    doc.get("bookedBy") instanceof Document bb ? bb.getString("username") : "N/A"
            );
        }).collect(Collectors.toList());

        ObservableList<Booking> data = FXCollections.observableArrayList(bookings);
        table.setItems(data);
    }

}
