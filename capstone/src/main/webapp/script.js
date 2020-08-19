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
});

/** user, at location _center, searches a query with search string
    keyword and selected filters _showSmallBusiness and _showBlackOwnedBusiness
    places matching user's query will be returned on _map
    and stored in _markersArray
 */
let _map;
const _markersArray = [];
let _center;
let _showSmallBusiness = false;
let _showBlackOwnedBusiness = false;
const _scrapedSmallBusinesses = new Set();
const _scrapedBlackBusinesses = new Set();
const _detailedSmallBusinesses = new Set();
const _detailedBlackOwned = new Set();
const SMALL = 'small';
const BLACK_OWNED = 'black-owned';

/** Gets business names from scraped datasets and puts in array */
function fetchBusinessNames() {
  fetch('/business-names').then((response) => response.json()).then(
      (restaurantNames) => {
        for (const name of restaurantNames) {
          _scrapedSmallBusinesses.add(name);
        }
      });

  fetch('/black-owned-restaurants-data').then((response) =>
    response.json()).then((restaurantNames) => {
    for (const name of restaurantNames) {
      _scrapedBlackBusinesses.add(name);
    }
  });
}

fetchBusinessNames();

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
        stylers: [{color: '#d59563'}],
      },
      {
        featureType: 'poi',
        elementType: 'labels.text.fill',
        stylers: [{color: '#d59563'}],
      },
      {
        featureType: 'poi.park',
        elementType: 'geometry',
        stylers: [{color: '#263c3f'}],
      },
      {
        featureType: 'poi.park',
        elementType: 'labels.text.fill',
        stylers: [{color: '#6b9a76'}],
      },
      {
        featureType: 'road',
        elementType: 'geometry',
        stylers: [{color: '#38414e'}],
      },
      {
        featureType: 'road',
        elementType: 'geometry.stroke',
        stylers: [{color: '#212a37'}],
      },
      {
        featureType: 'road',
        elementType: 'labels.text.fill',
        stylers: [{color: '#9ca5b3'}],
      },
      {
        featureType: 'road.highway',
        elementType: 'geometry',
        stylers: [{color: '#746855'}],
      },
      {
        featureType: 'road.highway',
        elementType: 'geometry.stroke',
        stylers: [{color: '#1f2835'}],
      },
      {
        featureType: 'road.highway',
        elementType: 'labels.text.fill',
        stylers: [{color: '#f3d19c'}],
      },
      {
        featureType: 'transit',
        elementType: 'geometry',
        stylers: [{color: '#2f3948'}],
      },
      {
        featureType: 'transit.station',
        elementType: 'labels.text.fill',
        stylers: [{color: '#d59563'}],
      },
      {
        featureType: 'water',
        elementType: 'geometry',
        stylers: [{color: '#17263c'}],
      },
      {
        featureType: 'water',
        elementType: 'labels.text.fill',
        stylers: [{color: '#515c6d'}],
      },
      {
        featureType: 'water',
        elementType: 'labels.text.stroke',
        stylers: [{color: '#17263c'}],
      },
    ],
  });
}

/** Obtains search results from Places API
    @param {any} keyword search query keyword
    @return {Promise} top 20 places matching search results */
function getPlacesSearchResults(keyword) {
  document.getElementById('map').style.width = '75%';
  document.getElementById('panel').style.display = 'block';
  document.getElementById('restaurant-results').innerHTML = '';

  const request = {
    location: _center,
    radius: 10000,
    rankBy: google.maps.places.RankBy.PROMINENCE,
    keyword: keyword,
    types: ['restaurant', 'food'],
  };
  service = new google.maps.places.PlacesService(_map);
  return service.nearbySearch(request, callback);
}

/** Function for aiding calls to nearbySearch and getDetails
    @param {any} results
    @param {any} status
*/
function callback(results, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
    for (let i = 0; i < results.length; i++) {
      if (!_showSmallBusiness && !_showBlackOwnedBusiness) {
        // put fetch here 
        setMarker(results[i]);
        continue;
      }
      if (_showSmallBusiness &&
      _scrapedSmallBusinesses.has(results[i].name)) {
        setMarker(results[i]);
        continue;
      }
      if (_showBlackOwnedBusiness &&
      _scrapedBlackBusinesses.has(results[i].name)) {
        setMarker(results[i]);
        continue;
      }
    }
  }
  _showSmallBusiness = false;
  _showBlackOwnedBusiness = false;
}

/** Creates an animated marker for each result location
   @param {Object} place location to set marker for
 */
function setMarker(place) {
  const marker = new google.maps.Marker({
    map: _map,
    position: place.geometry.location,
    animation: google.maps.Animation.DROP,
  });
  addToDisplayPanel(place);
  _markersArray.push(marker);
}

/** Clears all markers on map */
function clearMarkers() {
  for (let i = 0; i < _markersArray.length; i++ ) {
    _markersArray[i].setMap(null);
  }
  _markersArray.length = 0;
}

/** Itemizes each result into the collapsible panel
@param {Object} place location to add to results panel
 */
function addToDisplayPanel(place) {
  fetch('/get-details?placeId=' + place.place_id).then(response => response.json()).then((place) => {
    // put call to get details here and get new place before adding to panel
    const locationElement = document.getElementById('restaurant-results');
    locationElement.appendChild(createLocationElement(place));
    locationElement.appendChild(createAdditionalInfo(place));
  });
}

/** Creates button with location name for each place
    Add clicker event to each button to handle open and close of
    collapsible.
    @param {Object} place location to create button for
    @return {HTMLButtonElement} the created location button
 */
function createLocationElement(place) {
  const mainElement = document.createElement('button');
  mainElement.className = 'collapsible';
  mainElement.innerHTML = place.name;

  mainElement.addEventListener('click', function() {
    this.classList.toggle('active');
    const content = this.nextElementSibling;
    if (content.style.maxHeight) {
      content.style.maxHeight = null;
    } else {
      content.style.maxHeight = content.scrollHeight + 'px';
    }
  });

  return mainElement;
}

/** Creates div that contains all extra info about a place
    @param {Object} place place to add additional information for
    @return {HTMLDivElement} the added information in a div element
*/
function createAdditionalInfo(place) {
  const informationContainer = document.createElement('div');
  informationContainer.className = 'info';

  const information = document.createElement('p');
  information.innerHTML = `Address: ${place.formattedAddress}<br>Phone Number: ${place.formattedPhoneNumber}<br>Rating: ${place.rating}<br>Price:
   ${place.priceLevel}<br>Website: ${place.website}`;

  const editsLink = document.createElement('a');
  editsLink.setAttribute('href', 'feedback.html');
  editsLink.innerHTML = 'Suggest edits?';

  informationContainer.appendChild(information);
  informationContainer.appendChild(editsLink);

  return informationContainer;
}

/** Closes collapsible panels when the x is clicked. */
function closePanel() {
  document.getElementById('panel').style.display = 'none';
  document.getElementById('map').style.width = '100%';
}

/** Clears all exisiting markers on map
    and gets filters from checked boxes, ie. small or black-owned */
async function getInputFilters() {
  clearMarkers();
  keyword = document.getElementById('search').value;
  const selectedFilters = document.getElementById('filter-input').value;

  if (selectedFilters == SMALL) {
    _showSmallBusiness = true;
  } else if (selectedFilters == BLACK_OWNED) {
    _showBlackOwnedBusiness = true;
  }

  // do manual search if filter is selected, else do Nearby Search w Places API
  if (_showSmallBusiness || _showBlackOwnedBusiness) {
    if (!isStringEmpty(keyword)) {
      const keywordEntities = getEntities(keyword);
      // TODO(#47): manual search
    }
  } else {
    await getPlacesSearchResults(keyword);
  }
  document.getElementById('search-button').disabled = false;
}

/** @param {String} str input string to check
    @return {boolean} if str is empty, contains only white space, or null */
function isStringEmpty(str) {
  return (str.length === 0 || !str.trim() || !str);
}

async function getReviewsEntities(reviews) {
  // TODO(#33): integrate with actual reviews of businesses
  const reviewsEntities = await getEntities(reviews);
  return reviewsEntities;
}

/** send POST request to Cloud Natural Language API for entity recognition
    @param {String} messages message to passed into NLP API
    @return {Promise} entities from messages
*/
function getEntities(messages) {
  const url = '/nlp-entity-recognition?messages=' + messages;
  const requestParamPOST = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  };
  return fetch(url, requestParamPOST).then((response) => response.json())
      .then((entities) => {
        return entities;
      }).catch((err) => {
        console.log('Error reading data ' + err);
      });
}
