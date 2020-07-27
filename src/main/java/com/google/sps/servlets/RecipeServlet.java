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

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.sps.data.DBReferences;
import com.google.sps.data.Recipe;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/post")
public class RecipeServlet extends HttpServlet {
  private Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    if (recipeID == null)
      getRecipeList(response);
    else
      getDetailedRecipe(recipeID, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {}

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {}

  private void getRecipeList(HttpServletResponse response) throws IOException {
    Query query = DBReferences.recipes;
    ApiFuture<QuerySnapshot> querySnapshot = query.get();
    ArrayList<Object> recipeList = new ArrayList<>();

    try {
      for (DocumentSnapshot document : querySnapshot.get().getDocuments())
        recipeList.add(document.getData());

      String json = gson.toJson(recipeList);
      response.setContentType("application/json;");
      response.getWriter().println(json);
    } catch (Exception e) {
      System.out.println("Recipe GET threw exception: " + e);
    }
  }

  private void getDetailedRecipe(String recipeID, HttpServletResponse response) throws IOException {
    DocumentReference recipeRef = DBReferences.recipes.document(recipeID);
    ApiFuture<DocumentSnapshot> future = recipeRef.get();

    try {
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            String json = gson.toJson(document.getData());
            response.setContentType("application/json;");
            response.getWriter().println(json);
        } else {
            System.out.println("No such document!");
        }
    } catch (Exception e) {
        System.out.println("Exception on getDetailedRecipe: " + e);
    }
  }
}
