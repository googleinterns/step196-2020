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


import com.google.gson.Gson;
import com.google.maps.model.PlaceDetails;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@WebServlet("/business-names")
public class scraperServlet extends HttpServlet {
  
  private ArrayList<PlaceDetails> detailedPlaces = new ArrayList<>();
  private GettingDetails details = new GettingDetails();

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
        String name = restaurantName.text();
        detailedPlaces.add(details.request(name));

        restaurantNames.add(name);
      }
    }

    String restaurantNamesJson = new Gson().toJson(restaurantNames);
    response.setContentType("application/json;");
    response.getWriter().println(restaurantNamesJson);
  }
}