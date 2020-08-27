package com.google.sps.data;

import com.google.maps.model.PlaceDetails;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import com.google.sps.data.Restaurant;


public final class RestaurantQueryHelper {
  public static int MAX_RESULTS = 20;

  /**
   * @return str split on word boundries, 
   * transformed to all lowercase and only including alphabetic characters
   */
  public Set<String> splitStringToSet(String str) {
    String[] strArray = str.toLowerCase().split("\\P{Alpha}+");
    Set<String> strSet = new HashSet<>();
    strSet.addAll(Arrays.asList(strArray)); 
    return strSet;
  }

  /**
   * @return restaurant object with fields of restauarant entity,
   * or default value if parameter not specified
   */
  public Restaurant makeRestaurantObject(Entity RestaurantEntity) throws NullPointerException {
    String name = (String) RestaurantEntity.getProperty("name");
    int numberOfReviews = ((Long) RestaurantEntity.getProperty("numberOfReviews")).intValue();
    double rating = (double) RestaurantEntity.getProperty("rating");
    double lat = (double) RestaurantEntity.getProperty("lat");
    double lng = (double) RestaurantEntity.getProperty("lng");
    String address = (String) RestaurantEntity.getProperty("address");
    String phone = (String) RestaurantEntity.getProperty("phone");
    String price = (String) RestaurantEntity.getProperty("price");
    String website = (String) RestaurantEntity.getProperty("website");
    String ID = (String) RestaurantEntity.getProperty("ID");

    Restaurant restaurant = new Restaurant(name, numberOfReviews, rating, lat, lng, address, phone, price, website, ID);
    return restaurant;
  }

  public Entity makeRestaurantEntity(PlaceDetails place, String restaurantName, String reviews, String databaseName) {
    Entity restaurantEntity = new Entity(databaseName);
    
    try {
      int numberOfReviews = place.userRatingsTotal; 
      double rating = place.rating;
      Set<String> tags = splitStringToSet(restaurantName + " " + reviews);
      double lat = place.geometry.location.lat;
      double lng = place.geometry.location.lng;
      String address = place.formattedAddress;
      String phone = place.formattedPhoneNumber;
      String price = place.priceLevel.toString();
      String website = place.website.toString();
      String placeID = place.placeId;

      restaurantEntity.setProperty("name", restaurantName);
      restaurantEntity.setProperty("numberOfReviews", numberOfReviews);
      restaurantEntity.setProperty("rating", rating);
      restaurantEntity.setProperty("tags", tags);
      restaurantEntity.setProperty("lat", lat);
      restaurantEntity.setProperty("lng", lng);
      restaurantEntity.setProperty("address", address);
      restaurantEntity.setProperty("phone", phone);
      restaurantEntity.setProperty("price", price);
      restaurantEntity.setProperty("website", website);
      restaurantEntity.setProperty("ID", placeID);
    } 
    catch (NullPointerException e) {}

    return restaurantEntity;
  } 

}
