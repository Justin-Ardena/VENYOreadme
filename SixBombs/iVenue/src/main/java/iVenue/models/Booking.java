package iVenue.models;

import iVenue.services.VenueAdmin;
import iVenue.config.MongoDb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Booking {

    private LinkedList<Amenity> amenities;
    private static LinkedList<Booking> bookings = new LinkedList<>();
    private PaymentStatus paymentStatus;
    private BookingStatus bookingStatus;
    private int bookingId;
    private Venue venue;
    private Date date;
    private String purpose;
    private String username;

    public Booking(int bookingId, Venue venue, Date date,
                   PaymentStatus paymentStatus, BookingStatus bookingStatus,
                   String purpose, String username) {
        this.bookingId = bookingId;
        this.venue = venue;
        this.date = date;
        this.paymentStatus = paymentStatus;
        this.bookingStatus = bookingStatus;
        this.purpose = purpose;
        this.amenities = new LinkedList<>();
        this.username = username;
    }

    // ==============================
    // GETTERS & SETTERS
    // ==============================
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LinkedList<Amenity> getAmenities() { return amenities; }
    public void setAmenities(LinkedList<Amenity> amenities) { this.amenities = amenities; }
    public static LinkedList<Booking> getBookings() { return bookings; }
    public static void setBookings(LinkedList<Booking> bookings) { Booking.bookings = bookings; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // ==============================
    // CREATE BOOKING (GUI-ready)
    // ==============================
    public static Booking createBookingGUI(Customer customer, Venue venue,
                                           List<AmenitySelection> selectedAmenities,
                                           String purpose, boolean payNow, boolean lockVenue) {
        if (customer == null || venue == null) return null;

        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");

        // Generate bookingId
        int maxId = 0;
        Document last = collection.find().sort(new Document("bookingId", -1)).first();
        if (last != null) maxId = last.getInteger("bookingId");

        // Map selected amenities to Document
        LinkedList<Document> amenitiesDocs = new LinkedList<>();
        if (selectedAmenities != null) {
            for (AmenitySelection sel : selectedAmenities) {
                amenitiesDocs.add(new Document("amenityId", sel.getAmenityId())
                        .append("quantity", sel.getQuantity())
                        .append("price", sel.getPrice() * sel.getQuantity()));
            }
        }

        PaymentStatus paymentStatus = payNow ? PaymentStatus.UNPAID : PaymentStatus.UNPAID;
        BookingStatus bookingStatus = payNow ? BookingStatus.BOOKED : BookingStatus.PENDING;

        // Lock venue if requested
        MongoDb.getDatabase().getCollection("venues")
                .updateOne(new Document("venueId", venue.getVenueId()),
                        new Document("$set", new Document("availability", !lockVenue)));

        // Build bookedBy document
        Document userDoc = MongoDb.getDatabase().getCollection("users")
                .find(new Document("userId", customer.getUserId())).first();

        Document bookedBy = new Document("userId", customer.getUserId())
                .append("username", customer.getUsername())
                .append("firstName", userDoc != null ? userDoc.getString("firstName") : customer.getFirstName())
                .append("lastName", userDoc != null ? userDoc.getString("lastName") : customer.getLastName())
                .append("contactNumber", userDoc != null ? userDoc.getString("contactNumber") : customer.getContactNumber())
                .append("email", userDoc != null ? userDoc.getString("email") : customer.getEmail());

        // Insert booking document
        Document bookingDoc = new Document("bookingId", maxId + 1)
                .append("venueId", venue.getVenueId())
                .append("venueName", venue.getName())
                .append("userId", customer.getUserId())
                .append("bookedBy", bookedBy)
                .append("date", new Date().toString())
                .append("paymentStatus", paymentStatus.name())
                .append("bookingStatus", bookingStatus.name())
                .append("purpose", purpose)
                .append("amenities", amenitiesDocs)
                .append("price", venue.getPrice())
                .append("isFree", venue.isFree());

        collection.insertOne(bookingDoc);

        // Create Booking object
        Booking booking = new Booking(maxId + 1, venue, new Date(),
                paymentStatus, bookingStatus, purpose, customer.getUsername());

        // Populate Amenity objects if needed
        booking.setAmenities(new LinkedList<>());

        bookings.add(booking);
        return booking;
    }

    // ==============================
    // CANCEL BOOKING (GUI-ready + history)
    // ==============================
    public static boolean cancelBookingGUI(Customer customer, int bookingId) {
        if (customer == null) return false;

        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        Document doc = collection.find(new Document("bookingId", bookingId)
                .append("userId", customer.getUserId())).first();

        if (doc == null) return false;

        // Update booking status
        collection.updateOne(
                new Document("bookingId", bookingId),
                new Document("$set", new Document("bookingStatus", BookingStatus.CANCELLED.name())
                        .append("paymentStatus", PaymentStatus.CANCELLED.name()))
        );

        // Mark venue available
        int venueId = doc.getInteger("venueId");
        MongoDb.getDatabase().getCollection("venues")
                .updateOne(new Document("venueId", venueId),
                        new Document("$set", new Document("availability", true)));

        // Add to BookingHistory
        Booking snapshot = new Booking(
                bookingId,
                null,
                null,
                PaymentStatus.CANCELLED,
                BookingStatus.CANCELLED,
                doc.getString("purpose"),
                doc.get("bookedBy") instanceof Document bb ? bb.getString("username") : "N/A"
        );
        BookingHistory.addDeleted(snapshot);

        // Remove from active bookings
        collection.deleteOne(new Document("bookingId", bookingId));

        return true;
    }

    // ==============================
    // PAY BOOKING (GUI-ready)
    // ==============================
    public static boolean payBookingGUI(Customer customer, int bookingId, boolean fullPayment) {
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        Document doc = collection.find(new Document("bookingId", bookingId)
                .append("userId", customer.getUserId())).first();
        if (doc == null) return false;

        if (PaymentStatus.PAID.name().equalsIgnoreCase(doc.getString("paymentStatus"))) return false;

        double total = 0;
        int venueId = doc.getInteger("venueId");
        VenueAdmin venueAdmin = new VenueAdmin();
        Venue venue = venueAdmin.getById(venueId);

        if (doc.containsKey("amenities")) {
            List<Document> amenitiesList = doc.getList("amenities", Document.class);
            for (Document aDoc : amenitiesList) total += aDoc.getDouble("price");
        }

        double amountPaid = fullPayment ? total : total * 0.5;
        PaymentStatus status = fullPayment ? PaymentStatus.PAID : PaymentStatus.DOWNPAID;

        collection.updateOne(
                new Document("bookingId", bookingId),
                new Document("$set", new Document("paymentStatus", status.name())
                        .append("amountPaid", amountPaid)
                        .append("total", total))
        );

        return true;
    }

    // ==============================
    // FINISH BOOKING (GUI-ready + history)
    // ==============================
    public static boolean finishBookingGUI(int bookingId) {
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        Document doc = collection.find(new Document("bookingId", bookingId)).first();
        if (doc == null) return false;

        // Update status to FINISHED
        collection.updateOne(
                new Document("bookingId", bookingId),
                new Document("$set", new Document("bookingStatus", BookingStatus.FINISHED.name()))
        );

        // Mark venue available
        int venueId = doc.getInteger("venueId");
        MongoDb.getDatabase().getCollection("venues")
                .updateOne(new Document("venueId", venueId),
                        new Document("$set", new Document("availability", true)));

        // Add to BookingHistory
        Booking snapshot = new Booking(
                bookingId,
                null,
                null,
                PaymentStatus.valueOf(doc.getString("paymentStatus")),
                BookingStatus.FINISHED,
                doc.getString("purpose"),
                doc.get("bookedBy") instanceof Document bb ? bb.getString("username") : "N/A"
        );
        BookingHistory.addFinished(snapshot);

        // Remove from active bookings
        collection.deleteOne(new Document("bookingId", bookingId));
        return true;
    }

    // ==============================
    // VIEW BOOKINGS (returns data for GUI table)
    // ==============================
    public static List<Document> viewBookingsGUI(Customer customer) {
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        return collection.find(new Document("userId", customer.getUserId())).into(new LinkedList<>());
    }

    // ==============================
    // Helper class for GUI-selected amenities
    // ==============================
    public static class AmenitySelection {
        private int amenityId;
        private int quantity;
        private double price;

        public AmenitySelection(int amenityId, int quantity, double price) {
            this.amenityId = amenityId;
            this.quantity = quantity;
            this.price = price;
        }

        public int getAmenityId() { return amenityId; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }
}
