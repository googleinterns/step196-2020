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
import java.util.ArrayList;
import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
/**
* The FindingMeetingQuery is a container class that is able to find available meeting times for a particular requested meeting
*/

public final class GettingDetails {  

  protected GeoApiContext context = new GeoApiContext.Builder(new GaeRequestHandler.Builder())
    .apiKey("AIzaSyBmOq13SbE4zw0O6DDk05xmC2urtSfd_gk")
    .build();

  public PlaceDetails getDetails(String placeId) {
    PlaceDetails place = new PlaceDetails();
    try {
      place = PlacesApi.placeDetails(context, placeId).await();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return place;
  }

  public void request(ArrayList<PlaceDetails> detailsList, String placeName) {
    try {
        PlacesSearchResult[] results = PlacesApi
        .findPlaceFromText(context, placeName, FindPlaceFromTextRequest.InputType.TEXT_QUERY)
        .await()
        .candidates;

        String placeID = results[0].placeId;
        PlaceDetails place = getDetails(placeID);
        detailsList.add(place);
      
      } catch (Exception e) {
        e.printStackTrace();
      }
  }
}