package com.google.sps.data;

// holds data about login status
// status refers to if logged in(true) or out(false)
// url refers to redirect url
// email is user's email

public class LoginStatus {
  private final boolean status;
  private final String url;
  private final String email;

  public LoginStatus(boolean status, String url, String email) {
    this.status = status;
    this.url = url;
    this.email = email;
  }
}
