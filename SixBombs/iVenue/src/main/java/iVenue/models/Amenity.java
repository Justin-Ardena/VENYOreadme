package iVenue.models;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import iVenue.config.MongoDb;
import java.util.ArrayList;
import java.util.List;

public class Amenity {

    private int amenityId;
    private String name;
    private String description;
    private int quantity;
    private double price;

    public Amenity(int amenityId, String name, String description, int quantity, double price) {
        this.amenityId = amenityId;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }

    // === Getters and Setters ===
    public int getAmenityId() { return amenityId; }
    public void setAmenityId(int amenityId) { this.amenityId = amenityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // === GUI-ready list of all amenities ===
    public static List<Amenity> listAmenities() {
        List<Amenity> amenities = new ArrayList<>();
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("amenities");

        for (Document doc : collection.find()) {
            amenities.add(new Amenity(
                    doc.getInteger("amenityId"),
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getInteger("quantity"),
                    doc.getDouble("price")
            ));
        }
        return amenities;
    }

    // === Get single amenity by ID ===
    public static Amenity getAmenity(int amenityId) {
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("amenities");
        Document doc = collection.find(new Document("amenityId", amenityId)).first();
        if (doc != null) {
            return new Amenity(
                    doc.getInteger("amenityId"),
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getInteger("quantity"),
                    doc.getDouble("price")
            );
        }
        return null;
    }

    // === Check availability by quantity ===
    public static boolean isAvailable(int amenityId, int requiredQuantity) {
        Amenity a = getAmenity(amenityId);
        return a != null && a.getQuantity() >= requiredQuantity;
    }

}
