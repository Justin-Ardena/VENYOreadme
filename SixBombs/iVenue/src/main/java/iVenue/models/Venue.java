package iVenue.models;

import com.mongodb.client.MongoCollection;
import iVenue.config.MongoDb;
import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Venue {
    private int venueId;
    private String name;
    private String description;
    private int capacity;
    private boolean availability;
    private String location;
    private double price;
    private boolean isFree;

    public Venue(int venueId, String name, String description, int capacity, boolean availability, String location, double price) {
        this.venueId = venueId;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.availability = availability;
        this.location = location;
        this.price = Math.max(price, 0);
        this.isFree = (this.price == 0);
    }

    // ================= Getters & Setters =================
    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = Math.max(price, 0); this.isFree = (this.price == 0); }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public String getPriceLabel() { return (price == 0) ? "FREE" : "₱" + price; }

    // ================= MongoDB Helper Methods =================

    /**
     * Fetch booked dates for this venue from the bookings collection.
     * Returns a List of LocalDate.
     */
    public List<LocalDate> getBookedDates() {
        List<LocalDate> bookedDates = new ArrayList<>();
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Document doc : collection.find(new Document("venueId", this.venueId))) {
            if (doc.containsKey("date")) {
                String dateStr = doc.getString("date").split(" ")[0]; // yyyy-MM-dd
                bookedDates.add(LocalDate.parse(dateStr, formatter));
            }
        }

        return bookedDates;
    }
}
