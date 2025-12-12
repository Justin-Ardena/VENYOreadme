package iVenue.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import iVenue.config.MongoDb;
import iVenue.models.Venue;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class VenueAdmin implements AdminManagement<Venue> {

    private final MongoCollection<Document> collection;

    public VenueAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("venues");
    }

    @Override
    public boolean create(Venue venue) {
        if (venue == null) return false;

        // Get max venueId from MongoDB
        int maxId = 0;
        Document lastVenue = collection.find().sort(new Document("venueId", -1)).first();
        if (lastVenue != null) maxId = lastVenue.getInteger("venueId");

        venue.setVenueId(maxId + 1);

        Document doc = new Document("venueId", venue.getVenueId())
                .append("name", venue.getName())
                .append("description", venue.getDescription())
                .append("capacity", venue.getCapacity())
                .append("availability", venue.isAvailability())
                .append("location", venue.getLocation())
                .append("price", venue.getPrice())
                .append("isFree", venue.isFree());

        collection.insertOne(doc);
        return true;
    }

    @Override
    public boolean update(Venue venue) {
        if (venue == null) return false;

        Document doc = collection.find(new Document("venueId", venue.getVenueId())).first();
        if (doc == null) return false;

        Document updateFields = new Document()
                .append("name", venue.getName())
                .append("description", venue.getDescription())
                .append("capacity", venue.getCapacity())
                .append("availability", venue.isAvailability())
                .append("location", venue.getLocation())
                .append("price", venue.getPrice())
                .append("isFree", venue.isFree());

        collection.updateOne(new Document("venueId", venue.getVenueId()), new Document("$set", updateFields));
        return true;
    }

    @Override
    public boolean delete(int id) {
        Document doc = collection.find(new Document("venueId", id)).first();
        if (doc == null) return false;

        collection.deleteOne(new Document("venueId", id));
        return true;
    }

    @Override
    public List<Venue> getAll() {
        List<Venue> venues = new ArrayList<>();
        for (Document doc : collection.find()) {
            venues.add(documentToVenue(doc));
        }
        return venues;
    }

    @Override
    public Venue getById(int id) {
        Document doc = collection.find(new Document("venueId", id)).first();
        if (doc == null) return null;
        return documentToVenue(doc);
    }

    /** GUI-friendly: fetch by ID and availability filter */
    public Venue getById(int id, boolean onlyIfAvailable) {
        Document query = new Document("venueId", id);
        if (onlyIfAvailable) query.append("availability", true);
        Document doc = collection.find(query).first();
        if (doc == null) return null;
        return documentToVenue(doc);
    }

    /** Helper to convert MongoDB document to Venue object */
    private Venue documentToVenue(Document doc) {
        return new Venue(
                doc.getInteger("venueId"),
                doc.getString("name"),
                doc.getString("description"),
                doc.getInteger("capacity"),
                doc.getBoolean("availability"),
                doc.getString("location"),
                doc.getDouble("price")
        );
    }

    /** Optional console helper for testing */
    public void displayAll() {
        System.out.println("---- ALL VENUES ----");
        for (Venue v : getAll()) {
            String availability = v.isAvailability() ? "Available" : "Booked";
            String priceLabel = v.isFree() ? "FREE" : "₱" + v.getPrice();
            System.out.println("ID: " + v.getVenueId());
            System.out.println("Name: " + v.getName());
            System.out.println("Description: " + v.getDescription());
            System.out.println("Capacity: " + v.getCapacity());
            System.out.println("Availability: " + availability);
            System.out.println("Location: " + v.getLocation());
            System.out.println("Price: " + priceLabel);
            System.out.println("---------------------------");
        }
    }
}
