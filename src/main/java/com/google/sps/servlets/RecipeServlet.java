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
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBObject;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.Recipe;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.sps.meltingpot.data.SortingMethod;
import com.google.sps.meltingpot.data.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
  private DBInterface dbInterface;

  @Override
  public void init() {
    dbInterface = new FirestoreDB();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeId = request.getParameter("recipeID");
    String json;

    if (recipeId == null) {
      json = getRecipeList(request, response);
    } else {
      json = getDetailedRecipe(recipeId);
    }

    if (json == null) {
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
    newRecipe.metadata = gson.fromJson(data, RecipeMetadata.class);
    if (newRecipe.content == null || newRecipe.metadata.title == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = getUid(token, response);
    if (uid == null) {
      return;
    }

    newRecipe.metadata.creatorId = uid;
    String recipeId = dbInterface.addRecipe(newRecipe.metadata, newRecipe.content);

    dbInterface.makeUserPropertyTrue(uid, newRecipe.metadata.id, User.CREATED_RECIPES_KEY);

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(newRecipe.metadata.id));
    System.out.println(gson.toJson(newRecipe.metadata.id));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Recipe newRecipe = gson.fromJson(data, Recipe.class);
    newRecipe.metadata = gson.fromJson(data, RecipeMetadata.class);
    if (newRecipe.metadata.id == null || newRecipe.content == null || newRecipe.metadata.title == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = getUidAndMatchUser(token, newRecipe.metadata.id, response);
    if (uid == null) {
      return;
    }

    dbInterface.editRecipeTitleContent(newRecipe.metadata.id, newRecipe.metadata.title, newRecipe.content);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String recipeId = request.getParameter("recipeID");
    if (recipeId == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = getUidAndMatchUser(token, recipeId, response);
    if (uid == null) {
      return;
    }

    dbInterface.deleteComments(recipeId);
    dbInterface.deleteRecipe(recipeId);
  }

  private String getRecipeList(HttpServletRequest request, HttpServletResponse response) {
    String creatorToken = request.getParameter("token");

    String tagParam = request.getParameter("tagIDs");
    boolean isSavedRequest = Boolean.parseBoolean(request.getParameter("saved"));

    boolean isTagQuery = !(tagParam == null || tagParam.equals("None"));
    boolean isCreatorQuery = !(creatorToken == null || creatorToken.equals("None"));

    if (isSavedRequest || (isCreatorQuery && !isTagQuery)) {
      String uid = getUid(creatorToken, response);
      if (uid == null) {
        return null;
      }

      if (isSavedRequest) {
        return gson.toJson(dbInterface.getRecipesSavedBy(uid, SortingMethod.TOP));
      } else {
        return gson.toJson(dbInterface.getRecipesMatchingCreator(uid, SortingMethod.TOP));
      }
    } else if (isTagQuery && !isCreatorQuery) {
      String[] tagIDs = tagParam.split(",");
      return gson.toJson(dbInterface.getRecipesMatchingTags(Arrays.asList(tagIDs), SortingMethod.TOP));
    } else { // Currently addresses cases where both isTagQuery and isCreatorQuery, and where
             // neither.
      return gson.toJson(dbInterface.getAllRecipes(SortingMethod.TOP));
    }
  }

  private String getDetailedRecipe(String recipeId) throws IOException {
    Recipe recipe = new Recipe();
    recipe.metadata = dbInterface.getRecipeMetadata(recipeId);
    recipe.content = dbInterface.getRecipeContent(recipeId);
    return gson.toJson(recipe);
  }

  /** Checks the authentication of a given token. If the token is not valid, returns null */
  private String getUid(String token, HttpServletResponse response) {
    FirebaseToken decodedToken = Auth.verifyIdToken(token);
    if (decodedToken == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }
    return decodedToken.getUid();
  }

  /**
   * Checks if token valid, then if corresponding user created the given recipe. If either
   * fails, returns null and sets HttpServletResponse status accordingly
   */
  private String getUidAndMatchUser(String token, String recipeId, HttpServletResponse response) {
    String uid = getUid(token, response);
    if (uid == null) {
      return null;
    }

    if (!User.createdRecipe(uid, recipeId)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return null;
    }

    return uid;
  }
}
