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
    if (str.length() == 0) return Collections.emptySet();

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

    restaurantEntity.setProperty("name", getValue(restaurantName, ""));
    restaurantEntity.setProperty("numberOfReviews", getValue(place.userRatingsTotal, 0));
    restaurantEntity.setProperty("rating", getValue(place.rating, 0));
    restaurantEntity.setProperty("tags", getValue(splitStringToSet(restaurantName + " " + reviews), ""));
    restaurantEntity.setProperty("lat", place.geometry.location.lat);
    restaurantEntity.setProperty("lng", place.geometry.location.lng);
    restaurantEntity.setProperty("address", getValue(place.formattedAddress, ""));
    restaurantEntity.setProperty("phone", getValue(place.formattedPhoneNumber, ""));
    restaurantEntity.setProperty("price", getValue(place.priceLevel, "").toString());
    restaurantEntity.setProperty("website", getValue(place.website, "").toString());
    restaurantEntity.setProperty("ID", place.placeId);

    return restaurantEntity;
  } 

  /** return value if not null, else return default value */
  private static <T> T getValue(T value, T defaultValue) {
    return (value == null) ? defaultValue : value;
  }
  
}
