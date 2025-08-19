package com.s23010409.parknow;

/**
 * ParkingSpotModel
 * A data model class to represent a single parking spot.
 * This class holds properties like the spot's name (ID), location coordinates,
 * current availability, price per hour, and a brief description.
 */
public class ParkingSpotModel {

    private String spotName; // Unique identifier for the parking spot (e.g., "Colombo City Center Parking")
    private double latitude;
    private double longitude;
    private int availability; // Number of available slots
    private double pricePerHour;
    private String description;

    /**
     * Constructor for ParkingSpotModel.
     *
     * @param spotName       The unique name or ID of the parking spot.
     * @param latitude       The latitude coordinate of the parking spot.
     * @param longitude      The longitude coordinate of the parking spot.
     * @param availability   The number of parking slots currently available.
     * @param pricePerHour   The price to park per hour at this spot.
     * @param description    A brief description of the parking spot.
     */
    public ParkingSpotModel(String spotName, double latitude, double longitude,
                            int availability, double pricePerHour, String description) {
        this.spotName = spotName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availability = availability;
        this.pricePerHour = pricePerHour;
        this.description = description;
    }

    // --- Getters ---

    public String getSpotName() {
        return spotName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getAvailability() {
        return availability;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public String getDescription() {
        return description;
    }

    // --- Setters (optional, depending on if you need to modify these after creation) ---

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Provides a string representation of the ParkingSpotModel, useful for debugging.
     * @return A string containing the spot's name, availability, and price.
     */
    @Override
    public String toString() {
        return "Spot: " + spotName +
                ", Available: " + availability +
                ", Price: $" + String.format("%.2f", pricePerHour) + "/hr";
    }
}
