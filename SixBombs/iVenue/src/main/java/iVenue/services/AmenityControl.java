package iVenue.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import iVenue.models.Amenity;
import iVenue.config.MongoDb;

import java.util.ArrayList;
import java.util.List;

public class AmenityAdmin implements AdminManagement<Amenity> {

    private final MongoCollection<Document> collection;

    public AmenityAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("amenities");
    }

    @Override
    public boolean create(Amenity amenity) {
        if (amenity == null) return false;

        // Generate new amenityId if not set
        int maxId = 0;
        Document last = collection.find().sort(new Document("amenityId", -1)).first();
        if (last != null) maxId = last.getInteger("amenityId");

        amenity.setAmenityId(maxId + 1);

        Document doc = new Document("amenityId", amenity.getAmenityId())
                .append("name", amenity.getName())
                .append("description", amenity.getDescription())
                .append("quantity", amenity.getQuantity())
                .append("price", amenity.getPrice());

        collection.insertOne(doc);
        return true;
    }

    @Override
    public boolean update(Amenity amenity) {
        if (amenity == null) return false;

        Document doc = collection.find(Filters.eq("amenityId", amenity.getAmenityId())).first();
        if (doc == null) return false;

        Document updateFields = new Document()
                .append("name", amenity.getName())
                .append("description", amenity.getDescription())
                .append("quantity", amenity.getQuantity())
                .append("price", amenity.getPrice());

        collection.updateOne(Filters.eq("amenityId", amenity.getAmenityId()), new Document("$set", updateFields));
        return true;
    }

    @Override
    public boolean delete(int id) {
        Document doc = collection.find(Filters.eq("amenityId", id)).first();
        if (doc == null) return false;

        collection.deleteOne(Filters.eq("amenityId", id));
        return true;
    }

    @Override
    public List<Amenity> getAll() {
        List<Amenity> out = new ArrayList<>();
        for (Document doc : collection.find()) {
            Amenity a = new Amenity(
                    doc.getInteger("amenityId"),
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getInteger("quantity"),
                    doc.getDouble("price")
            );
            out.add(a);
        }
        return out;
    }

    @Override
    public Amenity getById(int id) {
        Document doc = collection.find(Filters.eq("amenityId", id)).first();
        if (doc == null) return null;

        return new Amenity(
                doc.getInteger("amenityId"),
                doc.getString("name"),
                doc.getString("description"),
                doc.getInteger("quantity"),
                doc.getDouble("price")
        );
    }
}
