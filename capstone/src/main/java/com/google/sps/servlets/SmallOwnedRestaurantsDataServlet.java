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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.maps.model.PlaceDetails;
import com.google.sps.data.Restaurant;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.RestaurantQueryHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@WebServlet("/small-restaurants")
public class SmallOwnedRestaurantsDataServlet extends HttpServlet {
  private RestaurantDetailsGetter details = new RestaurantDetailsGetter();
  private RestaurantQueryHelper queryHelper = new RestaurantQueryHelper();

  private ArrayList<String> restaurantNames = new ArrayList<>();

  /** scrapes business names from source */
  private ArrayList<String> getRestaurantNames() throws IOException {
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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keywords = (String) request.getParameter("keyword");
    Query query = new Query("SmallRestaurants").addSort("rating", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> allRestaurants =
        datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    ArrayList<Restaurant> result = new ArrayList<>();
    for (Entity RestaurantEntity : allRestaurants) {
      if (queryHelper.restaurantContainsKeyword(RestaurantEntity, keywords)) {
        Restaurant restaurant = queryHelper.makeRestaurantObject(RestaurantEntity);
        result.add(restaurant);

        if (result.size() >= queryHelper.MAX_RESULTS) break;
      }
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(result));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, NullPointerException {
    clearDatastore();

    List<String> restaurantNames = getRestaurantNames();

    for (String restaurantName : restaurantNames) {
      PlaceDetails place = details.request(restaurantName);

      PlaceDetails.Review[] reviewsArray = place.reviews;
      String reviews = details.getTagsfromReviews(reviewsArray);

      Entity restaurantEntity =
        queryHelper.makeRestaurantEntity(place, restaurantName, reviews, "SmallRestaurants");
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(restaurantEntity);
    }
    response.sendRedirect("/admin.html");
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

  /**
   * @return the request parameter, or the default value if the parameter was not specified by the
   *         client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
