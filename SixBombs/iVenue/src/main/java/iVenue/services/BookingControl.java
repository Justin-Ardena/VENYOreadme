package iVenue.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import iVenue.config.MongoDb;
import iVenue.models.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingAdmin implements AdminManagement<Booking> {

    private final MongoCollection<Document> collection;

    public BookingAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("bookings");
    }

    // Create booking (GUI-friendly)
    @Override
    public boolean create(Booking booking) {
        if (booking == null || booking.getVenue() == null) return false;

        // Generate Booking ID
        int maxId = 0;
        Document lastBooking = collection.find().sort(new Document("bookingId", -1)).first();
        if (lastBooking != null) {
            maxId = lastBooking.getInteger("bookingId");
        }
        booking.setBookingId(maxId + 1);

        // Build amenities docs
        List<Document> amenityDocs = new ArrayList<>();
        if (booking.getAmenities() != null) {
            for (Amenity a : booking.getAmenities()) {
                amenityDocs.add(new Document("amenityId", a.getAmenityId())
                        .append("name", a.getName())
                        .append("price", a.getPrice())
                        .append("quantity", a.getQuantity()));
            }
        }

        // Build MongoDB document
        Document doc = new Document("bookingId", booking.getBookingId())
                .append("venueId", booking.getVenue().getVenueId())
                .append("venueName", booking.getVenue().getName())
                .append("date", new Date().toString())
                .append("paymentStatus", booking.getPaymentStatus().name())
                .append("bookingStatus", booking.getBookingStatus().name())
                .append("purpose", booking.getPurpose())
                .append("bookedBy", booking.getUsername())
                .append("amenities", amenityDocs)
                .append("price", booking.getVenue().getPrice())
                .append("isFree", booking.getVenue().isFree());

        collection.insertOne(doc);
        return true;
    }

    // Update booking status (GUI-friendly)
    @Override
    public boolean update(Booking booking) {
        if (booking == null) return false;
        Document doc = collection.find(new Document("bookingId", booking.getBookingId())).first();
        if (doc == null) return false;

        collection.updateOne(
                new Document("bookingId", booking.getBookingId()),
                new Document("$set", new Document("bookingStatus", booking.getBookingStatus().name())
                        .append("paymentStatus", booking.getPaymentStatus().name()))
        );

        // If finished or cancelled, mark venue available
        if (booking.getBookingStatus() == BookingStatus.FINISHED ||
                booking.getBookingStatus() == BookingStatus.CANCELLED) {
            int venueId = doc.getInteger("venueId");
            MongoDb.getDatabase().getCollection("venues")
                    .updateOne(new Document("venueId", venueId),
                            new Document("$set", new Document("availability", true)));
        }

        return true;
    }

    // Delete booking by ID
    @Override
    public boolean delete(int bookingId) {
        Document doc = collection.find(new Document("bookingId", bookingId)).first();
        if (doc == null) return false;

        // Mark venue available
        if (doc.containsKey("venueId")) {
            int venueId = doc.getInteger("venueId");
            MongoDb.getDatabase().getCollection("venues")
                    .updateOne(new Document("venueId", venueId),
                            new Document("$set", new Document("availability", true)));
        }

        // Record deleted history
        Booking snapshot = new Booking(
                bookingId,
                null,
                null,
                PaymentStatus.valueOf(doc.getString("paymentStatus")),
                BookingStatus.valueOf(doc.getString("bookingStatus")),
                doc.getString("purpose"),
                doc.getString("bookedBy")
        );
        BookingHistory.addDeleted(snapshot);

        collection.deleteOne(new Document("bookingId", bookingId));
        return true;
    }

    // Fetch all bookings
    public List<Booking> getAll() {
        List<Booking> out = new ArrayList<>();
        for (Document doc : collection.find()) {
            Booking b = new Booking(
                    doc.getInteger("bookingId"),
                    null, // venue object can be fetched separately if needed
                    new Date(), // placeholder
                    PaymentStatus.valueOf(doc.getString("paymentStatus")),
                    BookingStatus.valueOf(doc.getString("bookingStatus")),
                    doc.getString("purpose"),
                    doc.getString("bookedBy")
            );
            out.add(b);
        }
        return out;
    }

    // Fetch single booking by ID
    public Booking getById(int bookingId) {
        Document doc = collection.find(new Document("bookingId", bookingId)).first();
        if (doc == null) return null;

        return new Booking(
                doc.getInteger("bookingId"),
                null, // venue can be fetched if necessary
                new Date(), // placeholder
                PaymentStatus.valueOf(doc.getString("paymentStatus")),
                BookingStatus.valueOf(doc.getString("bookingStatus")),
                doc.getString("purpose"),
                doc.getString("bookedBy")
        );
    }
}
