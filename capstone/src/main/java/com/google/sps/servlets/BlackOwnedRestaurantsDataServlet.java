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
import java.util.ArrayList;
import java.util.Set;
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
  
  private GettingDetails details = new GettingDetails();

  @Override
  public void init() {
    Scanner scanner =
        new Scanner(
            getServletContext().getResourceAsStream("/WEB-INF/black-owned-restaurants.csv"));
    while (scanner.hasNextLine()) {
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

}
