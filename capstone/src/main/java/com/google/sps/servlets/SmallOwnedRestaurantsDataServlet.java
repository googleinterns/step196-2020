// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.maps.model.PlaceDetails;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.RestaurantDetailsGetter;
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

@WebServlet("/small-restaurants")
public class SmallOwnedRestaurantsDataServlet extends HttpServlet {
  private int MAX_RESULTS = 20;
  private ArrayList<PlaceDetails> detailedPlaces = new ArrayList<>();
  private RestaurantDetailsGetter details = new RestaurantDetailsGetter();
  private ArrayList<String> restaurantNames = new ArrayList<>();

/** scrapes business names from source */
  private ArrayList<String> getRestaurantNames() throws IOException{
    System.out.println("scraping names");
    ArrayList<String> restaurantNames = new ArrayList<>();
    String urlbase = "https://www.helpourneighborhoodrestaurants.com/";
    String[] locations = {"brooklyn", "manhattan", "queens", "staten-island", "bronx"};

    for (String location : locations) {
      String url = urlbase.concat(location);
      Document page = Jsoup.connect(url).userAgent("JSoup Scraper").get();

      String restaurantNameSelector = "div > h4 > a";
      Elements restaurantNameElements = page.select(restaurantNameSelector);

      for (Element restaurantName : restaurantNameElements) {
        String name = restaurantName.text();
        restaurantNames.add(name);
      }
    }
    return restaurantNames;
  }

  private Set<String> splitStringToSet(String str) {
    String[] strArray = str.split(",");
    Set<String> strSet = new HashSet<>(Arrays.asList(strArray));
    return strSet;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keywordsCombinedString = (String) request.getParameter("keyword");
    Set<String> keywords = splitStringToSet(keywordsCombinedString);

    Query query = new Query("SmallRestaurants").addSort("rating", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> allRestaurants = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    ArrayList<Restaurant> result = new ArrayList<>();
    for (Entity RestaurantObject : allRestaurants){
      List<String> list = (List<String>) RestaurantObject.getProperty("tags");
      Set<String> currRestaurantTags = new HashSet<String>(); 
      currRestaurantTags.addAll(list); 

      if (!Collections.disjoint(currRestaurantTags, keywords)) {
        System.out.println("\n\nFOUND RES " + (String) RestaurantObject.getProperty("name"));
        Restaurant restaurant = makeRestaurantObject(RestaurantObject);
        result.add(restaurant);

        if (result.size() >= MAX_RESULTS) {
          break;
        }
      }
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(result));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, NullPointerException {
    restaurantNames = getRestaurantNames();
    for (String restaurantName : restaurantNames) {
      PlaceDetails place = details.request(restaurantName);
      detailedPlaces.add(place); 

      String reviews = "";
      PlaceDetails.Review[] reviewsList = place.reviews;
      try {
        for (PlaceDetails.Review review : reviewsList) {
          reviews += review.text + " ";
        } 
      }
      catch (NullPointerException e) {}
      
      Set<String> tags = details.getTags(reviews);
      System.out.println("place " + restaurantName + "\ntags: " + tags + "\n\n");

      try {
        int numberOfReviews = place.userRatingsTotal; 
        double rating = place.rating;
        double lat = place.geometry.location.lat;
        double lng = place.geometry.location.lng;
        String address = place.formattedAddress;
        String phone = place.formattedPhoneNumber;
        String price = place.priceLevel.toString();
        String website = place.website.toString();
        String placeID = place.placeId;

        Entity restaurantEntity = new Entity("SmallRestaurants");
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

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(restaurantEntity);

      } catch (NullPointerException e) {}
    }
    
    response.sendRedirect("/admin.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  private Restaurant makeRestaurantObject(Entity RestaurantEntity) throws NullPointerException {
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
}
