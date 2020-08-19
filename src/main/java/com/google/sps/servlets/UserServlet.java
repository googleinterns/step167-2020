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
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.Comment;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.User;
import com.google.sps.meltingpot.data.UserRequestType;
import java.io.IOException;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles user edit requests for saved/voted posts, tags following, etc. */
@WebServlet("/api/user")
public class UserServlet extends HttpServlet {
  private Gson gson = new Gson();

  private final Logger logger = Logger.getLogger(UserServlet.class.getName());

  private DBInterface db;

  public UserServlet(DBInterface mock) {
    this.db = mock;
  }

  public UserServlet() {}

  @Override
  public void init() {
    db = new FirestoreDB();
  }

  /** Currently lets the front end know if a recipe is saved by a user or not. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    String token = request.getParameter("token");

    UserRequestType requestType = UserRequestType.valueOf(request.getParameter("type"));

    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return;
    }

    switch (requestType) {
      case SAVE:
        if (recipeID == null) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }

        // Call the FirestoreDB method.
        Boolean isSavedRecipe = db.getUserProperty(uid, recipeID, User.SAVED_RECIPES_KEY);
        response.setContentType("text/plain");
        // If the property didn't exist at all, need to make sure to return false.
        if (isSavedRecipe == null) {
          response.getWriter().println("false");
        } else {
          response.getWriter().println(isSavedRecipe.toString());
        }
        break;
    }
  }

  /** Add a new user document to the Firebase users collection. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String token = request.getParameter("token");

    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return;
    }

    // Call the FirestoreDB method.
    db.addUser(uid);

    response.setStatus(HttpServletResponse.SC_CREATED);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    String token = request.getParameter("token");
    UserRequestType requestType = UserRequestType.valueOf(request.getParameter("type"));

    String tagID = request.getParameter("tagID");

    String uid = Auth.getUid(token, response);
    if (uid == null) {
      // Auth sets response status to unauthorized.
      return;
    }

    switch (requestType) {
      case SAVE:
        if (recipeID == null) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }

        // Call the FirestoreDB method.
        db.setUserProperty(uid, recipeID, User.SAVED_RECIPES_KEY, true);
        response.setStatus(HttpServletResponse.SC_OK);
        break;
      case UNSAVE:
        if (recipeID == null) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }

        // Call the FirestoreDB method.
        db.deleteUserProperty(uid, recipeID, User.SAVED_RECIPES_KEY);
        response.setStatus(HttpServletResponse.SC_OK);
        break;
      case FOLLOW_TAG:
        if (tagID == null) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }

        // Call the FirestoreDB method.
        db.setUserProperty(uid, tagID, User.TAGS_FOLLOWED_KEY, true);
        response.setStatus(HttpServletResponse.SC_OK);
        break;
      case UNFOLLOW_TAG:
        if (tagID == null) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
        // Call the FirestoreDB method.
        db.deleteUserProperty(uid, tagID, User.TAGS_FOLLOWED_KEY);
        response.setStatus(HttpServletResponse.SC_OK);
        break;
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String token = request.getParameter("token");

    String uid = Auth.getUid(token, response);
    if (uid == null) {
      // Auth sets response status to unauthorized.
      return;
    }

    // Call the FirestoreDB method.
    db.deleteUser(uid);
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
