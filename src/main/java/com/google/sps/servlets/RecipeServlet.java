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

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBObject;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.Recipe;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.sps.meltingpot.data.SortingMethod;
import com.google.sps.meltingpot.data.User;
import java.io.IOException;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles recipe post requests. */
@WebServlet("/api/post")
public class RecipeServlet extends HttpServlet {
  private Gson gson = new Gson();
  private DBInterface db;

  public RecipeServlet() {}

  public RecipeServlet(DBInterface mockInterface) {
    db = mockInterface;
  }

  @Override
  public void init() {
    db = new FirestoreDB();
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

    if (json == null || json.equals(gson.toJson(null))) {
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
    if (newRecipe.content == null || newRecipe.metadata == null
        || newRecipe.metadata.title == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return;
    }

    newRecipe.metadata.creatorId = uid;
    newRecipe.metadata.votes = 0;
    newRecipe.metadata.timestamp = System.currentTimeMillis();
    newRecipe.metadata.creatorLdap = Auth.getUserEmail(uid);
    String recipeId = db.addRecipe(newRecipe);

    db.setUserProperty(uid, recipeId, User.CREATED_RECIPES_KEY, true);

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(new DBObject(recipeId)));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Recipe newRecipe = gson.fromJson(data, Recipe.class);
    if (newRecipe == null || newRecipe.metadata == null || newRecipe.content == null
        || newRecipe.content.isEmpty() || newRecipe.metadata.title == null
        || newRecipe.metadata.id == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = matchUser(token, newRecipe.metadata.id, response);
    if (uid == null) {
      return;
    }

    db.editRecipeTitleContent(newRecipe.metadata.id, newRecipe.metadata.title, newRecipe.content);
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
    String uid = matchUser(token, recipeId, response);
    if (uid == null) {
      return;
    }

    db.deleteComments(recipeId);
    db.deleteUserProperty(uid, recipeId, User.CREATED_RECIPES_KEY);
    db.deleteRecipe(recipeId);
  }

  protected String getRecipeList(HttpServletRequest request, HttpServletResponse response) {
    String creatorToken = request.getParameter("token");

    SortingMethod sortingMethod;
    try {
      sortingMethod = SortingMethod.valueOf(request.getParameter("sort"));
    } catch (IllegalArgumentException | NullPointerException e) {
      sortingMethod = SortingMethod.TOP;
    }

    Integer page;
    try {
      page = Integer.parseInt(request.getParameter("page"));
    } catch (NumberFormatException e) {
      page = null;
    }

    String tagIDs[] = request.getParameterValues("tagIDs");
    // TODO: change sorting method tags to parameter tags
    boolean isFollowedTagsRequest = Boolean.parseBoolean(request.getParameter("followed-tags"));
    boolean isSavedRequest = Boolean.parseBoolean(request.getParameter("saved"));

    boolean isTagQuery = (tagIDs != null && tagIDs.length > 0 && !tagIDs[0].equals("None"));
    boolean isCreatorQuery = (creatorToken != null && !creatorToken.equals("None"));
    boolean isFollowedTagsQuery = (isCreatorQuery && isFollowedTagsRequest);

    if (isFollowedTagsQuery) {
      // If the front end is requesting recipes tagged with the tags that a certain user follows,
      // then perform that query
      String uid = Auth.getUid(creatorToken, response);
      if (uid == null) {
        return null;
      }

      return gson.toJson(page != null ? db.getRecipesMatchingFollowedTags(uid, sortingMethod, page)
                                      : db.getRecipesMatchingFollowedTags(uid, sortingMethod));
    } else if (isSavedRequest || (isCreatorQuery && !isTagQuery)) {
      // If frontend is requesting saved recipes or created recipes of a given user,
      // make sure they are authenticated
      String uid = Auth.getUid(creatorToken, response);
      if (uid == null) {
        return null;
      }

      // Then perform the corresponding query
      // if page is null, we want to get all associated recipes
      if (isSavedRequest) {
        return gson.toJson(page != null ? db.getRecipesSavedBy(uid, sortingMethod, page)
                                        : db.getRecipesSavedBy(uid, sortingMethod));
      } else {
        return gson.toJson(page != null ? db.getRecipesMatchingCreator(uid, sortingMethod, page)
                                        : db.getRecipesMatchingCreator(uid, sortingMethod));
      }
    } else if (isTagQuery && !isCreatorQuery) {
      // If the frontend is requesting recipes satisfying a certain set of tags,
      // then perform the query
      return gson.toJson(page != null
              ? db.getRecipesMatchingTags(Arrays.asList(tagIDs), sortingMethod, page)
              : db.getRecipesMatchingTags(Arrays.asList(tagIDs), sortingMethod));
    } else { // Currently addresses cases where frontend is requesting both a tag query and
             // a creator query, or none of the above query types
      return gson.toJson(
          page != null ? db.getRecipePage(sortingMethod, page) : db.getAllRecipes(sortingMethod));
    }
  }

  protected String getDetailedRecipe(String recipeId) throws IOException {
    Recipe recipe = new Recipe(db.getRecipeContent(recipeId), db.getRecipeMetadata(recipeId));
    return gson.toJson(recipe);
  }

  /**
   * Checks if token valid, then if corresponding user created the given recipe. If either
   * fails, returns null and sets HttpServletResponse status accordingly
   */
  protected String matchUser(String token, String recipeId, HttpServletResponse response) {
    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return null;
    }

    Boolean userCreatedRecipe = db.getUserProperty(uid, recipeId, User.CREATED_RECIPES_KEY);
    if (userCreatedRecipe == null || !userCreatedRecipe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return null;
    }

    return uid;
  }
}
