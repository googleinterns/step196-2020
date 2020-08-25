package com.google.sps.data;

import com.google.maps.model.PlaceDetails;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.RestaurantQueryHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.sps.data.Restaurant;


public final class RestaurantQueryHelper {
  public int MAX_RESULTS = 20;
  private ArrayList<Restaurant> result = new ArrayList<>(); 

  public boolean restaurantContainsKeyword( Entity RestaurantEntity, String keywordsCombinedString) {
    Set<String> keywords = splitStringToSet(keywordsCombinedString);

    List<String> restauarantTagsAsList = (List<String>) RestaurantEntity.getProperty("tags");
    Set<String> currRestaurantTags = new HashSet<String>(); 
    currRestaurantTags.addAll(restauarantTagsAsList); 

    if (!Collections.disjoint(currRestaurantTags, keywords)) {
      return true;
    }
    else return false;
  }

  /**
   * @return all lowercase set form of string with alphabetic characters
   */
  public Set<String> splitStringToSet(String str) {
    String[] strArray = str.toLowerCase().split("\\P{Alpha}+");
    Set<String> strSet = new HashSet<>();
    strSet.addAll(Arrays.asList(strArray)); 
    return strSet;
  }

  /**
   * @return restaurant object with fields of restauarant entity, or default value if parameter not specified
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