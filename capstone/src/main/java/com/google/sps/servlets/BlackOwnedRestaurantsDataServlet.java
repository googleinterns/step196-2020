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
import com.google.gson.Gson;
import com.google.sps.data.RestaurantDetailsGetter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import com.google.maps.model.PlaceDetails;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns black owned restaurants data as a JSON object */
@WebServlet("/black-owned-restaurants-data")
public class BlackOwnedRestaurantsDataServlet extends HttpServlet {

  private ArrayList<String> blackRestaurants = new ArrayList<>();
  private ArrayList<PlaceDetails> detailedPlaces = new ArrayList<>();
  private Set<String> entities;
  
  private RestaurantDetailsGetter details = new RestaurantDetailsGetter();

  @Override
  public void init() {
    Scanner scanner =
        new Scanner(
            getServletContext().getResourceAsStream("/WEB-INF/black-owned-restaurants.csv"));
    int i = 0;
    while (scanner.hasNextLine()) {
      if (i >= 1) break;
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String name = String.valueOf(cells[0]);
      PlaceDetails place = details.request(name);

      detailedPlaces.add(place); 

      String reviews = "";
      PlaceDetails.Review[] reviewsList = place.reviews;
      for (PlaceDetails.Review review : reviewsList) {
        reviews += review.text + " ";
      } 
      Set<String> tags = details.getTags(reviews);

      blackRestaurants.add(name);
      i++;
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(entities);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String restaurantName = request.getParameter("businessName");
    boolean blackOwned = true;
    boolean smallBusiness = false;
    // stores tags like "pizza" or "chinese"
    HashSet<String> tags = new HashSet<String>();
    // because input is string regardless of actual type, we need to extract each value in list
    // TODO: input type might change so will need to do specific character cuts
    //      as needed e.g. if inputted as list must omit '[' and ']' chars when storing
    String[] tagsAsStringValues = request.getParameter("tags").split(", ");
    for (String tag : tagsAsStringValues) {
      tags.add(tag);
    }

    Entity restaurantEntity = new Entity("Restaurant");
    restaurantEntity.setProperty("name", restaurantName);
    restaurantEntity.setProperty("blackOwned", blackOwned);
    restaurantEntity.setProperty("smallBusiness", smallBusiness);
    restaurantEntity.setProperty("tags", tags);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(restaurantEntity);
    response.sendRedirect("/login.html");
  }
}
