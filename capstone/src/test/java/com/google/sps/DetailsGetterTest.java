package com.google.sps;

import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.Restaurant;
import com.google.maps.model.PlaceDetails;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.maps.PlacesApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/** Tests RestaurantDetailsGetter java class
 *    getDetails(String placeId):
 *          placeId: unique string identifying location
 *          return: PlaceDetails object of that location
 *
 *    request(String name):
 *          name: name of location you want the details for 
 *          return: PlaceDetails object that contains all details of the location in question
 *
 *    getTagsFromReviews(PlaceDetails.Review[] reviewsArray):
 *          reviewsArray: review objects for a specific location
 *          return: String that is a concatenated value of all text reviews in the array
 */

@RunWith(JUnit4.class)
public final class DetailsGetterTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private RestaurantDetailsGetter details;

  private static String DEFAULT_STRING = "";

  @Before
  public void setUp() {
    helper.setUp();
    details = new RestaurantDetailsGetter();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testGetDetailsReturnsCorrectResponse() {

    String id = "ChIJM8mGj4lZwokRSbZBvNOVNKM";
    PlaceDetails place = details.getDetails(id);

    String expectedName = "NOMO SOHO";
    String expectedAddress = "9 Crosby St, New York, NY 10013, USA";

    String actualName = place.name;
    String actualAddress = place.formattedAddress;

    Assert.assertEquals(expectedName, actualName);
    Assert.assertEquals(expectedAddress, actualAddress);
  }

  @Test
  public void testRequestMethod() {
    
    String name = "Kikoo Sushi";

    PlaceDetails place = details.request(name);

    String actualName = place.name;
    String actualAddress = place.formattedAddress;
    String expectedAddress = "141 1st Avenue, New York, NY 10003, USA";

    Assert.assertEquals(name, actualName);
    Assert.assertEquals(expectedAddress, actualAddress);
  }

  @Test
  public void testGetTagsFromEmptyPlaceDetails() {

    PlaceDetails place = new PlaceDetails();
    PlaceDetails.Review[] reviews = place.reviews;

    String expected = DEFAULT_STRING;
    String actual = details.getTagsfromReviews(reviews);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testGetTagsFromReviewArrayofPlace() {

    PlaceDetails place = details.request("Burger King");
    PlaceDetails.Review[] reviews = place.reviews;

    try {
      for (PlaceDetails.Review review : reviews) {
        review.text = "Hello, world!";
      } 
    } catch (NullPointerException e) {}

    String expected = "Hello, world!Hello, world!Hello, world!Hello, world!Hello, world!";
    String actual = details.getTagsfromReviews(reviews);

    Assert.assertEquals(expected, actual);
  }
}
