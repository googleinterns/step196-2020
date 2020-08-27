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

  public Restaurant() {
    this.name = "";
    this.numberOfReviews = 0;
    this.rating = 0;
    this.lat = 0;
    this.lng = 0;
    this.address = "";
    this.phone = "";
    this.price = "";
    this.website = "";
    this.ID = "";
  }

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
}
