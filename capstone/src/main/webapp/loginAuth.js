/**
 * checks if user is logged in or not
 */
function fetchLoginStatus() {
  fetch('/login').then((response) => response.json()).then((loginStatus) => {
    const redirectUrl = document.createElement('a');
    redirectUrl.setAttribute('href', loginStatus.url);
    if (loginStatus.status) {
      location = '/main.html';
    } else {
      // if not logged in, go to login page
      document.getElementById('login-button').addEventListener('click',
          function() {
            window.location.href = loginStatus.url; // Navigate to map page
          });
    }
  });
}

function fetchLogoutStatus() {
  fetch('/login').then((response) => response.json()).then((loginStatus) => {
    const redirectUrl = document.createElement('a');
    redirectUrl.setAttribute('href', loginStatus.url);
    if(loginStatus.status) {
      document.getElementById('logout-button').addEventListener('click',
          function() {
            window.location.href = loginStatus.url; // Navigate to login page
          });
    } else {
      location = 'index.html';
    }
  });
}

window.addEventListener('DOMContentLoaded', (event) => {
  if (document.getElementById('login-button')) {
    fetchLoginStatus();
  }
  if (document.getElementById('logout-button')) {
    fetchLogoutStatus();
  }
});

/**
 Only shows button after title animation finished
 */
setTimeout(function() {
  document.getElementById('login-button').style.display = 'inline';
}, 3000);
