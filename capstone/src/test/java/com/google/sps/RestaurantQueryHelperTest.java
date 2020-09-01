package com.google.sps;

import com.google.sps.data.RestaurantQueryHelper;
import com.google.sps.data.Restaurant;
import com.google.maps.model.PlaceDetails;
import com.google.appengine.api.datastore.Entity;
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
 *    makeRestaurantObject(Entity RestaurantEntity):
 *          RestaurantEntity: has all fields w non-default values, fields have mixture of default and non-default values
 *          return: has all fields w non-default values, fields have mixture of default and non-default values
 *
 *    makeRestaurantEntity(PlaceDetails place, String restaurantName, String reviews, String databaseName):
 *          place: all fields filled, some fields null (Note: place input will not have all fields null, so do not have to test for this)
 *          return: Entity has all fields w non-default values, fields have mixture of default and non-default values
 *
 *    splitStringToSet(String str):
 *          str: num of words str contains = 0, >0
 *               str contains only alphabetic characters, contains both alphabetic and non-alphabetic characters, contains only non-alphabetic characters
 *               str contains all lowercase letters, contains both lowercase and uppercase letters, contains only uppercase letters
 *               location of non-alphabetic and uppercase letters: beginning, middle, end 
 *          return: size() = 0, >0
 */

@RunWith(JUnit4.class)
public final class RestaurantQueryHelperTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private RestaurantQueryHelper query;

  private static String DEFAULT_STRING = "";
  private static double DEFAULT_NUM = 0.0;

  private static String DATABASE = "database";
  private static String REVIEWS = "reviews";
  private static String NAME = "name";
  private static int NUM_REVIEWS = 100;
  private static double RATING = 5.0;
  private static double LAT = 100.0;
  private static double LNG = 200.0;
  private static String ADDRESS = "address";
  private static String PHONE = "phone";
  private static PriceLevel PRICE = PriceLevel.MODERATE;
  private static String WEBSITE = "https://developers.google.com/";
  private static String ID = "placeID";

  @Before
  public void setUp() {
    helper.setUp();
    query = new RestaurantQueryHelper();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testMakeRestaurantObject() {
    // makeRestaurantObject(): entity fields should equal Restaurant class fields
    Entity entity = new Entity("test");
    entity.setProperty("name", NAME);
    entity.setProperty("numberOfReviews", NUM_REVIEWS);
    entity.setProperty("rating", DEFAULT_NUM);
    entity.setProperty("lat", LAT);
    entity.setProperty("lng", LNG);
    entity.setProperty("address", DEFAULT_STRING);
    entity.setProperty("phone", DEFAULT_STRING);
    entity.setProperty("price", DEFAULT_STRING);
    entity.setProperty("website", WEBSITE);
    entity.setProperty("ID", ID);

    Restaurant expected = new Restaurant(NAME, NUM_REVIEWS, DEFAULT_NUM, LAT, LNG, DEFAULT_STRING, DEFAULT_STRING, DEFAULT_STRING, WEBSITE.toString(), ID);
    Restaurant actual = query.makeRestaurantObject(entity);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testMakeEntityWithNoDefaultValues() throws MalformedURLException {
    // makeRestaurantEntity(): place has all fields filled, so resulting entity will have no default values
    PlaceDetails place = new PlaceDetails();

    Geometry geo = new Geometry();
    LatLng loc = new LatLng(LAT, LNG);
    geo.location = loc;
    place.geometry = geo;
    
    place.name = NAME;
    place.userRatingsTotal = NUM_REVIEWS;
    place.rating = (float) RATING;
    place.formattedAddress = ADDRESS;
    place.formattedPhoneNumber = PHONE;
    place.priceLevel = PRICE;
    place.website = new URL(WEBSITE);
    place.placeId = ID;

    Entity actual = query.makeRestaurantEntity(place, NAME, REVIEWS, DATABASE);

    Assert.assertEquals(NAME, getGenericProperty(actual, "name"));
    Assert.assertEquals(NUM_REVIEWS, (int) getGenericProperty(actual, "numberOfReviews"));
    Assert.assertEquals(RATING, (float) getGenericProperty(actual, "rating"), 0.0f);
    Assert.assertNotNull((double) getGenericProperty(actual, "lat"));
    Assert.assertNotNull((double) getGenericProperty(actual, "lng"));
    Assert.assertEquals(ADDRESS, getGenericProperty(actual, "address"));
    Assert.assertEquals(PHONE, getGenericProperty(actual, "phone"));
    Assert.assertEquals(PRICE.toString(), getGenericProperty(actual, "price"));
    Assert.assertEquals(WEBSITE, getGenericProperty(actual, "website"));
    Assert.assertEquals(ID, getGenericProperty(actual, "ID"));
  }

  @Test
  public void testMakeEntityWithSomeDefaultValues() {
    // makeRestaurantEntity(): place do not have all fields filled, so resulting entity will have corresponding default values
    //                         missing numberOfReviews, address, phone, and website
    PlaceDetails place = new PlaceDetails();

    Geometry geo = new Geometry();
    LatLng loc = new LatLng(LAT, LNG);
    geo.location = loc;
    place.geometry = geo;
    
    place.name = NAME;
    place.rating = (float) RATING;
    place.priceLevel = PRICE;
    place.placeId = ID;

    Entity actual = query.makeRestaurantEntity(place, NAME, REVIEWS, DATABASE);

    Assert.assertEquals(NAME, getGenericProperty(actual, "name"));
    Assert.assertEquals(DEFAULT_NUM, (int) getGenericProperty(actual, "numberOfReviews"), 0.0f);
    Assert.assertEquals(RATING, (float) getGenericProperty(actual, "rating"), 0.0f);
    Assert.assertNotNull((double) getGenericProperty(actual, "lat"));
    Assert.assertNotNull((double) getGenericProperty(actual, "lng"));
    Assert.assertEquals(DEFAULT_STRING, getGenericProperty(actual, "address"));
    Assert.assertEquals(DEFAULT_STRING, getGenericProperty(actual, "phone"));
    Assert.assertEquals(PRICE.toString(), getGenericProperty(actual, "price"));
    Assert.assertEquals(DEFAULT_STRING, getGenericProperty(actual, "website"));
    Assert.assertEquals(ID, getGenericProperty(actual, "ID"));
  }

  @Test
  public void testEmptyString() {
    // splitStringToSet(): input string is empty, so return an empty set
    String str = "";
    Set<String> expected = new HashSet<>();
    Set<String> actual = query.splitStringToSet(str);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testNoChangesString() {
    // splitStringToSet(): input string has 1 word, all lowercase chars, all alphabetic chars. 
    //                     return should have no changes to str
    String str = "test";
    Set<String> expected = new HashSet<>(Arrays.asList(str));
    Set<String> actual = query.splitStringToSet(str);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testSplitStringSomeChanges() {
    // splitStringToSet(): input string has >1 word, some uppercase chars, and some non-alphabetic chars.
    //                     return should split the word boundaries and make the uppercase -> lowercase and remove all non-alphabetic chars
    String str = "One 2 thrEE.";
    Set<String> expected = new HashSet<>(Arrays.asList("one", "three"));
    Set<String> actual = query.splitStringToSet(str);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testRemoveAllChars() {
    // splitStringToSet(): input string has >1 word, all non-alphabetic chars
    //                     return should remove all chars and return empty set
    String str = "!23 @#$%^ (&;,";
    Set<String> expected = new HashSet<>();
    Set<String> actual = query.splitStringToSet(str);

    Assert.assertEquals(expected, actual);
  }

  private static <T> T getGenericProperty(Entity entity, String property) {
    T value = (T) entity.getProperty(property);
    return value;
  }

}