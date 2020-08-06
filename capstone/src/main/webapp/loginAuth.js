function fetchLoginStatus() {
  fetch("/login").then(response => response.json()).then((loginStatus) => {
    const redirectUrl = document.createElement('a');
    redirectUrl.setAttribute('href', loginStatus.url);
    if (loginStatus.status) {
      // if logged in, go to maps page
      window.location = "/main.html";
      // TODO: create logout button on page
    }
    else {
      // if not logged in, go to login page
      document.getElementById("login-button").addEventListener("click", function(){
        location = loginStatus.url; // Navigate to new page
      });
    }
  })
}

window.addEventListener('DOMContentLoaded', (event) => {
  if (document.getElementById("login-button")) {
    fetchLoginStatus();
  }
});
