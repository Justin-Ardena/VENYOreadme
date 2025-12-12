package iVenue.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import iVenue.config.MongoDb;
import iVenue.models.Customer;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomerAdmin service handles all CRUD operations for customers.
 * Fully GUI-ready: no Scanner; methods accept Customer objects or IDs.
 */
public class CustomerAdmin implements AdminManagement<Customer> {

    private final MongoCollection<Document> collection;

    public CustomerAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("users"); // all users in one collection
    }

    // ==============================
    // CREATE CUSTOMER
    // ==============================
    @Override
    public boolean create(Customer customer) {
        if (customer == null) return false;

        try {
            int maxId = 0;
            Document lastCustomer = collection.find().sort(new Document("userId", -1)).first();
            if (lastCustomer != null) maxId = lastCustomer.getInteger("userId");
            customer.setUserId(maxId + 1);

            Document doc = new Document("userId", customer.getUserId())
                    .append("username", customer.getUsername())
                    .append("password", customer.getPassword())
                    .append("userType", "customer")
                    .append("firstName", customer.getFirstName())
                    .append("lastName", customer.getLastName())
                    .append("email", customer.getEmail())
                    .append("contactNumber", customer.getContactNumber());

            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // UPDATE CUSTOMER
    // ==============================
    @Override
    public boolean update(Customer customer) {
        if (customer == null) return false;

        try {
            Document existing = collection.find(new Document("userId", customer.getUserId())).first();
            if (existing == null) return false;

            Document updates = new Document()
                    .append("username", customer.getUsername())
                    .append("password", customer.getPassword())
                    .append("firstName", customer.getFirstName())
                    .append("lastName", customer.getLastName())
                    .append("email", customer.getEmail())
                    .append("contactNumber", customer.getContactNumber());

            collection.updateOne(new Document("userId", customer.getUserId()), new Document("$set", updates));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // DELETE CUSTOMER
    // ==============================
    @Override
    public boolean delete(int userId) {
        try {
            Document existing = collection.find(new Document("userId", userId)).first();
            if (existing == null) return false;

            // Prevent deleting admin
            if ("admin".equalsIgnoreCase(existing.getString("userType"))) return false;

            collection.deleteOne(new Document("userId", userId));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // GET ALL CUSTOMERS
    // ==============================
    @Override
    public List<Customer> getAll() {
        List<Customer> customers = new ArrayList<>();
        for (Document doc : collection.find(new Document("userType", "customer"))) {
            customers.add(documentToCustomer(doc));
        }
        return customers;
    }

    // ==============================
    // GET CUSTOMER BY ID
    // ==============================
    @Override
    public Customer getById(int userId) {
        Document doc = collection.find(new Document("userId", userId).append("userType", "customer")).first();
        return documentToCustomer(doc);
    }

    // ==============================
    // HELPER: Convert Document -> Customer
    // ==============================
    private Customer documentToCustomer(Document doc) {
        if (doc == null) return null;
        return new Customer(
                doc.getString("username"),
                doc.getString("password"),
                doc.getInteger("userId"),
                doc.getString("firstName"),
                doc.getString("lastName"),
                doc.getString("contactNumber"),
                doc.getString("email"),
                doc.getString("userType")
        );
    }
}
