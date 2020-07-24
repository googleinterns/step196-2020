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

/** Creates a map and adds it to the page. */
function createMap() {
  const map = new google.maps.Map(
  document.getElementById('map'), {
    center: {lat: 40.7128, lng: -74.0060},
    zoom: 11,
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
    }
  );
}

function displayPanel() {
    document.getElementById("map").style.width = "75%"; 
    document.getElementById("panel").style.display = "block";
}

window.onload = createMap;
