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

window.addEventListener('DOMContentLoaded', (event) => {
  if (document.getElementById('map')) {
    createMap();
  }
  if (document.getElementById('filters-form')) {
    getInputFilters();
  }
});

/** user, at location _center, searches a query with search string _keyword and selected filters _showSmallBusiness and _showBlackOwnedBusiness 
    places matching user's query will be returned on _map 
 */
let _map;
let _center;
let _showSmallBusiness = false;
let _showBlackOwnedBusiness = false;
let _keyword;
const SMALL = 'small';
const BLACK_OWNED = 'black-owned';

/** Creates a map and adds it to the page. */
function createMap() {
  _center = new google.maps.LatLng(40.7128, -74.0060);
  _map = new google.maps.Map(document.getElementById('map'), {
    center: _center,
    zoom: 15,
    styles: [
      {elementType: 'geometry', stylers: [{color: '#242f3e'}]},
      {elementType: 'labels.text.stroke', stylers: [{color: '#242f3e'}]},
      {elementType: 'labels.text.fill', stylers: [{color: '#746855'}]},
      {
          featureType: 'administrative.locality',
          elementType: 'labels.text.fill',
          stylers: [{color: '#d59563'}]
      },
      {
          featureType: 'poi',
          elementType: 'labels.text.fill',
          stylers: [{color: '#d59563'}]
      },
      {
          featureType: 'poi.park',
          elementType: 'geometry',
          stylers: [{color: '#263c3f'}]
      },
      {
          featureType: 'poi.park',
          elementType: 'labels.text.fill',
          stylers: [{color: '#6b9a76'}]
      },
      {
          featureType: 'road',
          elementType: 'geometry',
          stylers: [{color: '#38414e'}]
      },
      {
          featureType: 'road',
          elementType: 'geometry.stroke',
          stylers: [{color: '#212a37'}]
      },
      {
          featureType: 'road',
          elementType: 'labels.text.fill',
          stylers: [{color: '#9ca5b3'}]
      },
      {
          featureType: 'road.highway',
          elementType: 'geometry',
          stylers: [{color: '#746855'}]
      },
      {
          featureType: 'road.highway',
          elementType: 'geometry.stroke',
          stylers: [{color: '#1f2835'}]
      },
      {
          featureType: 'road.highway',
          elementType: 'labels.text.fill',
          stylers: [{color: '#f3d19c'}]
      },
      {
          featureType: 'transit',
          elementType: 'geometry',
          stylers: [{color: '#2f3948'}]
      },
      {
          featureType: 'transit.station',
          elementType: 'labels.text.fill',
          stylers: [{color: '#d59563'}]
      },
      {
          featureType: 'water',
          elementType: 'geometry',
          stylers: [{color: '#17263c'}]
      },
      {
          featureType: 'water',
          elementType: 'labels.text.fill',
          stylers: [{color: '#515c6d'}]
      },
      {
          featureType: 'water',
          elementType: 'labels.text.stroke',
          stylers: [{color: '#17263c'}]
      }
    ]
  });
}

function getSearchResults() {
  let request = {
    location: _center,
    radius: 10000,
    rankBy: google.maps.places.RankBy.PROMINENCE,
    keyword: _keyword,
    types: ["restaurant", "food"]
  };
  service = new google.maps.places.PlacesService(_map);
  service.nearbySearch(request, callback);
}

function callback(results, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
    for (let i = 0; i < results.length; i++) {
      // TODO(#15): change condition to check if matches filter
      if (true) {
        setMarker(results[i]);
      }
    }
  }
  _showSmallBusiness = false;
  _showBlackOwnedBusiness = false;
}

function setMarker(place) {
  const marker = new google.maps.Marker({
    map: _map,
    position: place.geometry.location,
    animation: google.maps.Animation.DROP,
  });
  google.maps.event.addListener(marker, "click", () => {
    displayPanel(place.name);
  });
}

function displayPanel(name) {
  document.getElementById("map").style.width = "75%";
  document.getElementById("panel").style.display = "block";
  document.getElementById("restaurant-info").innerHTML = name;
}

function getInputFilters() {
  document.querySelector("button").addEventListener('click', function(event) {
    event.preventDefault();
    // TODO(#14): clear all markers on map each time new search query is submitted
    const form = document.querySelector("form");
    _keyword = document.getElementById("search").value;
    Array.from(form.querySelectorAll("input")).forEach(function(filterInput) {
      if(filterInput.checked) { 
        if (filterInput.value == SMALL){ 
          _showSmallBusiness = true; 
        }
        if (filterInput.value == BLACK_OWNED){ 
          _showBlackOwnedBusiness = true; 
        }
      } 
    });
    getSearchResults();
  });
}

function fetchBusinessNames() {
  fetch('/businessNames').then(response => response.json()).then((restaurantNames) => {
    for(let name of restaurantNames) {
      console.log(name);
    }
  });
}

getSmallBizNames();
