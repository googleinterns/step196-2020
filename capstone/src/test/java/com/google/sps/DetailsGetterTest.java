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


package com.google.sps;

import static org.mockito.Mockito.*;
import com.google.sps.data.RestaurantDetailsGetter;
import com.google.maps.model.PlaceDetails;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PlacesApi.class, FindPlaceFromTextRequest.class, PlaceDetailsRequest.class, PlaceDetails.class})
public final class DetailsGetterTest {
  private static final String address = "25 E 77th St, New York, NY 10075, USA";
  private static final String name = "The Mark Hotel";
  private static final String placeId = "ChIJXYOiRJRYwokRm1i3c9R6WDA"; 

  private RestaurantDetailsGetter details;

  @Before
  public void initRestaurantDetailsGetter() {
    details = new RestaurantDetailsGetter();
  }

  @Before
  public void setUp() {
    details.setUp();
  }

  @After
  public void tearDown() {
    details.tearDown();
  }

  @Test
  public void testForExactLocation() {
    PlaceDetails actual = new PlaceDetails();
    PlaceDetails expected = new PlaceDetails();

    actual = details.request("The Mark Hotel");

    Assert.assertEquals(expected, actual);
  }
}
