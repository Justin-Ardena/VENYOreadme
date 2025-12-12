package iVenue.models;

import iVenue.services.BookingAdmin;
import iVenue.services.VenueAdmin;
import iVenue.services.AmenityAdmin;
import iVenue.services.CustomerAdmin;
import iVenue.repositories.UserStore;
import iVenue.models.Customer;
import iVenue.models.User;

import java.util.List;

/**
 * Admin facade/service class for GUI.
 * Handles CRUD operations for bookings, venues, amenities, and customers.
 */
public class Admin extends User{

    private final BookingAdmin bookingAdmin;
    private final VenueAdmin venueAdmin;
    private final AmenityAdmin amenityAdmin;
    private final CustomerAdmin customerAdmin;

    public Admin() {
        super(VerifiedUser.loadAdminFromDb().getUsername(),
                VerifiedUser.loadAdminFromDb().getPassword(),
                VerifiedUser.loadAdminFromDb().getUserId());
        this.bookingAdmin = new BookingAdmin();
        this.venueAdmin = new VenueAdmin();
        this.amenityAdmin = new AmenityAdmin();
        this.customerAdmin = new CustomerAdmin();
    }


    // ==============================
    //       USER MANAGEMENT
    // ==============================

    public List<User> getAllUsers() {
        return UserStore.getAll(); // use repository
    }

    public boolean deleteUser(int userId) {
        // Call UserStore delete, return true if deleted
        if (userId <= 0) return false;
        var doc = UserStore.getById(userId);
        if (doc == null) return false; // user not found
        UserStore.delete(userId); // void method
        return true;
    }

    // ==============================
    //     CUSTOMER MANAGEMENT
    // ==============================

    public List<Customer> getAllCustomers() {
        return customerAdmin.getAll();
    }

    public boolean createCustomer(Customer customer) {
        return customerAdmin.create(customer);
    }

    public boolean updateCustomer(Customer customer) {
        return customerAdmin.update(customer);
    }

    public boolean deleteCustomer(int userId) {
        return customerAdmin.delete(userId);
    }

    public Customer getCustomerById(int userId) {
        return customerAdmin.getById(userId);
    }

    // ==============================
    //     BOOKING MANAGEMENT
    // ==============================

    public boolean createBooking(Booking booking) {
        return bookingAdmin.create(booking);
    }

    public boolean updateBooking(Booking booking) {
        return bookingAdmin.update(booking);
    }

    public boolean deleteBooking(int bookingId) {
        return bookingAdmin.delete(bookingId);
    }

    public List<Booking> getAllBookings() {
        return bookingAdmin.getAll();
    }

    public Booking getBookingById(int bookingId) {
        return bookingAdmin.getById(bookingId);
    }

    // ==============================
    //     VENUE MANAGEMENT
    // ==============================

    public boolean createVenue(Venue venue) {
        return venueAdmin.create(venue);
    }

    public boolean updateVenue(Venue venue) {
        return venueAdmin.update(venue);
    }

    public boolean deleteVenue(int venueId) {
        return venueAdmin.delete(venueId);
    }

    public List<Venue> getAllVenues() {
        return venueAdmin.getAll();
    }

    public Venue getVenueById(int venueId) {
        return venueAdmin.getById(venueId);
    }

    // ==============================
    //    AMENITY MANAGEMENT
    // ==============================

    public boolean createAmenity(Amenity amenity) {
        return amenityAdmin.create(amenity);
    }

    public boolean updateAmenity(Amenity amenity) {
        return amenityAdmin.update(amenity);
    }

    public boolean deleteAmenity(int amenityId) {
        return amenityAdmin.delete(amenityId);
    }

    public List<Amenity> getAllAmenities() {
        return amenityAdmin.getAll();
    }

    public Amenity getAmenityById(int amenityId) {
        return amenityAdmin.getById(amenityId);
    }
}
