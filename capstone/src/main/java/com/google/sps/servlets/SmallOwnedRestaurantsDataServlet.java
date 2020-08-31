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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.businessDataUtils;
import com.google.sps.data.Restaurant;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.sps.data.RestaurantQueryHelper;
import java.io.IOException;
import java.util.ArrayList;
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
  private static final String DATABASE_NAME = "SmallRestaurants";

  private RestaurantDetailsGetter details = new RestaurantDetailsGetter();
  private RestaurantQueryHelper queryHelper = new RestaurantQueryHelper();

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

  /**
   * gets top 20 restaurants from small business database with matching tags as keyword, sorted by
   * rating
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keywordsCombinedString = (String) request.getParameter("keyword");
    Set<String> keywords = queryHelper.splitStringToSet(keywordsCombinedString);

    Query query = new Query(DATABASE_NAME).addSort("rating", SortDirection.DESCENDING);

    if (!keywords.isEmpty()) {
      Filter propertyFilter = new FilterPredicate("tags", FilterOperator.IN, keywords);
      query.setFilter(propertyFilter);
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> allRestaurants =
        datastore.prepare(query).asList(FetchOptions.Builder.withLimit(queryHelper.MAX_RESULTS));

    ArrayList<Restaurant> result = new ArrayList<>();
    for (Entity RestaurantEntity : allRestaurants) {
      Restaurant restaurant = queryHelper.makeRestaurantObject(RestaurantEntity);
      result.add(restaurant);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(result));
  }

  /**
   * triggers call to scrape business names, get place details for each business, and populate
   * database
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, NullPointerException {
    List<String> restaurantNames = getRestaurantNames();

    businessDataUtils storeDataHelper = new businessDataUtils();
    storeDataHelper.storeData(restaurantNames, DATABASE_NAME, details, queryHelper);

    response.sendRedirect("/main.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter was not specified by the
   *     client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
