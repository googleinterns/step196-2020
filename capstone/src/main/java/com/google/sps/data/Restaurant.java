package com.google.sps.data;

/**
 * Class representing a restaurant
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Restaurant {
  private final String name;
  private final int numberOfReviews;
  private final double rating;
  private final double lat;
  private final double lng;
  private final String formattedAddress;
  private final String formattedPhoneNumber;
  private final String priceLevel;
  private final String website;
  private final String placeId;
  private final String vicinity;
  private final boolean openStatus;

  public Restaurant(String name, int numberOfReviews, double rating, double lat, double lng, String address, String phone, String price, String website, String ID, String vicinity) {
    this.name = name;
    this.numberOfReviews = numberOfReviews;
    this.rating = rating;
    this.lat = lat;
    this.lng = lng;
    this.formattedAddress = address;
    this.formattedPhoneNumber = phone;
    this.priceLevel = price;
    this.website = website;
    this.placeId = ID;
    this.vicinity = vicinity; 
    this.openStatus = true;
  }

  public String name() {
    return this.name;
  }
}
