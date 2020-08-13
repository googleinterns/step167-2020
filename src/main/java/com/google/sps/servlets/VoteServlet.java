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

import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.sps.meltingpot.data.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles recipe vote requests. */
@WebServlet("/api/vote")
public class VoteServlet extends HttpServlet {
  private Gson gson = new Gson();
  private DBInterface db;

  public VoteServlet(DBInterface db) {
    this.db = db;
  }

  public VoteServlet() {}

  @Override
  public void init() {
    db = new FirestoreDB();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String[] recipeIds = request.getParameterValues("recipeIds");
    String token = request.getParameter("token");

    if (recipeIds == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    FirebaseToken authToken = Auth.verifyIdToken(token);
    if (authToken == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    response.setContentType("application/json");
    response.getWriter().println(
        gson.toJson(db.inUserMap(authToken.getUid(), recipeIds, User.VOTED_RECIPES_KEY)));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeId = request.getParameter("recipeId");
    String vote = request.getParameter("vote");
    String token = request.getParameter("token");

    if (recipeId == null || vote == null || (!vote.equals("true") && !vote.equals("false"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (!db.isDocument(recipeId, DBUtils.DB_RECIPES_COLLECTION)) {
      // recipe does not exist
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    FirebaseToken authToken = Auth.verifyIdToken(token);
    if (authToken == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String uid = authToken.getUid();
    if (!db.isUser(uid)) {
      db.addUser(uid);
    }

    /*
    Expected Behavior: Go to position in table
    vote \  Current Position | Upvoted | Neutral | Downvoted
    --------------------------------------------------------------
    True (upvote request)    | Neutal  | Upvote  | Upvote
    False (downvote request) | Downvote| Downvote| Neutral
    */

    DBUtils.blockOnFuture(DBUtils.database.runTransaction(transaction -> {
      Boolean votedRecipe = db.inUserMap(uid, recipeId, User.VOTED_RECIPES_KEY, transaction);
      if (votedRecipe != null) {
        if (votedRecipe == true) { // previously upvoted
          if (vote.equals("true")) { // request wants to reset the upvote
            db.voteRecipe(recipeId, -1, transaction);
            db.deleteUserProperty(uid, recipeId, User.VOTED_RECIPES_KEY, transaction);
          } else { // request wants to change upvote to downvote
            db.voteRecipe(recipeId, -2, transaction);
            db.setUserProperty(uid, recipeId, User.VOTED_RECIPES_KEY, false, transaction);
          }
        } else { // previously downvoted
          if (vote.equals("true")) { // request wants to change downvote to upvote
            db.voteRecipe(recipeId, 2, transaction);
            db.setUserProperty(uid, recipeId, User.VOTED_RECIPES_KEY, true, transaction);
          } else { // request wants to reset the downvote
            db.voteRecipe(recipeId, 1, transaction);
            db.deleteUserProperty(uid, recipeId, User.VOTED_RECIPES_KEY, transaction);
          }
        }
      } else { // previously neutral
        if (vote.equals("true")) { // request wants to upvote
          db.voteRecipe(recipeId, 1, transaction);
          db.setUserProperty(uid, recipeId, User.VOTED_RECIPES_KEY, true, transaction);
        } else { // request wants to downvote
          db.voteRecipe(recipeId, -1, transaction);
          db.setUserProperty(uid, recipeId, User.VOTED_RECIPES_KEY, false, transaction);
        }
      }
      return 0;
    }));
  }
}
