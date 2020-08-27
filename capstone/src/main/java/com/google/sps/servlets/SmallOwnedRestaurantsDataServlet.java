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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.RestaurantDetailsGetter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@WebServlet("/business-names")
public class SmallOwnedRestaurantsDataServlet extends HttpServlet {
  
  private ArrayList<PlaceDetails> detailedPlaces = new ArrayList<>();
  private RestaurantDetailsGetter details = new RestaurantDetailsGetter();
  private ArrayList<String> restaurantNames = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

    String restaurantNamesJson = new Gson().toJson(restaurantNames);
    response.setContentType("application/json;");
    response.getWriter().println(restaurantNamesJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    clearDatastore();

    for (String restaurantName : restaurantNames) {
      PlaceDetails place = details.request(restaurantName);

      detailedPlaces.add(place); 

      String reviews = "";
      PlaceDetails.Review[] reviewsList = place.reviews;
      for (PlaceDetails.Review review : reviewsList) {
        reviews += review.text + " ";
      } 
      
      Set<String> tags = details.getTags(reviews);
      String tagsString = tags.toString();

      String placeString = place.toString();

      boolean blackOwned = true;
      boolean smallBusiness = false;
      int numberOfReviews = place.userRatingsTotal; 
      float rating = place.rating;
    
      Entity restaurantEntity = new Entity("Restaurant");
      restaurantEntity.setProperty("name", restaurantName);
      restaurantEntity.setProperty("placeObject", placeString);
      restaurantEntity.setProperty("blackOwned", blackOwned);
      restaurantEntity.setProperty("smallBusiness", smallBusiness);
      restaurantEntity.setProperty("numberOfReviews", numberOfReviews);
      restaurantEntity.setProperty("rating", rating);
      restaurantEntity.setProperty("tags", tagsString);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(restaurantEntity);
    }
    response.sendRedirect("/admin.html");
  }

  public void clearDatastore(){
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
