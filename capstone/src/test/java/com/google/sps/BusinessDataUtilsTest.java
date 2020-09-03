package com.google.sps;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.BusinessDataUtils;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.RestaurantQueryHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** 
 * Testing Strategy / Partitioning input(s) and output(s) for testing
 *    storeData(List<String> restaurantNames, String DATABASE_NAME, RestaurantDetailsGetter details, RestaurantQueryHelper queryHelper)
 *          restaurantNames: contains random restaurant names
 *          DATABASE_NAME: either "BlackOwnedRestaurants" or "SmallRestaurants"
 *          details: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          queryHelper: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          return: datastore should have stored these restaurant names with same query DATABASE_NAME
 *
 *    clearDatastore(String DATABASE_NAME)
 *          DATABASE_NAME: either "BlackOwnedRestaurants" or "SmallRestaurants"
 *          return: datastore should have no entities with query DATABASE_NAME
 *    
 *    updateData(List<String> restaurantNames, String DATABASE_NAME, RestaurantDetailsGetter details, RestaurantQueryHelper queryHelper)
 *          restaurantNames: contains random restaurant names
 *          DATABASE_NAME: either "BlackOwnedRestaurants" or "SmallRestaurants"
 *          details: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          queryHelper: all fields filled w/ mixture of default and non-default values based on restaurant name
 *          return: datastore should have stored these restaurant names with same query DATABASE_NAME w/ no duplicates
 *                  restaurants not in new list should not be stored
 */

@RunWith(JUnit4.class)
public final class BusinessDataUtilsTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private BusinessDataUtils utility;
  private RestaurantQueryHelper query;
  private RestaurantDetailsGetter details;
  private String DATABASE_NAME;
  private List<String> smallRestaurantNames = new ArrayList<String>();
  private List<String> blackRestaurantNames = new ArrayList<String>();
  private static final String SMALL_RESTAURANTS_DB_NAME = "SmallRestaurants";
  private static final String BLACK_OWNED_RESTAURANTS_DB_NAME = "BlackOwnedRestaurants";

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
  public void testStoreData_SmallAndBlackOwnedRestaurants() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = SMALL_RESTAURANTS_DB_NAME;
    smallRestaurantNames.add("Restaurant 1");
    smallRestaurantNames.add("Restaurant 2");
    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfSmallRestaurantsStored = 2;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();
    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);

    DATABASE_NAME = BLACK_OWNED_RESTAURANTS_DB_NAME;
    blackRestaurantNames.add("Restaurant 3");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 1;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();
    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
  }

  // adding 2 small-business restaurants to datastore
  // checking if both restaurants are cleared after clearDatastore() call
  @Test
  public void testClearDatastore_SmallBusiness() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = SMALL_RESTAURANTS_DB_NAME;
    smallRestaurantNames.add("Restaurant 1");
    smallRestaurantNames.add("Restaurant 2");

    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    utility.clearDatastore(DATABASE_NAME);
    int expectedNumberOfSmallRestaurantsStored = 0;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);
  }

  // adding 3 black-owned-business restaurants to datastore
  // checking if all restaurants are cleared after clearDatastore() call
  @Test
  public void testClearDatastore_BlackOwnedBusiness() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = BLACK_OWNED_RESTAURANTS_DB_NAME;
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    blackRestaurantNames.add("Restaurant 3");

    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    utility.clearDatastore(DATABASE_NAME);
    int expectedNumberOfBlackOwnedRestaurantsStored = 0;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
  }

  // adding 2 black-owned-business restaurants
  // and 1 small business restaurant to datastore
  // removing all small business restaurant
  // making sure all black-owned-business restaurants still remain
  @Test
  public void testClearDatastore_OnlySmallBusiness() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = BLACK_OWNED_RESTAURANTS_DB_NAME;
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 2;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    DATABASE_NAME = SMALL_RESTAURANTS_DB_NAME;
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
  // removing all restaurants
  // making sure no restaurants still remain
  @Test
  public void testClearDatastore_SmallandBlackOwnedBusiness() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = BLACK_OWNED_RESTAURANTS_DB_NAME;
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    utility.clearDatastore(DATABASE_NAME);
    int expectedNumberOfBlackOwnedRestaurantsStored = 0;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    DATABASE_NAME = SMALL_RESTAURANTS_DB_NAME;
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
  public void testUpdateData_AdditionalSmallBusinesses() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = BLACK_OWNED_RESTAURANTS_DB_NAME;
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfBlackOwnedRestaurantsStored = 2;
    int actualNumberOfBlackOwnedRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    DATABASE_NAME = SMALL_RESTAURANTS_DB_NAME;
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
  public void testUpdateData_RemovedBlackOwnedBusiness() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    String DATABASE_NAME = BLACK_OWNED_RESTAURANTS_DB_NAME;
    blackRestaurantNames.add("Restaurant 1");
    blackRestaurantNames.add("Restaurant 2");
    blackRestaurantNames.add("Restaurant 3");
    blackRestaurantNames.add("Restaurant 4");
    utility.storeData(blackRestaurantNames, DATABASE_NAME, details, query);
    blackRestaurantNames.remove(blackRestaurantNames.size() - 1);
    blackRestaurantNames.remove(blackRestaurantNames.size() - 1);
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

    DATABASE_NAME = SMALL_RESTAURANTS_DB_NAME;
    smallRestaurantNames.add("Restaurant 3");
    utility.storeData(smallRestaurantNames, DATABASE_NAME, details, query);
    int expectedNumberOfSmallRestaurantsStored = 1;
    int actualNumberOfSmallRestaurantsStored = ds.prepare(new Query(DATABASE_NAME)).countEntities();

    Assert.assertEquals(expectedNumberOfBlackOwnedRestaurantsStored, actualNumberOfBlackOwnedRestaurantsStored);
    Assert.assertEquals(expectedNumberOfSmallRestaurantsStored, actualNumberOfSmallRestaurantsStored);
    Assert.assertEquals(expectedHasDuplicatedBlackRestaurants, actualHasDuplicatedBlackRestaurants);
  }
}
