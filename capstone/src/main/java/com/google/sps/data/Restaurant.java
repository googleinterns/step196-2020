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
  private final String address;
  private final String phone;
  private final String price;
  private final String website;
  private final String ID;

  public Restaurant(String name, int numberOfReviews, double rating, double lat, double lng, String address, String phone, String price, String website, String ID) {
    this.name = name;
    this.numberOfReviews = numberOfReviews;
    this.rating = rating;
    this.lat = lat;
    this.lng = lng;
    this.address = address;
    this.phone = phone;
    this.price = price;
    this.website = website;
    this.ID = ID;
  }

  public String name() {
    return this.name;
  }

  @Override
  public boolean equals(Object that) {
    return that instanceof Restaurant && this.sameValue((Restaurant) that);
  }

  private boolean sameValue(Restaurant that) {
    return this.name == that.name
        && this.numberOfReviews == that.numberOfReviews 
        && this.rating == that.rating 
        && this.lat == that.lat 
        && this.lng == that.lng 
        && this.address == that.address
        && this.phone == that.phone
        && this.website == that.website
        && this.ID == that.ID;
  }
}
