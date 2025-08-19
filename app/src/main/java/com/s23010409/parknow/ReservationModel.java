package com.s23010409.parknow;

/**
 * ReservationModel
 * A data model class to represent a single parking reservation made by a user.
 * This class holds properties like the reservation ID (if applicable),
 * the ID of the user who made the reservation, the ID of the parking spot,
 * and the timestamp when the reservation was made.
 */
public class ReservationModel {

    private int id; // Primary key for the reservation in the database
    private int userId;
    private String spotId; // Name or unique identifier of the parking spot
    private String timestamp; // Date and time of the reservation

    /**
     * Constructor for creating a new ReservationModel object.
     * Used when retrieving data from the database, including the ID.
     *
     * @param id          The unique ID of the reservation.
     * @param userId      The ID of the user who made the reservation.
     * @param spotId      The unique identifier of the reserved parking spot.
     * @param timestamp   The timestamp when the reservation was made (e.g., "YYYY-MM-DD HH:MM:SS").
     */
    public ReservationModel(int id, int userId, String spotId, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.spotId = spotId;
        this.timestamp = timestamp;
    }

    /**
     * Constructor for creating a new ReservationModel object without an ID.
     * Used when inserting a new reservation into the database, where the ID is auto-generated.
     *
     * @param userId      The ID of the user who made the reservation.
     * @param spotId      The unique identifier of the reserved parking spot.
     * @param timestamp   The timestamp when the reservation was made (e.g., "YYYY-MM-DD HH:MM:SS").
     */
    public ReservationModel(int userId, String spotId, String timestamp) {
        this.userId = userId;
        this.spotId = spotId;
        this.timestamp = timestamp;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getSpotId() {
        return spotId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // --- Setters (optional, if you need to modify these after creation) ---

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Provides a string representation of the ReservationModel, useful for debugging or display.
     * @return A string containing reservation details.
     */
    @Override
    public String toString() {
        return "Reservation ID: " + id +
                ", User ID: " + userId +
                ", Spot: " + spotId +
                ", Time: " + timestamp;
    }
}
