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
import java.io.IOException;
import java.lang.String;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns black owned restaurants data as a JSON object */
@WebServlet("/black-owned-restaurants-data")
public class BlackOwnedRestaurantsDataServlet extends HttpServlet {

  private ArrayList<String> restaurantNames = new ArrayList<>();

  @Override
  public void init() {
    Scanner scanner = 
      new Scanner(
        getServletContext().getResourceAsStream("/WEB-INF/black-owned-restaurants.csv"));
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String name = String.valueOf(cells[0]);

      restaurantNames.add(name);
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(restaurantNames);
    response.getWriter().println(json);
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String restaurantName = request.getParameter("businessName");
    boolean blackOwned = true;
    boolean smallBusiness = false;
    // stores tags like "pizza" or "chinese"
    // implemented as hashset for faster lookup
    HashSet<String> tags = new HashSet<String>();
    // because input is string regardless of actual type, we need to extract each value in list
    // TODO: input type might change so will need to do specific character cuts
    //      as needed e.g. if inputted as list must omit '[' and ']' chars when storing
    String[] tagsAsStringValues = request.getParameter("tags").split(", ");
    for (String tag : tagsAsStringValues) {
      if (tags.contains(tag) == false) {
        tags.add(tag);
      }
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
