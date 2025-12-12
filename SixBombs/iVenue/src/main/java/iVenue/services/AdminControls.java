package iVenue.services;

import java.util.List;

/**
 * Generic interface for Admin CRUD operations.
 * Fully GUI-ready: object-based, no Scanner.
 *
 * @param <T> the type of entity managed (Customer, Booking, Venue, Amenity, User, etc.)
 */
public interface AdminManagement<T> {

    /**
     * Create a new entity.
     *
     * @param entity the object to create
     * @return true if successful, false otherwise
     */
    boolean create(T entity);

    /**
     * Update an existing entity.
     *
     * @param entity the object with updated fields
     * @return true if successful, false otherwise
     */
    boolean update(T entity);

    /**
     * Delete an entity by its ID.
     *
     * @param id the unique identifier of the entity
     * @return true if successful, false otherwise
     */
    boolean delete(int id);

    /**
     * Get all entities (for table or list views in GUI).
     *
     * @return list of all entities
     */
    List<T> getAll();

    /**
     * Get an entity by its ID.
     *
     * @param id the unique identifier
     * @return the entity object, or null if not found
     */
    T getById(int id);
}
