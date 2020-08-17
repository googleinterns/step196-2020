package com.google.sps.servlets;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Token;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/nlp-entity-recognition")
public class EntityRecognitionServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String messages = request.getParameter("messages");
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      Document doc = 
          Document.newBuilder().setContent(messages).setType(Document.Type.PLAIN_TEXT).build();
      AnalyzeEntitiesRequest entitiesRequest =
          AnalyzeEntitiesRequest.newBuilder()
              .setDocument(doc)
              .setEncodingType(EncodingType.UTF16)
              .build();

      AnalyzeEntitiesResponse entitiesResponse = language.analyzeEntities(entitiesRequest);
      Set<String> allEntityNames = 
          entitiesResponse.getEntitiesList().stream()
              .map(entity -> entity.getName())
              .collect(Collectors.toSet());

      Gson gson = new Gson();
      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(allEntityNames));
    }
  }
}
