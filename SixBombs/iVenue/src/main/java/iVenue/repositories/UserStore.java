package iVenue.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import iVenue.config.MongoDb;
import iVenue.models.Admin;
import iVenue.models.Customer;
import iVenue.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserStore {

    private static final MongoDatabase DATABASE = MongoDb.getDatabase();
    private static final MongoCollection<Document> COLLECTION = DATABASE.getCollection("users");

    /** Ensure default admin exists */
    public static void ensureAdminExists() {
        Document adminDoc = COLLECTION.find(new Document("userType", "admin")).first();
        if (adminDoc == null) {
            int maxId = 0;
            Document last = COLLECTION.find().sort(new Document("userId", -1)).first();
            if (last != null) maxId = last.getInteger("userId");

            Document doc = new Document("userId", maxId + 1)
                    .append("username", "admin")
                    .append("password", "admin123")
                    .append("userType", "admin")
                    .append("firstName", "System")
                    .append("lastName", "Administrator");
            COLLECTION.insertOne(doc);
            System.out.println("Seeded default admin (username: admin, password: admin123)");
        }
    }

    /** GUI-friendly customer registration */
    public static Customer registerCustomer(Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        String firstName = data.get("firstName");
        String lastName = data.get("lastName");
        String contactNumber = data.get("contactNumber");
        String email = data.get("email");

        // Check duplicate
        Document existing = COLLECTION.find(new Document("username", username)).first();
        if (existing != null) return null;

        int maxId = 0;
        Document last = COLLECTION.find().sort(new Document("userId", -1)).first();
        if (last != null) maxId = last.getInteger("userId");

        Document doc = new Document("userId", maxId + 1)
                .append("username", username)
                .append("password", password)
                .append("userType", "customer")
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("contactNumber", contactNumber)
                .append("email", email);

        COLLECTION.insertOne(doc);

        return new Customer(username, password, maxId + 1, firstName, lastName, contactNumber, email, "customer");
    }

    /** GUI-friendly overload: accept direct fields instead of a Map */
    public static Customer registerCustomer(String username, String password, String firstName, String lastName, String contactNumber, String email) {
        Map<String, String> data = new java.util.HashMap<>();
        data.put("username", username);
        data.put("password", password);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("contactNumber", contactNumber);
        data.put("email", email);

        return registerCustomer(data);
    }

    /** Find user by credentials */
    public static User findByCredentials(String username, String password) {
        Document doc = COLLECTION.find(new Document("username", username)
                .append("password", password)).first();
        if (doc == null) return null;

        String type = doc.getString("userType");
        int id = doc.getInteger("userId");

        if ("customer".equalsIgnoreCase(type)) {
            return new Customer(
                    username,
                    password,
                    id,
                    doc.getString("firstName"),
                    doc.getString("lastName"),
                    doc.getString("contactNumber"),
                    doc.getString("email"),
                    type
            );
        } else if ("admin".equalsIgnoreCase(type)) {
            return new Admin();
        }

        return null;
    }

    /** Cast base User to Customer using MongoDB doc */
    public static Customer castToCustomer(User user, Document doc) {
        if (user == null || doc == null) return null;
        return new Customer(
                user.getUsername(),
                user.getPassword(),
                user.getUserId(),
                doc.getString("firstName"),
                doc.getString("lastName"),
                doc.getString("contactNumber"),
                doc.getString("email"),
                doc.getString("userType")
        );
    }

    /** Get all users (for GUI table) */
    public static List<User> getAll() {
        List<User> out = new ArrayList<>();
        for (Document doc : COLLECTION.find()) {
            String username = doc.getString("username");
            String password = doc.getString("password");
            String type = doc.getString("userType");
            int id = doc.getInteger("userId");

            if ("customer".equalsIgnoreCase(type)) {
                out.add(new Customer(
                        username,
                        password,
                        id,
                        doc.getString("firstName"),
                        doc.getString("lastName"),
                        doc.getString("contactNumber"),
                        doc.getString("email"),
                        type
                ));
            } else if ("admin".equalsIgnoreCase(type)) {
                out.add(new Admin());
            }
        }
        return out;
    }

    /** Get user by ID */
    public static User getById(int userId) {
        Document doc = COLLECTION.find(new Document("userId", userId)).first();
        if (doc == null) return null;
        return findByCredentials(doc.getString("username"), doc.getString("password"));
    }

    /** Delete user by ID */
    public static boolean delete(int userId) {
        Document doc = COLLECTION.find(new Document("userId", userId)).first();
        if (doc == null) return false;
        COLLECTION.deleteOne(new Document("userId", userId));
        return true;
    }
}
