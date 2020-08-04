function fetchLoginStatus() {
  fetch('/login').then(response => response.text()).then((loginStatus) => {
    //Because loginStatus will return either the logout / login message
    //we can just check the length of return message to ascertain if
    //we are logged in or out
    if(loginStatus.length < 100) {
      window.location = "/login.html";
    }
  });
}

window.addEventListener('load', fetchLoginStatus);
