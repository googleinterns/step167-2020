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

package com.google.sps.meltingpot.servlets;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.meltingpot.data.DBObject;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.Recipe;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles recipe post requests. */
@WebServlet("/api/post")
public class RecipeServlet extends HttpServlet {
  private Gson gson = new Gson();
  private boolean documentNotFound = false;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    String json;

    if (recipeID == null) {
      json = getRecipeList(request);
    } else {
      json = getDetailedRecipe(recipeID);
    }

    if (documentNotFound || json == null) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Recipe newRecipe = gson.fromJson(data, Recipe.class);
    if (newRecipe.content == null || newRecipe.title == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    DocumentReference recipeRef = DBUtils.recipes().document();
    newRecipe.id = recipeRef.getId();
    ApiFuture future = recipeRef.set(newRecipe);
    DBUtils.blockOnFuture(future);

    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(new DBObject(newRecipe.id)));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Recipe newRecipe = gson.fromJson(data, Recipe.class);
    if (newRecipe.id == null || newRecipe.content == null || newRecipe.title == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    DocumentReference recipeRef = DBUtils.recipe(newRecipe.id);
    ApiFuture future = recipeRef.set(newRecipe);
    DBUtils.blockOnFuture(future);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String recipeID = request.getParameter("recipeID");
    if (recipeID == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    deleteComments(recipeID);
    ApiFuture<WriteResult> writeResult = DBUtils.recipes().document(recipeID).delete();
  }

  private String getRecipeList(HttpServletRequest request) {
    String tagParam = request.getParameter("tagIDs");
    Query query;

    if (tagParam == null || tagParam.equals("None")) {
      query = DBUtils.recipes();
    } else {
      String[] tagIDs = tagParam.split(",");
      query = recipeWhereContainsArray(tagIDs, tagIDs.length - 1);
    }

    ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
    ArrayList<Object> recipeList = new ArrayList<>();
    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(querySnapshotFuture);

    if (querySnapshot == null) {
      return null;
    }

    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
      recipeList.add(document.getData());
    }
    return gson.toJson(recipeList);
  }

  private Query recipeWhereContainsArray(String[] tagIDs, int index) {
    if (index > 0)
      return recipeWhereContainsArray(tagIDs, index - 1)
          .whereEqualTo("tags." + tagIDs[index], true);
    return DBUtils.recipes().whereEqualTo("tags." + tagIDs[index], true);
  }

  private String getDetailedRecipe(String recipeID) throws IOException {
    DocumentReference recipeRef = DBUtils.recipes().document(recipeID);
    ApiFuture<DocumentSnapshot> future = recipeRef.get();
    DocumentSnapshot document = DBUtils.blockOnFuture(future);

    if (document == null || !document.exists()) {
      return null;
    } else {
      return gson.toJson(document.getData());
    }
  }

  private void deleteComments(String recipeID) {
    ApiFuture<QuerySnapshot> future = DBUtils.comments(recipeID).get();
    List<QueryDocumentSnapshot> documents = DBUtils.blockOnFuture(future).getDocuments();
    for (QueryDocumentSnapshot document : documents) {
      document.getReference().delete();
    }
  }
}
