package com.google.sps;

import com.google.sps.data.BusinessDataUtils;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.RestaurantQueryHelper;
import com.google.sps.data.Restaurant;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.maps.model.PlaceDetails;
import java.util.ArrayList;
import java.util.List;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.maps.PlacesApi;
import com.google.maps.model.PriceLevel;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import java.net.URL;
import java.net.MalformedURLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/** Testing Strategy / Partitioning input(s) and output(s) for testing
 *    storeData(List<String> restaurantNames, String DATABASE_NAME, RestaurantDetailsGetter details, RestaurantQueryHelper queryHelper)
 *          restaurantNames: contains random restaurant names
 *          DATABASE_NAME: either "BlackOwnedRestuarants" or "SmallRestaurants"
 *          details: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          queryHelper: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          return: datastore should have stored these restaruant names with same query DATABASE_NAME
 *
 *    clearDatastore(String DATABASE_NAME)
 *          DATABASE_NAME: either "BlackOwnedRestuarants" or "SmallRestaurants"
 *          return: datastore should have no entities with query DATABASE_NAME
 *    
 *    updateData(List<String> restaurantNames, String DATABASE_NAME, RestaurantDetailsGetter details, RestaurantQueryHelper queryHelper)
 *          restaurantNames: contains random restaurant names
 *          DATABASE_NAME: either "BlackOwnedRestuarants" or "SmallRestaurants"
 *          details: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          queryHelper: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          return: datastore should have stored these restaruant names with same query DATABASE_NAME w/ no duplicates
 *                  restaurants not in new list should not be stored
 */

@RunWith(JUnit4.class)
public final class BusinessDataUtilsTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private BusinessDataUtils utility;
  private RestaurantQueryHelper query;
  private RestaurantDetailsGetter details;
  private String DATABASE_NAME;
  private List<String> smallRestaurantNames = new ArrayList<String>();
  private List<String> blackRestaurantNames = new ArrayList<String>();

  @Before
  public void setUp() {
    helper.setUp();
    query = new RestaurantQueryHelper();
    utility = new BusinessDataUtils();
    details = new RestaurantDetailsGetter();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // adding 2 small-business restaurants
  // and 1 black-owned restaurant to datastore
  // and making sure they are stored with distinction based on type
  @Test
  public void testStoreSmallAndBlackOwnedRestaurantsIntoDatastore() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = "SmallRestaurants";
    smallRestaurantNames.add("Restaurant 1");
    smallRestaurantNames.add("Restaurant 2");
    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfSmallRestaurantsStored = 2;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();
    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);

    DATABASE_NAME = "BlackOwnedRestuarants";
    blackRestaurantNames.add("Restaurant 3");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 1;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();
    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
  }

  // adding 2 small-business restaurants to datastore
  // checking if both restaurants are cleared after clearDatastore() call
  @Test
  public void testClearSmallBusinessFromDatastore() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = "SmallRestaurants";
    smallRestaurantNames.add("Restaurant 1");
    smallRestaurantNames.add("Restaurant 2");

    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    utility.clearDatastore(DATABASE_NAME);
    int expectedNumberOfSmallRestaurantsStored = 0;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);
  }

  // adding 2 black-owned-business restaurants
  // and 1 small business restaurant to datastore
  // removing all small business restaurant
  // making sure all black-owned-business restaurants still remain
  @Test
  public void testClearOnlySmallBusinessFromDatastore() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = "BlackOwnedRestuarants";
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 2;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    DATABASE_NAME = "SmallRestaurants";
    smallRestaurantNames.add("Restaurant 3");
    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    utility.clearDatastore(DATABASE_NAME);
    int expectedNumberOfSmallRestaurantsStored = 0;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);
  }

  // adding 2 black-owned-business restaurants
  // and 1 small business restaurant to datastore
  // updating small business restaurants with 3 more restaurants 
  // with 1 of the 3 newly added the exisiting small business restaurant
  // making sure there are only 3 small business restaurants in datastore w/ no duplicates
  // and still 2 black-owned-business restaurants in datastore
  @Test
  public void testUpdateSmallBusinessFromDatastoreViaAddition() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = "BlackOwnedRestuarants";
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 2;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    DATABASE_NAME = "SmallRestaurants";
    smallRestaurantNames.add("Restaurant 3");
    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    smallRestaurantNames.clear();
    smallRestaurantNames.add("Restaurant 3");
    smallRestaurantNames.add("Restaurant 4");
    smallRestaurantNames.add("Restaurant 5");
    utility.updateData(smallRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfSmallRestaurantsStored = 3;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    PreparedQuery allSmallRestaurants = ds.prepare(new Query(DATABASE_NAME));
    HashSet<Entity> seenSmallRestaurants = new HashSet<Entity>();
    boolean expectedHasDuplicatedSmallRestaurants = false;
    boolean actualHasDuplicatedSmallRestaurants = false;

    for(Entity smallRestaurant : allSmallRestaurants.asIterable()) {
      if(seenSmallRestaurants.contains(smallRestaurant)) {
        actualHasDuplicatedSmallRestaurants = true;
      } else {
        seenSmallRestaurants.add(smallRestaurant);
      }
    }

    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);
    Assert.assertEquals(expectedHasDuplicatedSmallRestaurants, actualHasDuplicatedSmallRestaurants);
  }

  // adding 4 black-owned-business restaurants
  // and 1 small business restaurant to datastore
  // updating black-owned-business restaurants with removal of 2 existing restaurants 
  // making sure there is only 2 black-owned-business restaurants in datastore w/ no duplicates
  // and still 1 small business restaurants in datastore
  @Test
  public void testUpdateBlackOwnedBusinessFromDatastoreViaRemoval() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = "BlackOwnedRestuarants";
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    blackRestaurantNames.add("Restaurant 3");
    blackRestaurantNames.add("Restaurant 4");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    blackRestaurantNames.remove(blackRestaurantNames.size()-1);
    blackRestaurantNames.remove(blackRestaurantNames.size()-1);
    utility.updateData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 2;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    PreparedQuery allBlackRestaurants = ds.prepare(new Query(DATABASE_NAME));
    HashSet<Entity> seenBlackRestaurants = new HashSet<Entity>();
    boolean expectedHasDuplicatedBlackRestaurants = false;
    boolean actualHasDuplicatedBlackRestaurants = false;

    for(Entity blackRestaurant : allBlackRestaurants.asIterable()) {
      if(seenBlackRestaurants.contains(blackRestaurant)) {
        actualHasDuplicatedBlackRestaurants = true;
      } else {
        seenBlackRestaurants.add(blackRestaurant);
      }
    }

    DATABASE_NAME = "SmallRestaurants";
    smallRestaurantNames.add("Restaurant 3");
    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfSmallRestaurantsStored = 1;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);
    Assert.assertEquals(expectedHasDuplicatedBlackRestaurants, actualHasDuplicatedBlackRestaurants);
  }
}
