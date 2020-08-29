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
import com.google.gson.Gson;
import com.google.maps.model.PlaceDetails;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.RestaurantQueryHelper;
import com.google.sps.data.Restaurant;
import com.google.sps.data.StoreRestaurantDataHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;

/** Returns black owned restaurants data as a JSON object */
@WebServlet("/black-owned-restaurants")
public class BlackOwnedRestaurantsDataServlet extends HttpServlet {
  private static final String DATABASE_NAME = "BlackOwnedRestuarants";

  private RestaurantDetailsGetter details = new RestaurantDetailsGetter();
  private RestaurantQueryHelper queryHelper = new RestaurantQueryHelper();

  /** scrapes business names from source */
  private ArrayList<String> getRestaurantNames() throws IOException{
    ArrayList<String> blackOwnedRestaurants = new ArrayList<>();
    
    Scanner scanner =
        new Scanner(
            getServletContext().getResourceAsStream("/WEB-INF/black-owned-restaurants.csv"));

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String name = String.valueOf(cells[0]);

      blackOwnedRestaurants.add(name);
    }
    scanner.close();
    return blackOwnedRestaurants;
  }

  /** gets top 20 restaurants from black owned business database with matching tags as keyword, sorted by rating */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keywordsCombinedString = (String) request.getParameter("keyword");
    Set<String> keywords = queryHelper.splitStringToSet(keywordsCombinedString);

    Filter propertyFilter = new FilterPredicate("tags", FilterOperator.IN, keywords);
    Query query = new Query(DATABASE_NAME).setFilter(propertyFilter).addSort("rating", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> allRestaurants = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(queryHelper.MAX_RESULTS));

    ArrayList<Restaurant> result = new ArrayList<>();
    for (Entity RestaurantEntity : allRestaurants){
      Restaurant restaurant = queryHelper.makeRestaurantObject(RestaurantEntity);
      result.add(restaurant);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(result));
  }

  /** triggers call to scrape business names, get place details for each business, and populate database */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<String> restaurantNames = getRestaurantNames();
    StoreRestaurantDataHelper storeDataHelper = new StoreRestaurantDataHelper();
    storeDataHelper.storeData(restaurantNames, DATABASE_NAME, details, queryHelper);

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
