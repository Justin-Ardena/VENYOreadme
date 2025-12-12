package iVenue.models;
import iVenue.services.VenueAdmin;


import iVenue.config.MongoDb;
import iVenue.repositories.UserStore;
import org.bson.Document;

import java.util.List;

public class Customer extends User implements Payment {
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String email;
    private String userType;

    public Customer(String username, String password, int userId,
                    String firstName, String lastName,
                    String contactNumber, String email, String userType) {
        super(username, password, userId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.userType = userType;
    }

    // ==============================
    // GETTERS & SETTERS
    // ==============================
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }
    public String getUserType() { return userType; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) {
        if (!email.endsWith("@gmail.com")) throw new IllegalArgumentException("Email must end with @gmail.com");
        this.email = email;
    }
    public void setContactNumber(String contact) {
        if (!contact.matches("\\+?\\d+")) throw new IllegalArgumentException("Contact number invalid");
        this.contactNumber = contact;
    }
    public void setUserType(String userType) { this.userType = userType; }

    // ==============================
    // CUSTOMER ACTIONS (GUI)
    // ==============================
    public Booking createBooking(Venue venue, List<Booking.AmenitySelection> selectedAmenities,
                                 String purpose, boolean payNow, boolean lockVenue) {
        return Booking.createBookingGUI(this, venue, selectedAmenities, purpose, payNow, lockVenue);
    }

    public boolean cancelBooking(int bookingId) {
        return Booking.cancelBookingGUI(this, bookingId);
    }

    public boolean payBooking(int bookingId, boolean fullPayment) {
        return Booking.payBookingGUI(this, bookingId, fullPayment);
    }

    public List<Document> viewBookings() {
        return Booking.viewBookingsGUI(this);
    }

    // ==============================
    // CUSTOMER REGISTRATION (GUI)
    // ==============================
    public static Customer registerCustomerGUI(String username, String password,
                                               String firstName, String lastName,
                                               String contactNumber, String email) {
        // Validation
        if (username == null || username.isEmpty()) return null;
        if (password == null || password.length() < 6) return null;
        if (firstName == null || firstName.isEmpty()) return null;
        if (lastName == null || lastName.isEmpty()) return null;
        if (contactNumber == null || !contactNumber.matches("\\d{7,}")) return null;
        if (email == null || !email.contains("@") || !email.contains(".")) return null;

        return UserStore.registerCustomer(username, password, firstName, lastName, contactNumber, email);
    }

    // ==============================
    // PROFILE UPDATE (GUI)
    // ==============================
    public static void updateProfileGUI(Customer customer,
                                        String firstName, String lastName,
                                        String contactNumber, String email,
                                        String username, String password) {
        if (customer == null) return;

        if (firstName != null && !firstName.isEmpty()) customer.setFirstName(firstName);
        if (lastName != null && !lastName.isEmpty()) customer.setLastName(lastName);
        if (contactNumber != null && contactNumber.matches("\\d{7,}")) customer.setContactNumber(contactNumber);
        if (email != null && email.contains("@") && email.contains(".")) customer.setEmail(email);
        if (username != null && !username.isEmpty()) customer.setUsername(username);
        if (password != null && password.length() >= 6) customer.setPassword(password);

        Document updates = new Document()
                .append("firstName", customer.getFirstName())
                .append("lastName", customer.getLastName())
                .append("contactNumber", customer.getContactNumber())
                .append("email", customer.getEmail())
                .append("username", customer.getUsername())
                .append("password", customer.getPassword());

        MongoDb.getDatabase().getCollection("users")
                .updateOne(new Document("userId", customer.getUserId()),
                        new Document("$set", updates));
    }

    // ==============================
    // PAYMENT CALCULATION
    // ==============================
    @Override
    public double calculatePayment(int bookingId) {
        var collection = MongoDb.getDatabase().getCollection("bookings");
        var doc = collection.find(new Document("bookingId", bookingId)).first();
        if (doc == null) return 0;

        double total = 0;

        Integer venueId = doc.getInteger("venueId");
        if (venueId != null) {
            VenueAdmin venueAdmin = new VenueAdmin();
            Venue v = venueAdmin.getById(venueId);
            if (v != null) total += v.getPrice();
        }

        if (doc.containsKey("amenities")) {
            @SuppressWarnings("unchecked")
            List<Document> amenities = (List<Document>) doc.get("amenities");
            for (Document aDoc : amenities) {
                double price = aDoc.getDouble("price") != null ? aDoc.getDouble("price") : 0;
                total += price;
            }
        }

        return total;
    }

}
