package iVenue.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDb {
    private static final String CONNECTION_STRING = "mongodb+srv://vincentjohntamano_db_user:CZAngelsBaby1234567891011121314151617181920@cluster1.e8ynseg.mongodb.net/";
    private static final String DATABASE_NAME = "iVenue";
    static MongoClient mongoClient;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
        }
        return database;
    }
}
