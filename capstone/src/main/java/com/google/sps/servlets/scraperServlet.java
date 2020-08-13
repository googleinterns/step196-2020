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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/business-names")
public class scraperServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ArrayList<String> restaurantNames = new ArrayList<>();
    String urlbase = "https://www.helpourneighborhoodrestaurants.com/";
    String[] locations = {"brooklyn", "manhattan", "queens", "staten-island", "bronx"};

    for (String location : locations) {
      String url = urlbase.concat(location);
      Document page = Jsoup.connect(url).userAgent("JSoup Scraper").get();

      String restaurantNameSelector = "div > h4 > a";
      Elements restaurantNameElements = page.select(restaurantNameSelector);

      for (Element restaurantName : restaurantNameElements) {
        restaurantNames.add(restaurantName.text());
      }
    }

    String restaurantNamesJson = new Gson().toJson(restaurantNames);
    response.setContentType("application/json;");
    response.getWriter().println(restaurantNamesJson);
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String restaurantName = request.getParameter("businessName");
    boolean blackOwned = false;
    boolean smallBusiness = true;
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
