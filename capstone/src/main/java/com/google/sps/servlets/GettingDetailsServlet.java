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
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.FindPlaceFromTextRequest.Response;
import com.google.maps.model.OpeningHours;
import com.google.maps.model.OpeningHours.Period;
import com.google.maps.model.OpeningHours.Period.OpenClose;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceDetails.AlternatePlaceIds;
import com.google.maps.model.PlaceDetails.Review;
import com.google.maps.model.PlaceDetails.Review.AspectRating;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlaceDetailsRequest.Response;
import com.google.maps.PlacesApi;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/details")
public class GettingDetailsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    response.getWriter().println("<h1>Hello world!</h1>");
  }
}
