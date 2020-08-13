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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form and add them to the database of suggestions in datastore
    String name = getParameter(request, "name-input", "");
    String relation = getParameter(request, "relation-input", "");
    String problemArea = getParameter(request, "problem-area-input", "");
    String suggestion = getParameter(request, "suggestion-input", "");
    String otherFeedback = getParameter(request, "feedback-input", "");

    Entity feedbackEntity = new Entity("Feedback");
    feedbackEntity.setProperty("name", name);
    feedbackEntity.setProperty("relation", relation);
    feedbackEntity.setProperty("problemArea", problemArea);
    feedbackEntity.setProperty("suggestion", suggestion);
    feedbackEntity.setProperty("otherFeedback", otherFeedback);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(feedbackEntity);

    response.sendRedirect("/main.html");
  }

  /**
    * @return the request parameter, or the default value if the parameter was not specified by the
    *     client
    */
  private String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
    String value = request.getParameter(paramName);
    if (value.length() == 0) {
      return defaultValue;
    }
    return value;
  }
}
