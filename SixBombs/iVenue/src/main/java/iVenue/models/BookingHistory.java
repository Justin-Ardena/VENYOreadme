package iVenue.models;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import iVenue.config.MongoDb;


public class BookingHistory {

    private static final Queue<Booking> finishedQueue = new LinkedList<>();
    private static final Queue<Booking> deletedQueue = new LinkedList<>();
    private static final MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("booking_history");

    static {
        loadHistory();
    }

    private static void loadHistory() {
        loadQueue(finishedQueue, "finished");
        loadQueue(deletedQueue, "cancelled");
    }

    private static void loadQueue(Queue<Booking> queue, String type) {
        for (Document doc : collection.find(new Document("type", type))) {
            PaymentStatus ps = parsePaymentStatus(doc.getString("paymentStatus"));
            BookingStatus bs = parseBookingStatus(doc.getString("bookingStatus"));
            Booking b = new Booking(
                    doc.getInteger("bookingId"),
                    null,
                    null,
                    ps,
                    bs,
                    doc.getString("purpose"),
                    doc.getString("username")
            );
            queue.offer(b);
        }
    }

    private static PaymentStatus parsePaymentStatus(String s) {
        if (s == null) return PaymentStatus.UNPAID;
        for (PaymentStatus ps : PaymentStatus.values()) {
            if (ps.name().equalsIgnoreCase(s)) return ps;
        }
        return PaymentStatus.UNPAID;
    }

    private static BookingStatus parseBookingStatus(String s) {
        if (s == null) return BookingStatus.PENDING;
        for (BookingStatus bs : BookingStatus.values()) {
            if (bs.name().equalsIgnoreCase(s)) return bs;
        }
        return BookingStatus.PENDING;
    }

    private static void writeHistory(Booking booking, String type) {
        if (booking == null) return;
        Document doc = new Document("bookingId", booking.getBookingId())
                .append("type", type)
                .append("paymentStatus", booking.getPaymentStatus().name())
                .append("bookingStatus", booking.getBookingStatus().name())
                .append("purpose", booking.getPurpose())
                .append("username", booking.getUsername())
                .append("timestamp", new java.util.Date());
        collection.insertOne(doc);
    }

    // ==============================
    // Finished bookings
    // ==============================
    public static synchronized void addFinished(Booking booking) {
        if (booking == null) return;
        finishedQueue.offer(booking);
        writeHistory(booking, "finished");
    }

    public static synchronized List<Booking> listFinishedByCustomer(Customer customer) {
        if (customer == null) return new ArrayList<>();
        List<Booking> result = new ArrayList<>();
        for (Booking b : finishedQueue) {
            if (b.getUsername() != null && b.getUsername().equals(customer.getUsername())) result.add(b);
        }
        return result;
    }

    public static synchronized List<Booking> listFinished() {
        return new ArrayList<>(finishedQueue);
    }

    // ==============================
    // Deleted / Cancelled bookings
    // ==============================
    public static synchronized void addDeleted(Booking booking) {
        if (booking == null) return;
        deletedQueue.offer(booking);
        writeHistory(booking, "cancelled");
    }

    public static synchronized List<Booking> listDeletedByCustomer(Customer customer) {
        if (customer == null) return new ArrayList<>();
        List<Booking> result = new ArrayList<>();
        for (Booking b : deletedQueue) {
            if (b.getUsername() != null && b.getUsername().equals(customer.getUsername())) result.add(b);
        }
        return result;
    }

    public static synchronized List<Booking> listDeleted() {
        return new ArrayList<>(deletedQueue);
    }

    // ==============================
    // Unpaid / Downpayment bookings from active bookings collection
    // ==============================
    public static synchronized List<Booking> listUnpaidFromBookings(Customer customer) {
        if (customer == null) return new ArrayList<>();
        return getBookingsByPaymentStatus(customer.getUserId(), PaymentStatus.UNPAID);
    }

    public static synchronized List<Booking> listDownPaymentsFromBookings(Customer customer) {
        if (customer == null) return new ArrayList<>();
        List<Booking> result = new ArrayList<>();
        MongoCollection<Document> coll = MongoDb.getDatabase().getCollection("bookings");
        for (Document doc : coll.find(new Document("userId", customer.getUserId()))) {
            if (doc.containsKey("downPayment") || doc.containsKey("partialPaid")) {
                Integer id = doc.getInteger("bookingId");
                String purpose = doc.getString("purpose");
                String uname = extractUsername(doc);
                BookingStatus bs = parseBookingStatus(doc.getString("bookingStatus"));
                Booking b = new Booking(id != null ? id : 0, null, null, PaymentStatus.DOWNPAID, bs, purpose, uname);
                result.add(b);
            }
        }
        return result;
    }

    private static List<Booking> getBookingsByPaymentStatus(int userId, PaymentStatus status) {
        List<Booking> result = new ArrayList<>();
        MongoCollection<Document> coll = MongoDb.getDatabase().getCollection("bookings");
        for (Document doc : coll.find(new Document("userId", userId).append("paymentStatus", status.name()))) {
            Integer id = doc.getInteger("bookingId");
            String purpose = doc.getString("purpose");
            String uname = extractUsername(doc);
            BookingStatus bs = parseBookingStatus(doc.getString("bookingStatus"));
            Booking b = new Booking(id != null ? id : 0, null, null, status, bs, purpose, uname);
            result.add(b);
        }
        return result;
    }

    private static String extractUsername(Document doc) {
        if (doc.containsKey("bookedBy") && doc.get("bookedBy") instanceof Document bb) {
            return bb.getString("username");
        }
        return null;
    }

    // ==============================
    // GUI-ready combined methods
    // ==============================
    public static synchronized List<Booking> listAllFinishedAndDeleted(Customer customer) {
        List<Booking> combined = new ArrayList<>();
        combined.addAll(listFinishedByCustomer(customer));
        combined.addAll(listDeletedByCustomer(customer));
        return combined;
    }


}
