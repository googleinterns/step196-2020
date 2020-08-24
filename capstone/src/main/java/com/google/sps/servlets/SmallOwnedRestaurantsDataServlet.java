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
    System.out.println("keywords " + keywords);

    Query query = new Query("SmallRestaurants").addSort("rating", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> allRestaurants = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    // EntityQuery.Builder builder = Query.newEntityQueryBuilder()
    //     .setFilter(new FilterPredicate("tags", Query.FilterOperator.IN, keywords));

    ArrayList<String> result = new ArrayList<>();
    for (Entity RestaurantObject : allRestaurants){
      List<String> list = (List<String>) RestaurantObject.getProperty("tags");
      Set<String> currRestaurantTags = new HashSet<String>(); 
      currRestaurantTags.addAll(list); 

      System.out.println("\n\n" + currRestaurantTags + "\n" + keywords);

      if (!Collections.disjoint(currRestaurantTags, keywords)) {
        System.out.println("\n\nFOUND RES " + (String) RestaurantObject.getProperty("name"));
        result.add((String) RestaurantObject.getProperty("placeObject"));
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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
      String placeString = place.toString();

      int numberOfReviews = place.userRatingsTotal; 
      float rating = place.rating;
    
      Entity restaurantEntity = new Entity("SmallRestaurants");
      restaurantEntity.setProperty("name", restaurantName);
      restaurantEntity.setProperty("placeObject", placeString);
      restaurantEntity.setProperty("numberOfReviews", numberOfReviews);
      restaurantEntity.setProperty("rating", rating);
      restaurantEntity.setProperty("tags", tags);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(restaurantEntity);
    }
    response.sendRedirect("/admin.html");
  }
}
