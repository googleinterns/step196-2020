package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.maps.model.PlaceDetails;
import java.util.ArrayList;
import java.util.List;

public final class businessDataUtils {
  public void storeData(
      List<String> restaurantNames,
      String DATABASE_NAME,
      RestaurantDetailsGetter details,
      RestaurantQueryHelper queryHelper) {

    clearDatastore();

    for (String restaurantName : restaurantNames) {
      PlaceDetails place = details.request(restaurantName);

      if (place.geometry == null) continue;

      PlaceDetails.Review[] reviewsArray = place.reviews;
      String reviews = details.getTagsfromReviews(reviewsArray);

      Entity restaurantEntity =
          queryHelper.makeRestaurantEntity(place, restaurantName, reviews, DATABASE_NAME);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(restaurantEntity);
    }
  }

  public void clearDatastore() {
    Query restaurantQuery = new Query("Restaurant");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery allRestaurants = datastore.prepare(restaurantQuery);
    ArrayList<Key> keys = new ArrayList<>();
    for (Entity restaurant : allRestaurants.asIterable()) {
      keys.add(restaurant.getKey());
    }
    datastore.delete(keys);
  }
}
