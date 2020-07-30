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

var map;
var center = new google.maps.LatLng(40.7128, -74.0060);
let _showSmallBusiness = false;
let _showBlackOwnedBusiness = false;

/** Creates a map and adds it to the page. */
function createMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    center: center,
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
  var request = {
    location: center,
    radius: 10000,
    types: ["restaurant", "food"]
  };
  service = new google.maps.places.PlacesService(map);
  service.nearbySearch(request, callback);
}

function callback(results, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
    for (var i = 0; i < results.length; i++) {
      // TODO: change condition to check if matches filter
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
    map: map,
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
    // TODO: clear all markers on map each time new search query is submitted
    const form = document.querySelector("form");
    Array.from(form.querySelectorAll("input")).forEach(function(filterInput) {
      if(filterInput.checked) { 
        if (filterInput.value == 'small'){ 
          _showSmallBusiness = true; 
        }
        if (filterInput.value == 'black-owned'){ 
          _showBlackOwnedBusiness = true; 
        }
      } 
    });
    getSearchResults();
  });
}
